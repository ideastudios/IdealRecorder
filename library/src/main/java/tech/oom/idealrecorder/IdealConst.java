package tech.oom.idealrecorder;

/**
 * SDK的相关常量
 */

public class IdealConst {


    /**
     * 录音错误时的返回码
     */
    public static final class RecorderErrorCode {
        /**
         * 启动或录音时抛出异常
         */
        public static final int RECORDER_EXCEPTION_OCCUR = 0;

        /**
         * Recorder.read 过程中发生错误
         */
        public static final int RECORDER_READ_ERROR = 1;

        /**
         * 当前录音没有权限或者录音功能被占用
         */
        public static final int RECORDER_PERMISSION_ERROR = 3;
    }
}
