package tech.oom.idealrecorder;

/**
 * 录音各种状态的回调类
 */

public class StatusListener {


    /**
     * 开始录音的回调
     */
    public void onStartRecording() {
    }

    /**
     * 录音时的buffer
     *
     * @param data   PCM Data
     * @param length 长度
     */
    public void onRecordData(short[] data, int length) {
    }

    /**
     * 录音时的buffer回调 在工作线程中
     *
     * @param data   PCM Data
     * @param length 长度
     */
    public void onRecordDataOnWorkerThread(short[] data, int length) {

    }


    /**
     * 录音时的音量
     *
     * @param volume 音量
     */
    public void onVoiceVolume(int volume) {
    }


    /**
     * 录音失败
     *
     * @param code     错误码  {@link IdealConst.RecorderErrorCode}
     * @param errorMsg 错误信息描述
     */
    public void onRecordError(int code, String errorMsg) {

    }


    /**
     * 保存文件失败
     *
     * @param error
     */
    public void onFileSaveFailed(String error) {

    }

    /**
     * 保存录音文件成功
     *
     * @param fileUri 保存文件的路径
     */
    public void onFileSaveSuccess(String fileUri) {

    }


    /**
     * 停止录音的回调
     */
    public void onStopRecording() {
    }

    /**
     * 全部的录音pcm数据
     *
     * @param arr 所有的录音pcm数据
     */
    public void onRecordedAllData(byte[] arr) {

    }

}
