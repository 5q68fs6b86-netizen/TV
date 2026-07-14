package com.fongmi.android.tv.ai.subtitle;

import android.os.SystemClock;
import android.util.Log;

import com.k2fsa.sherpa.onnx.FeatureConfig;
import com.k2fsa.sherpa.onnx.EndpointConfig;
import com.k2fsa.sherpa.onnx.EndpointRule;
import com.k2fsa.sherpa.onnx.OfflineModelConfig;
import com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig;
import com.k2fsa.sherpa.onnx.OfflineQwen3AsrModelConfig;
import com.k2fsa.sherpa.onnx.OfflineRecognizer;
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig;
import com.k2fsa.sherpa.onnx.OfflineRecognizerResult;
import com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig;
import com.k2fsa.sherpa.onnx.OfflineStream;
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig;
import com.k2fsa.sherpa.onnx.OfflineWenetCtcModelConfig;
import com.k2fsa.sherpa.onnx.OnlineModelConfig;
import com.k2fsa.sherpa.onnx.OnlineRecognizer;
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig;
import com.k2fsa.sherpa.onnx.OnlineRecognizerResult;
import com.k2fsa.sherpa.onnx.OnlineStream;
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig;
import com.k2fsa.sherpa.onnx.OnlineZipformer2CtcModelConfig;
import com.k2fsa.sherpa.onnx.SileroVadModelConfig;
import com.k2fsa.sherpa.onnx.SpeechSegment;
import com.k2fsa.sherpa.onnx.Vad;
import com.k2fsa.sherpa.onnx.VadModelConfig;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class SherpaSubtitleController {
    private static final String TAG = "AiSubtitle";
    private static final long PCM_GAP_RESET_US = 50_000L;
    private static final long STREAMING_MAX_UTTERANCE_MS = 4_800L;
    private static final long STREAMING_MIN_UTTERANCE_MS = 1_200L;
    // The callback reserve includes network translation, the PCM/tap scheduling gap and a small
    // main-thread hand-off margin.  Samsung multi-channel AudioTrack paths expose only about 5.2 s
    // of real headroom, so keeping the old fixed 4.8 s endpoint left no time for translation.
    private static final long MIN_TRANSLATION_RESERVE_MS = 2_600L;
    private static final long TRANSLATION_RUNTIME_MARGIN_MS = 1_200L;

    public interface Listener {
        void onRecognized(RecognizedSegment segment);

        void onStatus(String status);
    }

    public record RecognizedSegment(String text, String detectedLanguage, long startUs, long endUs,
                                    long decodeMs, long audioMs) {
    }

    public static final class Metrics {
        public final long offeredChunks;
        public final long droppedChunks;
        public final long droppedSpeechSegments;
        public final long recognizedSegments;
        public final long maxOfferMicros;

        Metrics(long offeredChunks, long droppedChunks, long droppedSpeechSegments,
                long recognizedSegments, long maxOfferMicros) {
            this.offeredChunks = offeredChunks;
            this.droppedChunks = droppedChunks;
            this.droppedSpeechSegments = droppedSpeechSegments;
            this.recognizedSegments = recognizedSegments;
            this.maxOfferMicros = maxOfferMicros;
        }
    }

    private record PcmChunk(float[] samples, int sampleRate, long startUs, long endUs) {
    }

    private record SpeechChunk(float[] samples, long startUs, long endUs) {
    }

    private final AsrModelManager models;
    private final Listener listener;
    // PCM/VAD must keep consuming while the offline recognizer is busy.  We only drop at the
    // already-segmented speech queue, where absolute sample timestamps remain intact.
    private final LatestQueue<PcmChunk> pcmQueue = new LatestQueue<>(256);
    private final LatestQueue<SpeechChunk> speechQueue = new LatestQueue<>(3);
    private final ExecutorService vadEngine = Executors.newSingleThreadExecutor(r -> thread(r, "ai-sherpa-vad"));
    private final ExecutorService recognizerEngine = Executors.newSingleThreadExecutor(r -> thread(r, "ai-sherpa-asr"));
    private final AtomicInteger generation = new AtomicInteger();
    private final AtomicLong offeredChunks = new AtomicLong();
    private final AtomicLong droppedSpeechSegments = new AtomicLong();
    private final AtomicLong recognizedSegments = new AtomicLong();
    private final AtomicLong maxOfferMicros = new AtomicLong();
    private volatile boolean active;
    private volatile long audioLookaheadMs = Long.MAX_VALUE;
    private volatile long worstTranslationCallbackMs;

    public SherpaSubtitleController(AsrModelManager models, Listener listener) {
        this.models = models;
        this.listener = listener;
    }

    private static Thread thread(Runnable runnable, String name) {
        Thread thread = new Thread(runnable, name);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    }

    public void start(AiLanguage language) {
        stop();
        audioLookaheadMs = Long.MAX_VALUE;
        worstTranslationCallbackMs = 0L;
        active = true;
        int token = generation.incrementAndGet();
        vadEngine.execute(() -> run(token, language));
    }

    public void stop() {
        active = false;
        generation.incrementAndGet();
        pcmQueue.clear();
        speechQueue.clear();
    }

    public void offer(float[] mono, int sampleRate, long startUs, long endUs) {
        if (!active || mono == null || mono.length == 0 || sampleRate <= 0 || endUs <= startUs) return;
        long start = System.nanoTime();
        offeredChunks.incrementAndGet();
        pcmQueue.offerLatest(new PcmChunk(mono, sampleRate, startUs, endUs));
        long micros = (System.nanoTime() - start) / 1000L;
        maxOfferMicros.accumulateAndGet(micros, Math::max);
    }

    public Metrics metrics() {
        return new Metrics(offeredChunks.get(), pcmQueue.dropped(),
                droppedSpeechSegments.get() + speechQueue.dropped(),
                recognizedSegments.get(), maxOfferMicros.get());
    }

    /** Updates the real AudioTrack headroom without restarting the recognizer. */
    public void updateAudioLookaheadMs(long lookaheadMs) {
        if (lookaheadMs > 0L) audioLookaheadMs = lookaheadMs;
    }

    /** Feeds completed request latency back into the next streaming endpoint budget. */
    public void recordTranslationCallbackMs(long callbackMs) {
        if (callbackMs > 0L) worstTranslationCallbackMs = Math.max(worstTranslationCallbackMs, callbackMs);
    }

    static long streamingUtteranceBudgetMs(long lookaheadMs, long worstCallbackMs) {
        if (lookaheadMs == Long.MAX_VALUE || lookaheadMs <= 0L) return STREAMING_MAX_UTTERANCE_MS;
        long reserveMs = Math.max(MIN_TRANSLATION_RESERVE_MS,
                Math.max(0L, worstCallbackMs) + TRANSLATION_RUNTIME_MARGIN_MS);
        return Math.max(STREAMING_MIN_UTTERANCE_MS,
                Math.min(STREAMING_MAX_UTTERANCE_MS, lookaheadMs - reserveMs));
    }

    private void run(int token, AiLanguage language) {
        if (language.model().isStreaming()) runStreaming(token, language);
        else runOffline(token, language);
    }

    private void runOffline(int token, AiLanguage language) {
        OfflineRecognizer recognizer = null;
        Vad vad = null;
        Future<?> recognition = null;
        try {
            if (!models.isInstalled(language)) {
                status(token, "请先下载“" + language.label() + "”语言包");
                return;
            }
            if (!models.canRun(language)) {
                status(token, "设备内存不足，已阻止加载该语言包");
                return;
            }
            status(token, "正在加载“" + language.label() + "”专用语言包");
            Log.i(TAG, "ASR route language=" + language.code() + " model=" + language.model().folder
                    + " engine=" + language.model().engine);
            recognizer = new OfflineRecognizer(buildRecognizer(language));
            vad = new Vad(buildVad(language));
            OfflineRecognizer sessionRecognizer = recognizer;
            recognition = recognizerEngine.submit(() -> recognizeLoop(token, sessionRecognizer));
            status(token, "实时字幕引擎已就绪");
            consumePcm(token, vad, flushSamples(language));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (Throwable error) {
            Log.e(TAG, "recognition skipped after engine error", error);
        } finally {
            // If this run ended by itself, invalidate its recognizer loop.  A newer generation is
            // never overwritten by the compare-and-set.
            if (generation.compareAndSet(token, token + 1)) active = false;
            speechQueue.clear();
            if (recognition != null) {
                try {
                    recognition.get();
                } catch (Throwable ignored) {
                }
            }
            if (vad != null) {
                try {
                    vad.release();
                } catch (Throwable ignored) {
                }
            }
            if (recognizer != null) {
                try {
                    recognizer.release();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private void runStreaming(int token, AiLanguage language) {
        OnlineRecognizer recognizer = null;
        OnlineStream stream = null;
        try {
            if (!models.isInstalled(language)) {
                status(token, "请先下载“" + language.label() + "”语言包");
                return;
            }
            if (!models.canRun(language)) {
                status(token, "设备内存不足，已阻止加载该语言包");
                return;
            }
            status(token, "正在加载“" + language.label() + "”专用语言包");
            Log.i(TAG, "ASR route language=" + language.code() + " model=" + language.model().folder
                    + " engine=" + language.model().engine);
            long loadStarted = SystemClock.elapsedRealtime();
            recognizer = new OnlineRecognizer(buildOnlineRecognizer(language));
            stream = recognizer.createStream();
            Log.i(TAG, "streaming ASR loaded model=" + language.model().folder
                    + " loadMs=" + (SystemClock.elapsedRealtime() - loadStarted));
            status(token, "实时字幕引擎已就绪");
            consumeStreamingPcm(token, language, recognizer, stream);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (Throwable error) {
            Log.e(TAG, "streaming recognition skipped after engine error", error);
        } finally {
            if (generation.compareAndSet(token, token + 1)) active = false;
            if (stream != null) {
                try {
                    stream.release();
                } catch (Throwable ignored) {
                }
            }
            if (recognizer != null) {
                try {
                    recognizer.release();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private void consumeStreamingPcm(int token, AiLanguage language, OnlineRecognizer recognizer,
                                     OnlineStream stream) throws InterruptedException {
        long streamBaseUs = Long.MIN_VALUE;
        long previousEndUs = Long.MIN_VALUE;
        long decodeNanos = 0L;
        while (isCurrent(token)) {
            PcmChunk chunk = pcmQueue.poll(120, TimeUnit.MILLISECONDS);
            if (chunk == null) continue;
            boolean discontinuity = previousEndUs != Long.MIN_VALUE
                    && Math.abs(chunk.startUs - previousEndUs) > PCM_GAP_RESET_US;
            if (streamBaseUs == Long.MIN_VALUE || discontinuity) {
                if (discontinuity) {
                    Log.i(TAG, "pcm timeline gap resetStreaming gapUs=" + (chunk.startUs - previousEndUs));
                    recognizer.reset(stream);
                    decodeNanos = 0L;
                }
                streamBaseUs = chunk.startUs;
            }
            previousEndUs = chunk.endUs;
            float[] samples = resample(chunk.samples, chunk.sampleRate, 16000);
            if (samples.length == 0) continue;
            stream.acceptWaveform(samples, 16000);
            while (isCurrent(token) && recognizer.isReady(stream)) {
                long started = System.nanoTime();
                recognizer.decode(stream);
                decodeNanos += System.nanoTime() - started;
            }
            long utteranceMs = Math.max(0L, (chunk.endUs - streamBaseUs) / 1_000L);
            long budgetMs = streamingUtteranceBudgetMs(audioLookaheadMs, worstTranslationCallbackMs);
            boolean endpoint = recognizer.isEndpoint(stream);
            boolean adaptiveBoundary = utteranceMs >= budgetMs;
            if (!isCurrent(token) || (!endpoint && !adaptiveBoundary)) continue;
            OnlineRecognizerResult result = recognizer.getResult(stream);
            Log.i(TAG, "streaming endpoint adaptive=" + adaptiveBoundary
                    + " recognizer=" + endpoint + " utteranceMs=" + utteranceMs
                    + " budgetMs=" + budgetMs + " lookaheadMs=" + audioLookaheadMs
                    + " worstTranslationMs=" + worstTranslationCallbackMs);
            emitStreamingResult(token, language, result, streamBaseUs, chunk.endUs,
                    TimeUnit.NANOSECONDS.toMillis(decodeNanos));
            recognizer.reset(stream);
            streamBaseUs = chunk.endUs;
            decodeNanos = 0L;
        }
    }

    private void emitStreamingResult(int token, AiLanguage language, OnlineRecognizerResult result,
                                     long streamBaseUs, long currentEndUs, long decodeMs) {
        if (!isCurrent(token) || result == null) return;
        String text = result.getText() == null ? "" : result.getText().trim();
        if (text.isEmpty() || !containsSpeechText(text)) return;
        float[] timestamps = result.getTimestamps();
        long startUs;
        long endUs;
        if (timestamps != null && timestamps.length > 0) {
            startUs = streamBaseUs + Math.max(0L, (long) (timestamps[0] * 1_000_000L));
            endUs = streamBaseUs + Math.max(0L,
                    (long) ((timestamps[timestamps.length - 1] + 0.32f) * 1_000_000L));
            endUs = Math.min(currentEndUs, endUs);
        } else {
            endUs = currentEndUs;
            startUs = Math.max(streamBaseUs,
                    endUs - STREAMING_MAX_UTTERANCE_MS * 1_000L);
        }
        if (endUs <= startUs) {
            endUs = currentEndUs;
            startUs = Math.max(streamBaseUs, endUs - 800_000L);
        }
        long audioMs = Math.max(1L, (endUs - startUs) / 1000L);
        recognizedSegments.incrementAndGet();
        Log.i(TAG, "streaming ASR final language=" + language.code() + " decodeMs=" + decodeMs
                + " audioMs=" + audioMs + " text=" + text);
        listener.onRecognized(new RecognizedSegment(text, language.code(), startUs, endUs,
                decodeMs, audioMs));
    }

    private void consumePcm(int token, Vad vad, int flushSamples) throws InterruptedException {
        int speechSamples = 0;
        long vadBaseUs = Long.MIN_VALUE;
        long previousEndUs = Long.MIN_VALUE;
        while (isCurrent(token)) {
            PcmChunk chunk = pcmQueue.poll(120, TimeUnit.MILLISECONDS);
            if (chunk == null) continue;
            boolean discontinuity = previousEndUs != Long.MIN_VALUE
                    && Math.abs(chunk.startUs - previousEndUs) > PCM_GAP_RESET_US;
            if (vadBaseUs == Long.MIN_VALUE || discontinuity) {
                if (discontinuity) {
                    Log.i(TAG, "pcm timeline gap resetVad gapUs=" + (chunk.startUs - previousEndUs));
                    vad.reset();
                }
                vadBaseUs = chunk.startUs;
                speechSamples = 0;
            }
            previousEndUs = chunk.endUs;
            float[] samples = resample(chunk.samples, chunk.sampleRate, 16000);
            if (samples.length == 0) continue;
            vad.acceptWaveform(samples);
            speechSamples = vad.isSpeechDetected() ? speechSamples + samples.length : 0;
            if (speechSamples >= flushSamples) {
                vad.flush();
                speechSamples = 0;
            }
            while (!vad.empty() && isCurrent(token)) {
                SpeechSegment segment = vad.front();
                vad.pop();
                float[] speech = segment.getSamples();
                if (speech.length < 3200) continue;
                long startUs = vadBaseUs + segment.getStart() * 1_000_000L / 16000L;
                long endUs = startUs + speech.length * 1_000_000L / 16000L;
                speechQueue.offerLatest(new SpeechChunk(speech, startUs, endUs));
            }
        }
    }

    private void recognizeLoop(int token, OfflineRecognizer recognizer) {
        try {
            while (isCurrent(token)) {
                SpeechChunk speech = speechQueue.poll(120, TimeUnit.MILLISECONDS);
                if (speech == null) continue;
                int skipped = 0;
                SpeechChunk newer;
                while ((newer = speechQueue.poll()) != null) {
                    speech = newer;
                    skipped++;
                }
                if (skipped > 0) {
                    droppedSpeechSegments.addAndGet(skipped);
                    Log.i(TAG, "ASR catch-up droppedSpeechSegments=" + skipped);
                }
                recognize(token, recognizer, speech);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void recognize(int token, OfflineRecognizer recognizer, SpeechChunk speech) {
        OfflineStream stream = recognizer.createStream();
        long started = SystemClock.elapsedRealtime();
        try {
            stream.acceptWaveform(speech.samples, 16000);
            recognizer.decode(stream);
            OfflineRecognizerResult result = recognizer.getResult(stream);
            if (!isCurrent(token)) return;
            String text = result.getText() == null ? "" : result.getText().trim();
            if (!text.isEmpty()) {
                long decodeMs = SystemClock.elapsedRealtime() - started;
                long audioMs = speech.samples.length * 1000L / 16000L;
                recognizedSegments.incrementAndGet();
                listener.onRecognized(new RecognizedSegment(text, result.getLang(), speech.startUs,
                        speech.endUs, decodeMs, audioMs));
            }
        } finally {
            stream.release();
        }
    }

    private boolean isCurrent(int token) {
        return active && token == generation.get();
    }

    private void status(int token, String value) {
        if (isCurrent(token)) listener.onStatus(value);
    }

    private int flushSamples(AiLanguage language) {
        boolean whisper = language.model().engine == AsrModel.Engine.OFFLINE_WHISPER;
        // Shorter Whisper chunks finish inside the TV's measured 6-7 s PCM headroom more often,
        // allowing the cue to keep the speech segment's full duration instead of flashing a tail.
        return (int) (16000 * (whisper ? 2.8f : 2.2f));
    }

    private OfflineRecognizerConfig buildRecognizer(AiLanguage language) {
        FeatureConfig feature = FeatureConfig.builder().setSampleRate(16000).setFeatureDim(80).setDither(0).build();
        OfflineModelConfig model = buildModel(language);
        return OfflineRecognizerConfig.builder().setFeatureConfig(feature).setOfflineModelConfig(model).build();
    }

    private OfflineModelConfig buildModel(AiLanguage language) {
        AsrModel selected = language.model();
        File dir = models.modelDir(selected);
        OfflineModelConfig.Builder builder = OfflineModelConfig.builder()
                .setNumThreads(AiSubtitleSettings.getAsrThreads())
                .setDebug(false)
                .setProvider("cpu");
        switch (selected) {
            case CANTONESE_WENET_CTC -> builder
                    .setWenetCtc(OfflineWenetCtcModelConfig.builder()
                            .setModel(new File(dir, "model.int8.onnx").getAbsolutePath())
                            .build())
                    .setTokens(new File(dir, "tokens.txt").getAbsolutePath())
                    .setModelType("wenet_ctc");
            case JAPANESE_MOONSHINE -> builder
                    .setMoonshine(OfflineMoonshineModelConfig.builder()
                            .setEncoder(new File(dir, "encoder_model.ort").getAbsolutePath())
                            .setMergedDecoder(new File(dir, "decoder_model_merged.ort").getAbsolutePath())
                            .build())
                    .setTokens(new File(dir, "tokens.txt").getAbsolutePath())
                    .setModelType("moonshine");
            case SENSE_VOICE -> builder
                    .setSenseVoice(OfflineSenseVoiceModelConfig.builder()
                            .setModel(new File(dir, "model.int8.onnx").getAbsolutePath())
                            .setLanguage(language.code())
                            .setInverseTextNormalization(true)
                            .build())
                    .setTokens(new File(dir, "tokens.txt").getAbsolutePath())
                    .setModelType("sense_voice");
            case WHISPER_BASE -> builder
                    .setWhisper(whisper(dir, "base", language.code()))
                    .setTokens(new File(dir, "base-tokens.txt").getAbsolutePath())
                    .setModelType("whisper");
            case WHISPER_SMALL -> builder
                    .setWhisper(whisper(dir, "small", language.code()))
                    .setTokens(new File(dir, "small-tokens.txt").getAbsolutePath())
                    .setModelType("whisper");
            case WHISPER_MEDIUM -> builder
                    .setWhisper(whisper(dir, "medium", language.code()))
                    .setTokens(new File(dir, "medium-tokens.txt").getAbsolutePath())
                    .setModelType("whisper");
            case QWEN3_ASR -> builder
                    .setQwen3Asr(OfflineQwen3AsrModelConfig.builder()
                            .setConvFrontend(new File(dir, "conv_frontend.onnx").getAbsolutePath())
                            .setEncoder(new File(dir, "encoder.int8.onnx").getAbsolutePath())
                            .setDecoder(new File(dir, "decoder.int8.onnx").getAbsolutePath())
                            .setTokenizer(new File(dir, "tokenizer").getAbsolutePath())
                            .setTemperature(0)
                            .setTopP(1)
                            .setSeed(0)
                            .build())
                    .setModelType("qwen3_asr");
            case MANDARIN_ZIPFORMER_CTC, GERMAN_KROKO, ENGLISH_KROKO,
                    FRENCH_KROKO, SPANISH_KROKO -> throw new IllegalStateException(
                    "Streaming model cannot be built by OfflineRecognizer: " + selected);
        }
        return builder.build();
    }

    private OnlineRecognizerConfig buildOnlineRecognizer(AiLanguage language) {
        AsrModel selected = language.model();
        File dir = models.modelDir(selected);
        OnlineModelConfig.Builder model = OnlineModelConfig.builder()
                .setTokens(new File(dir, "tokens.txt").getAbsolutePath())
                .setNumThreads(AiSubtitleSettings.getAsrThreads())
                .setDebug(false)
                .setProvider("cpu");
        switch (selected.engine) {
            case ONLINE_TRANSDUCER -> model
                    .setTransducer(OnlineTransducerModelConfig.builder()
                            .setEncoder(new File(dir, "encoder.onnx").getAbsolutePath())
                            .setDecoder(new File(dir, "decoder.onnx").getAbsolutePath())
                            .setJoiner(new File(dir, "joiner.onnx").getAbsolutePath())
                            .build())
                    .setModelType("zipformer2");
            case ONLINE_ZIPFORMER_CTC -> model
                    .setZipformer2Ctc(OnlineZipformer2CtcModelConfig.builder()
                            .setModel(new File(dir, "model.int8.onnx").getAbsolutePath())
                            .build())
                    .setModelType("zipformer2_ctc");
            default -> throw new IllegalStateException("Offline model cannot be built by OnlineRecognizer: " + selected);
        }
        EndpointConfig endpoint = EndpointConfig.builder()
                .setRule1(EndpointRule.builder()
                        .setMustContainNonSilence(false)
                        .setMinTrailingSilence(2.4f)
                        .setMinUtteranceLength(0f)
                        .build())
                .setRule2(EndpointRule.builder()
                        .setMustContainNonSilence(true)
                        .setMinTrailingSilence(0.36f)
                        .setMinUtteranceLength(0f)
                        .build())
                .setRule3(EndpointRule.builder()
                        .setMustContainNonSilence(false)
                        .setMinTrailingSilence(0f)
                        .setMinUtteranceLength(STREAMING_MAX_UTTERANCE_MS / 1_000f)
                        .build())
                .build();
        FeatureConfig feature = FeatureConfig.builder()
                .setSampleRate(16000)
                .setFeatureDim(80)
                .setDither(0)
                .build();
        return OnlineRecognizerConfig.builder()
                .setFeatureConfig(feature)
                .setOnlineModelConfig(model.build())
                .setEndpointConfig(endpoint)
                .setEnableEndpoint(true)
                .setDecodingMethod("greedy_search")
                .setMaxActivePaths(4)
                .build();
    }

    private static boolean containsSpeechText(String text) {
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            if (Character.isLetterOrDigit(codePoint)) return true;
            i += Character.charCount(codePoint);
        }
        return false;
    }

    private static OfflineWhisperModelConfig whisper(File dir, String prefix, String language) {
        return OfflineWhisperModelConfig.builder()
                .setEncoder(new File(dir, prefix + "-encoder.int8.onnx").getAbsolutePath())
                .setDecoder(new File(dir, prefix + "-decoder.int8.onnx").getAbsolutePath())
                .setLanguage(language)
                .setTask("transcribe")
                .setTailPaddings(80)
                .setEnableTokenTimestamps(true)
                .setEnableSegmentTimestamps(false)
                .build();
    }

    private VadModelConfig buildVad(AiLanguage language) {
        boolean whisper = language.model().engine == AsrModel.Engine.OFFLINE_WHISPER;
        SileroVadModelConfig silero = SileroVadModelConfig.builder()
                .setModel(models.vadFile().getAbsolutePath())
                .setThreshold(0.5f)
                .setMinSilenceDuration(0.3f)
                .setMinSpeechDuration(0.25f)
                .setWindowSize(512)
                .setMaxSpeechDuration(whisper ? 3.2f : 3.0f)
                .build();
        return VadModelConfig.builder()
                .setSileroVadModelConfig(silero)
                .setSampleRate(16000)
                .setNumThreads(1)
                .setProvider("cpu")
                .setDebug(false)
                .build();
    }

    static float[] resample(float[] input, int fromRate, int toRate) {
        if (fromRate == toRate) return input;
        if (input.length < 2 || fromRate <= 0 || toRate <= 0) return new float[0];
        int outputLength = Math.max(1, (int) (((long) input.length * toRate) / fromRate));
        float[] output = new float[outputLength];
        double step = fromRate / (double) toRate;
        for (int i = 0; i < outputLength; i++) {
            double source = i * step;
            int left = Math.min((int) source, input.length - 1);
            int right = Math.min(left + 1, input.length - 1);
            float fraction = (float) (source - left);
            output[i] = input[left] + (input[right] - input[left]) * fraction;
        }
        return output;
    }
}
