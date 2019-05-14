package tech.oom.idealrecorder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import tech.oom.idealrecorder.file.AudioFileHelper;
import tech.oom.idealrecorder.file.AudioFileListener;
import tech.oom.idealrecorder.record.Recorder;
import tech.oom.idealrecorder.record.RecorderCallback;
import tech.oom.idealrecorder.utils.BytesTransUtil;
import tech.oom.idealrecorder.utils.Log;

/**
 * 录音功能的核心类
 */

public class IdealRecorder implements RecorderCallback, AudioFileListener {
    private static final String TAG = "IdealRecorder";
    private Context context;
    private Handler idealHandler;
    private RecordConfig config;

    private AudioFileHelper audioFileHelper;
    private boolean isAudioFileHelperInit;
    private Recorder recorder;
    private StatusListener statusListener;
    private long maxRecordTime = 6000L;
    private long volumeInterval = 200L;
    private int count;
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private AtomicBoolean isStarted = new AtomicBoolean(false);

    private IdealRecorder() {
        idealHandler = new Handler();
        recorder = new Recorder(config, this);
        audioFileHelper = new AudioFileHelper(this);
    }

    public static IdealRecorder getInstance() {
        return IdealRecorderHolder.instance;
    }

    /**
     * 初始化当前实例
     *
     * @param mContext 当前应用的application context
     */
    public void init(Context mContext) {
        context = mContext;

    }

    /**
     * 获取当前应用的context
     *
     * @return 当前应用的context
     */
    public Context getContext() {
        if (context == null)
            throw new IllegalStateException("请先在Application或Activity中调用 IdealRecorder.getInstance.init() 初始化！");
        return context;
    }

    public IdealRecorder setRecordConfig(RecordConfig config) {
        this.config = config;
        audioFileHelper.setRecorderConfig(config);
        recorder.setRecordConfig(config);
        return this;
    }

    /**
     * 设置最长语音
     *
     * @param maxRecordTimeMillis 最长录音时间 单位 毫秒
     * @return
     */
    public IdealRecorder setMaxRecordTime(long maxRecordTimeMillis) {
        this.maxRecordTime = maxRecordTimeMillis;
        return this;
    }


    /**
     * 设置音量回调时长 单位毫秒 必须为100毫秒的整数倍
     *
     * @param intervalMillis 音量回调间隔时长
     * @return
     */
    public IdealRecorder setVolumeInterval(long intervalMillis) {
        if (intervalMillis < 100) {
            Log.e(TAG, "Volume interval should at least 100 Millisecond .Current set will not take effect, default interval is 200ms");
            return this;
        }
        if (intervalMillis % Recorder.TIMER_INTERVAL != 0) {
            intervalMillis = intervalMillis / Recorder.TIMER_INTERVAL * Recorder.TIMER_INTERVAL;
            Log.e(TAG, "Current interval is changed to " + intervalMillis);
        }
        this.volumeInterval = intervalMillis;
        return this;
    }


