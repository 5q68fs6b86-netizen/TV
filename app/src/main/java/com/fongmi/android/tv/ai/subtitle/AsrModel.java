package com.fongmi.android.tv.ai.subtitle;

public enum AsrModel {
    MANDARIN_ZIPFORMER_CTC(
            "mandarin-zipformer-ctc",
            1280,
            Engine.ONLINE_ZIPFORMER_CTC,
            false,
            new ModelFile("model.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-ctc-zh-int8-2025-06-30/resolve/f29132af655b91f258800df19c5b5d9b9281a5cd/model.int8.onnx", 162290887L, "24ffdc19ba9aaed5a6a9beaede1e087745217d82425cf4041bca0c696661801e"),
            new ModelFile("tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-ctc-zh-int8-2025-06-30/resolve/f29132af655b91f258800df19c5b5d9b9281a5cd/tokens.txt", 20628L, "6193c7ea1c96d0d9a1e9652789b40d13a8a913b434a5451e93158f5a09fd6652")),
    CANTONESE_WENET_CTC(
            "cantonese-wenetspeech-ctc",
            1280,
            Engine.OFFLINE_WENET_CTC,
            true,
            new ModelFile("model.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-wenetspeech-yue-u2pp-conformer-ctc-zh-en-cantonese-int8-2025-09-10/resolve/c911764e8227cf372ee60adc75f7f407e5b8c905/model.int8.onnx", 134698500L, "201bfd9e12ec4ac9ee3b23c5e071d9fa2381a8b21df317e2e08a170d6f1f55d3"),
            new ModelFile("tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-wenetspeech-yue-u2pp-conformer-ctc-zh-en-cantonese-int8-2025-09-10/resolve/c911764e8227cf372ee60adc75f7f407e5b8c905/tokens.txt", 85361L, "c7750677a1183606d2fd6f16d792e06e70d9843dba8a0c6e23a9dec78e06977a")),
    GERMAN_KROKO(
            "german-kroko-streaming",
            1024,
            Engine.ONLINE_TRANSDUCER,
            false,
            new ModelFile("encoder.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-de-kroko-2025-08-06/resolve/887db3d083240198c2d2b99fb66cfcfe6948ced8/encoder.onnx", 70091557L, "6e83993d6967ec7a3498b055b7e85ace85b5d64d1b1e8773cb29a43a11f5edb5"),
            new ModelFile("decoder.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-de-kroko-2025-08-06/resolve/887db3d083240198c2d2b99fb66cfcfe6948ced8/decoder.onnx", 617489L, "94a29592b403c53fa2231b478637da1ab4abcef7f5e46e432098416a4a3ed562"),
            new ModelFile("joiner.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-de-kroko-2025-08-06/resolve/887db3d083240198c2d2b99fb66cfcfe6948ced8/joiner.onnx", 336817L, "28356bff070aea51ab1d725a3278e81d19f9300f860d3248a7014292264df15a"),
            new ModelFile("tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-de-kroko-2025-08-06/resolve/887db3d083240198c2d2b99fb66cfcfe6948ced8/tokens.txt", 5606L, "86e8370994ff2c01149ba8c4f8709aa93cdc18914b27a717e291e96faf39a6eb")),
    ENGLISH_KROKO(
            "english-kroko-streaming",
            1024,
            Engine.ONLINE_TRANSDUCER,
            false,
            new ModelFile("encoder.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-en-kroko-2025-08-06/resolve/572aaf4e2e0c603c3fc2a574d096e755a178faa1/encoder.onnx", 70092599L, "d4881c57449d581e0770fd53fa66c2fdc6cd167d92ece7c715e603defc96d9d4"),
            new ModelFile("decoder.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-en-kroko-2025-08-06/resolve/572aaf4e2e0c603c3fc2a574d096e755a178faa1/decoder.onnx", 617488L, "455ba38466fce8d5a57e7db68a323b684079ca4d9e1dd93a740d9b2429aae3b1"),
            new ModelFile("joiner.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-en-kroko-2025-08-06/resolve/572aaf4e2e0c603c3fc2a574d096e755a178faa1/joiner.onnx", 336817L, "d406f616736350e2a7df3e39398b78eb2fc1a2ca6973a19d3853fa3227e25b52"),
            new ModelFile("tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-en-kroko-2025-08-06/resolve/572aaf4e2e0c603c3fc2a574d096e755a178faa1/tokens.txt", 6310L, "396dbeb5f4858875690716084f54e90d339679d0ba3e6b5b584f3d7589254d2d")),
    FRENCH_KROKO(
            "french-kroko-streaming",
            1024,
            Engine.ONLINE_TRANSDUCER,
            false,
            new ModelFile("encoder.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-fr-kroko-2025-08-06/resolve/08b84b7b7cf519be9817e9c16919d96a7a8bad91/encoder.onnx", 70092599L, "e02facae1daf6f1f13da67ea3ace7c722516d0868d1768d78c0580bc22cc0c5b"),
            new ModelFile("decoder.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-fr-kroko-2025-08-06/resolve/08b84b7b7cf519be9817e9c16919d96a7a8bad91/decoder.onnx", 617488L, "6aed547570e3ab5afc05429a017cedd3a056c16df3baa5703f02461cefa25bac"),
            new ModelFile("joiner.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-fr-kroko-2025-08-06/resolve/08b84b7b7cf519be9817e9c16919d96a7a8bad91/joiner.onnx", 336817L, "a51eec759bcdcaae2614686fa2a8b57417b2d420dd55a5a5558b388d35a9b2b6"),
            new ModelFile("tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-fr-kroko-2025-08-06/resolve/08b84b7b7cf519be9817e9c16919d96a7a8bad91/tokens.txt", 5415L, "fedfb9c844bfb2bf14171f8184863e3d617b815a8667bdd9fc9a3149fde73298")),
    SPANISH_KROKO(
            "spanish-kroko-streaming",
            1280,
            Engine.ONLINE_TRANSDUCER,
            false,
            new ModelFile("encoder.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-es-kroko-2025-08-06/resolve/20cf7a4921613397841d31168796cade5b866585/encoder.onnx", 154878102L, "2d9f5ef87d1a5257f8a6687e21501c56f3aa2fcbfcfab9364dcc4ce4e06ae81b"),
            new ModelFile("decoder.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-es-kroko-2025-08-06/resolve/20cf7a4921613397841d31168796cade5b866585/decoder.onnx", 617488L, "d4ce176b94b25f7acc88717bc3f704fcf5d6e131aaac2e0cabab3885541181ee"),
            new ModelFile("joiner.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-es-kroko-2025-08-06/resolve/20cf7a4921613397841d31168796cade5b866585/joiner.onnx", 336817L, "dae35df88d676e320fcdb99217328e66dcf722bf11b0f2459e14ddb5b982ded5"),
            new ModelFile("tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-es-kroko-2025-08-06/resolve/20cf7a4921613397841d31168796cade5b866585/tokens.txt", 6385L, "1be5e0a58e05d06d327df4c6b7b5e4f8aba01da6981eb016fcaceafc6a56680f")),
    JAPANESE_MOONSHINE(
            "japanese-moonshine-tiny",
            1280,
            Engine.OFFLINE_MOONSHINE,
            true,
            new ModelFile("encoder_model.ort", "https://huggingface.co/csukuangfj2/sherpa-onnx-moonshine-tiny-ja-quantized-2026-02-27/resolve/550e2eb0a8b33092f3b394f64649ee1b3d9eb506/encoder_model.ort", 13238184L, "86ece73812604b9b5f1274b4d1e6eec0d783b96088ff46d49e30a53f881cad73"),
            new ModelFile("decoder_model_merged.ort", "https://huggingface.co/csukuangfj2/sherpa-onnx-moonshine-tiny-ja-quantized-2026-02-27/resolve/550e2eb0a8b33092f3b394f64649ee1b3d9eb506/decoder_model_merged.ort", 58327272L, "9fcd9b71323a496b307e20dd305c4e9a1b533c7bedd6e4f660e974967dd60bb6"),
            new ModelFile("tokens.txt", "https://huggingface.co/csukuangfj2/sherpa-onnx-moonshine-tiny-ja-quantized-2026-02-27/resolve/550e2eb0a8b33092f3b394f64649ee1b3d9eb506/tokens.txt", 549350L, "2870d843e14c1e187bf1913a521562a63b53933814bd7f2145120468f494a049")),
    SENSE_VOICE(
            "sense-voice",
            1536,
            Engine.OFFLINE_SENSE_VOICE,
            true,
            new ModelFile("model.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2025-09-09/resolve/main/model.int8.onnx", 237115547L, "12ca1a2ae7ecf3e0019ef2822307ee0b5cadc9196569e379b4c4026f8205276d"),
            new ModelFile("tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2025-09-09/resolve/main/tokens.txt", 315894L, "f449eb28dc567533d7fa59be34e2abca8784f771850c78a47fb731a31429a1dc")),
    WHISPER_BASE(
            "whisper-base",
            1792,
            Engine.OFFLINE_WHISPER,
            true,
            new ModelFile("base-encoder.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-encoder.int8.onnx", 29120534L, "0b8fb1304b6109976038efff5ace81720e00386f3ff6b54ee8c75291ca0a1e11"),
            new ModelFile("base-decoder.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-decoder.int8.onnx", 130672026L, "9759d217388a01b3a4c7c15533201067b48ae819c4daafc8624e64b9409dc02d"),
            new ModelFile("base-tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-tokens.txt", 816730L, "b34b360dbb493e781e479794586d661700670d65564001f23024971d1f2fa126")),
    WHISPER_SMALL(
            "whisper-small",
            4096,
            Engine.OFFLINE_WHISPER,
            true,
            new ModelFile("small-encoder.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-encoder.int8.onnx", 112442483L, "4cbe7b22fa9026b843b60a68640c747de05bafb1a11b57edc0e66c232d9f33a9"),
            new ModelFile("small-decoder.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-decoder.int8.onnx", 262226114L, "acad50b5c782696e91b55914cc5ab4f756f1532f76e22aa6fc615f39fb69a8ee"),
            new ModelFile("small-tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-tokens.txt", 816730L, "b34b360dbb493e781e479794586d661700670d65564001f23024971d1f2fa126")),
    WHISPER_MEDIUM(
            "whisper-medium",
            6144,
            Engine.OFFLINE_WHISPER,
            true,
            new ModelFile("medium-encoder.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-medium/resolve/main/medium-encoder.int8.onnx", 374196283L, "1c54582b4d829de0089f6cb63bbbdb3bf7555398bacaf855fbecf1a84dfd193e"),
            new ModelFile("medium-decoder.int8.onnx", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-medium/resolve/main/medium-decoder.int8.onnx", 571059257L, "595d00a338a365a7bfa0ca7f296cabc639583bef770ab6130df90f49a6412747"),
            new ModelFile("medium-tokens.txt", "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-medium/resolve/main/medium-tokens.txt", 816730L, "b34b360dbb493e781e479794586d661700670d65564001f23024971d1f2fa126")),
    QWEN3_ASR(
            "qwen3-asr",
            6144,
            Engine.OFFLINE_QWEN3,
            true,
            new ModelFile("conv_frontend.onnx", "https://huggingface.co/pantinor/sherpa-onnx-qwen3-asr-0.6b-int8/resolve/main/conv_frontend.onnx", 44148281L, "d22dc4423e0940e49884e903d2ea2f7e5567c14fc1aed97e4e26d6b8f208ef9e"),
            new ModelFile("encoder.int8.onnx", "https://huggingface.co/pantinor/sherpa-onnx-qwen3-asr-0.6b-int8/resolve/main/encoder.int8.onnx", 182491662L, "60748d3e6744a57c9c91e1b17424a6c2990567e8adceb0783940c03ed98fa9d9"),
            new ModelFile("decoder.int8.onnx", "https://huggingface.co/pantinor/sherpa-onnx-qwen3-asr-0.6b-int8/resolve/main/decoder.int8.onnx", 756563239L, "61e5f8249f9e7c82d5e01e1938c79fb3f5b3135f91664928033029e42451bd18"),
            new ModelFile("tokenizer/merges.txt", "https://huggingface.co/pantinor/sherpa-onnx-qwen3-asr-0.6b-int8/resolve/main/tokenizer/merges.txt", 1671853L),
            new ModelFile("tokenizer/tokenizer_config.json", "https://huggingface.co/pantinor/sherpa-onnx-qwen3-asr-0.6b-int8/resolve/main/tokenizer/tokenizer_config.json", 12487L),
            new ModelFile("tokenizer/vocab.json", "https://huggingface.co/pantinor/sherpa-onnx-qwen3-asr-0.6b-int8/resolve/main/tokenizer/vocab.json", 2776833L));

    public static final ModelFile SILERO_VAD = new ModelFile(
            "silero_vad.onnx",
            "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/silero_vad.onnx",
            643854L, "9e2449e1087496d8d4caba907f23e0bd3f78d91fa552479bb9c23ac09cbb1fd6");

    public enum Engine {
        ONLINE_TRANSDUCER,
        ONLINE_ZIPFORMER_CTC,
        OFFLINE_WENET_CTC,
        OFFLINE_MOONSHINE,
        OFFLINE_SENSE_VOICE,
        OFFLINE_WHISPER,
        OFFLINE_QWEN3
    }

    public final String folder;
    public final int minRamMb;
    public final Engine engine;
    public final boolean needsVad;
    public final ModelFile[] files;

    AsrModel(String folder, int minRamMb, Engine engine, boolean needsVad, ModelFile... files) {
        this.folder = folder;
        this.minRamMb = minRamMb;
        this.engine = engine;
        this.needsVad = needsVad;
        this.files = files;
    }

    public boolean isStreaming() {
        return engine == Engine.ONLINE_TRANSDUCER || engine == Engine.ONLINE_ZIPFORMER_CTC;
    }

    public long downloadBytes() {
        long value = 0;
        for (ModelFile file : files) value += file.size;
        return value;
    }
}
