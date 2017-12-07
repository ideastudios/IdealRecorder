package tech.oom.idealrecorder.record;

public abstract interface RecorderCallback {
    /**
     * 录音开始
     */
    public abstract boolean onRecorderStart();

    /**
     * 录音是否就绪
     */
    public abstract boolean onRecorderReady();

    /**
     * 录音停止
     */
    public abstract void onRecorderStop();

    /**
     * 正在录音
     *
     * @param wave 录制的数据data
     */
    public abstract void onRecorded(short[] wave);


    /**
     * 录制失败
     *
     * @param paramInt 失败的code
     */
    public abstract void onRecordedFail(int paramInt);
}