    /**
     * 设置录音保存路径 保存格式为wav
     *
     * @param path 文件保存绝对路径
     */
    public IdealRecorder setRecordFilePath(String path) {
        if (!TextUtils.isEmpty(path) && audioFileHelper != null) {
            if (path.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath()) && !isWriteExternalStoragePermissionGranted()) {
                Log.e(TAG, "set recorder file path failed,because no WRITE_EXTERNAL_STORAGE permission was granted");

                return this;
            }
            isAudioFileHelperInit = true;
            audioFileHelper.setSavePath(path);
        } else {
            isAudioFileHelperInit = false;
            audioFileHelper.setSavePath(null);
        }
        return this;
    }

    /**
     * 设置录音保存的格式是否为wav 默认保存为wav格式 true 保存为wav格式 false 文件保存问pcm格式
     *
     * @param isWav 是否为wav格式 默认为true 保存为wav格式 ;false 文件保存问pcm格式
     * @return
     */
    public IdealRecorder setWavFormat(boolean isWav) {
        audioFileHelper.setWav(isWav);
        return this;
    }

    /**
     * 设置录音时各种状态的监听
     *
     * @param statusListener statusListener
     * @return
     */
    public IdealRecorder setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
        return this;
    }

    /**
     * 判断是否有录音权限
     *
     * @return
     */
    public boolean isRecordAudioPermissionGranted() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 判断是否有读写存储权限
     *
     * @return
     */
    public boolean isWriteExternalStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 开始录音
     *
     * @return
     */
    public boolean start() {
        if (isStarted.compareAndSet(false, true)) {
            recorder.start();
            Log.d(TAG, "Ideal Recorder Started");
            return true;
        } else {
            Log.e(TAG, "Start failed , Because the Ideal Recorder already started");
            return false;
        }
    }

    /**
     * 停止录音
     */
    public void stop() {
        Log.d(TAG, "Stop Ideal Recorder is called");
        if (this.isStarted.get()) {
            this.isStarted.set(false);
            this.recorder.immediateStop();

        } else if (this.recorder != null) {
            this.recorder.immediateStop();
        }
    }

    /**
     * 在UI线程执行
     *
     * @param runnable 需要执行的runnable
     */
    private void runOnUi(Runnable runnable) {
        idealHandler.post(runnable);
    }


    @Override
    public boolean onRecorderReady() {
        if (!isRecordAudioPermissionGranted()) {
            Log.e(TAG, "set recorder failed,because no RECORD_AUDIO permission was granted");
            onRecordedFail(IdealConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR);
        }
        return isRecordAudioPermissionGranted();
    }

    @Override
    public boolean onRecorderStart() {
        if (isAudioFileHelperInit) {
            audioFileHelper.start();
        }
        count = 0;
        byteArrayOutputStream.reset();
        runOnUi(new Runnable() {
            public void run() {
                if (statusListener != null) {
                    statusListener.onStartRecording();
                }
                Log.d(TAG, "onRecorderStart");
            }
        });
        return true;
    }

    @Override
    public void onRecorded(final short[] wave) {
        count++;
        final byte[] bytes = BytesTransUtil.getInstance().Shorts2Bytes(wave);
        if (isAudioFileHelperInit) {

            audioFileHelper.save(bytes, 0, bytes.length);
        }
        byteArrayOutputStream.write(bytes, 0, bytes.length);
        if (statusListener != null) {
            statusListener.onRecordDataOnWorkerThread(wave, wave == null ? 0 : wave.length);
        }
        runOnUi(new Runnable() {
            @Override
            public void run() {
                if (statusListener != null) {
                    statusListener.onRecordData(wave, wave == null ? 0 : wave.length);
                }
            }
        });

        long recordedTime = count * Recorder.TIMER_INTERVAL;
        if (recordedTime >= volumeInterval && recordedTime % volumeInterval == 0) {
            onRecorderVolume(calculateVolume(wave));
        }
        if (recordedTime >= maxRecordTime) {
            recorder.stop();
            isStarted.set(false);
        }

    }

    private void onRecorderVolume(final int volume) {

        runOnUi(new Runnable() {
            public void run() {
                if (statusListener != null) {
                    statusListener.onVoiceVolume(volume);
                }
            }
        });


    }

    @Override
    public void onRecordedFail(final int paramInt) {
        if (isAudioFileHelperInit) {

            audioFileHelper.cancel();
        }
        runOnUi(new Runnable() {
            public void run() {
                String errorMsg = "";
                switch (paramInt) {
                    case IdealConst.RecorderErrorCode.RECORDER_EXCEPTION_OCCUR:
                        errorMsg = "启动或录音时抛出异常Exception";
                        break;
                    case IdealConst.RecorderErrorCode.RECORDER_READ_ERROR:
                        errorMsg = "Recorder.read() 过程中发生错误";
                        break;
                    case IdealConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR:
                        errorMsg = "当前应用没有录音权限或者录音功能被占用";
                        break;
                    default:
                        errorMsg = "未知错误";
                }
                if (statusListener != null) {
                    statusListener.onRecordError(paramInt, errorMsg);
                }
            }
        });
    }

    @Override
    public void onRecorderStop() {
        if (isAudioFileHelperInit) {
            audioFileHelper.finish();
        }
        runOnUi(new Runnable() {
            @Override
            public void run() {
                if (statusListener != null) {
                    statusListener.onRecordedAllData(byteArrayOutputStream.toByteArray());
                    statusListener.onStopRecording();
                }
            }
        });
//        byteArrayOutputStream.reset();
    }

    private int calculateVolume(short[] wave) {
        long v = 0;
        // 将 buffer 内容取出，进行平方和运算
        for (int i = 0; i < wave.length; i++) {
            v += wave[i] * wave[i];
        }
        // 平方和除以数据总长度，得到音量大小。
        double mean = v / (double) wave.length;
        double volume = 10 * Math.log10(mean);
        return (int) volume;
    }


    /**
     * 保存文件失败
     */
    @Override
    public void onFailure(final String reason) {

        Log.d(TAG, "save record file failure, this reason is " + reason);

        runOnUi(new Runnable() {
            public void run() {
                if (statusListener != null) {
                    statusListener.onFileSaveFailed(reason);
                }
            }
        });
    }

    /**
     * 保存文件成功
     */
    @Override
    public void onSuccess(final String savePath) {
        Log.d(TAG, "save record file success, the file path is" + savePath);
        runOnUi(new Runnable() {
            public void run() {
                if (statusListener != null) {
                    statusListener.onFileSaveSuccess(savePath);
                }
            }
        });

    }

    /**
     * 录音的配置信息  默认配置为16K采样率 单通道 16位
     * <pre>
     *      audioSource = MediaRecorder.AudioSource.MIC;
     *      sampleRate = SAMPLE_RATE_16K_HZ;
     *      channelConfig = AudioFormat.CHANNEL_IN_MONO;
     *      audioFormat = AudioFormat.ENCODING_PCM_16BIT;
     * </pre>
     */
    public static class RecordConfig {
        public static final int SAMPLE_RATE_44K_HZ = 44100;
        public static final int SAMPLE_RATE_22K_HZ = 22050;
        public static final int SAMPLE_RATE_16K_HZ = 16000;
        public static final int SAMPLE_RATE_11K_HZ = 11025;
        public static final int SAMPLE_RATE_8K_HZ = 8000;
        private int audioSource = MediaRecorder.AudioSource.MIC;
        private int sampleRate = SAMPLE_RATE_16K_HZ;
        private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        /**
         * 录音配置的构造方法
         *
         * @param audioSource   the recording source.
         *                      See {@link MediaRecorder.AudioSource} for the recording source definitions.
         *                      recommend {@link MediaRecorder.AudioSource#MIC}
         * @param sampleRate    the sample rate expressed in Hertz. {@link RecordConfig#SAMPLE_RATE_44K_HZ} is Recommended ,
         *                      {@link RecordConfig#SAMPLE_RATE_22K_HZ},{@link RecordConfig#SAMPLE_RATE_16K_HZ},
         *                      {@link RecordConfig#SAMPLE_RATE_11K_HZ},{@link RecordConfig#SAMPLE_RATE_8K_HZ}
         * @param channelConfig describes the configuration of the audio channels.
         *                      See {@link AudioFormat#CHANNEL_IN_MONO} and
         *                      {@link AudioFormat#CHANNEL_IN_STEREO}.  {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed
         *                      to work on all devices.
         * @param audioFormat   the format in which the audio data is to be returned.
         *                      See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
         *                      and {@link AudioFormat#ENCODING_PCM_FLOAT}.
         */
        public RecordConfig(int audioSource, int sampleRate, int channelConfig, int audioFormat) {
            this.audioSource = audioSource;
            this.sampleRate = sampleRate;
            this.channelConfig = channelConfig;
            this.audioFormat = audioFormat;
        }

        /**
         * 录音配置的构造方法
         */
        public RecordConfig() {

        }

        public int getAudioSource() {
            return audioSource;
        }

        /**
         * @param audioSource the recording source.
         *                    See {@link MediaRecorder.AudioSource} for the recording source definitions.
         *                    recommend {@link MediaRecorder.AudioSource#MIC}
         */
        public RecordConfig setAudioSource(int audioSource) {
            this.audioSource = audioSource;
            return this;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        /**
         * @param sampleRate the sample rate expressed in Hertz. {@link RecordConfig#SAMPLE_RATE_44K_HZ} is Recommended ,
         * @link RecordConfig#SAMPLE_RATE_22K_HZ},{@link RecordConfig#SAMPLE_RATE_16K_HZ},{@link RecordConfig#SAMPLE_RATE_11K_HZ},{@link RecordConfig#SAMPLE_RATE_8K_HZ}
         * which is usually the sample rate of the source.
         */
        public RecordConfig setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public int getChannelConfig() {
            return channelConfig;
        }

        /**
         * @param channelConfig describes the configuration of the audio channels.
         *                      See {@link AudioFormat#CHANNEL_IN_MONO} and
         *                      {@link AudioFormat#CHANNEL_IN_STEREO}.  {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed
         *                      to work on all devices.
         */
        public RecordConfig setChannelConfig(int channelConfig) {
            this.channelConfig = channelConfig;
            return this;
        }

        public int getAudioFormat() {
            return audioFormat;
        }

        /**
         * @param audioFormat the format in which the audio data is to be returned.
         *                    See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
         *                    and {@link AudioFormat#ENCODING_PCM_FLOAT}.
         */
        public RecordConfig setAudioFormat(int audioFormat) {
            this.audioFormat = audioFormat;
            return this;
        }


    }


    /**
     * idealRecorder的holder 用来初始化
     */
    private static class IdealRecorderHolder {
        private final static IdealRecorder instance = new IdealRecorder();
    }
}
