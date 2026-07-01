package com.fongmi.android.tv.player.mpv;

import androidx.media3.common.PlaybackException;

public class MpvErrorMsgProvider {

    public String get(PlaybackException e) {
        // If the exception message is detailed, use it directly
        String message = e.getMessage();
        if (message != null && message.startsWith("MPV播放失败")) {
            return message;
        }

        // Otherwise fallback to error code mapping
        return switch (e.errorCode) {
            case PlaybackException.ERROR_CODE_BAD_VALUE -> "MPV 无效参数";
            case PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK -> "MPV 运行时错误";
            case PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> "MPV IO错误";
            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "MPV 网络连接失败";
            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "MPV 网络连接超时";
            case PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE -> "MPV 无效的内容类型";
            case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "MPV HTTP错误";
            case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "MPV 文件未找到";
            case PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> "MPV 无权限访问";
            case PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED -> "MPV 不允许明文传输";
            case PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE -> "MPV 读取位置超出范围";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> "MPV 容器格式损坏";
            case PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> "MPV 清单格式损坏";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "MPV 不支持的容器格式";
            case PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED -> "MPV 不支持的清单格式";
            case PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED -> "MPV 音频轨道初始化失败";
            case PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSOR_INIT_FAILED -> "MPV 视频处理器初始化失败";
            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "MPV 解码器初始化失败";
            case PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED -> "MPV 解码器查询失败";
            case PlaybackException.ERROR_CODE_DECODING_FAILED -> "MPV 解码失败";
            case PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES -> "MPV 格式超出能力";
            case PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> "MPV 不支持的解码格式";
            default -> message != null && !message.isEmpty() ? message : "MPV 播放错误";
        };
    }
}
