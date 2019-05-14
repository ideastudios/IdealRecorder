package tech.oom.idealrecorder.record;

import android.media.AudioFormat;
import android.media.AudioRecord;

import tech.oom.idealrecorder.IdealConst;
import tech.oom.idealrecorder.IdealRecorder;
import tech.oom.idealrecorder.utils.Log;


public class Recorder {
    public static final int TIMER_INTERVAL = 20;
    private static final String TAG = "Recorder";
    private IdealRecorder.RecordConfig recordConfig;
    private AudioRecord mAudioRecorder = null;
    private RecorderCallback mCallback;
    private int bufferSize;
    private boolean isRecord = false;
    private Thread mThread = null;
    private short[] wave;
    private Runnable RecordRun = new Runnable() {

        public void run() {
            if ((mAudioRecorder != null) && (mAudioRecorder.getState() == 1)) {

                try {
                    mAudioRecorder.stop();
                    mAudioRecorder.startRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                    recordFailed(IdealConst.RecorderErrorCode.RECORDER_EXCEPTION_OCCUR);
                    mAudioRecorder = null;
                }
            }
            if ((mAudioRecorder != null) &&
                    (mAudioRecorder.getState() == 1) && (mAudioRecorder.getRecordingState() == 1)) {
                Log.e(TAG, "no recorder permission or recorder is not available right now");
                recordFailed(IdealConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR);
                mAudioRecorder = null;
            }
            for (int i = 0; i < 2; i++) {
                if (mAudioRecorder == null) {
                    isRecord = false;
                    break;
                }
                mAudioRecorder.read(wave, 0, wave.length);
            }
            while (isRecord) {
                int nLen = 0;
                try {
                    nLen = mAudioRecorder.read(wave, 0, wave.length);
                } catch (Exception e) {
                    isRecord = false;
                    recordFailed(IdealConst.RecorderErrorCode.RECORDER_EXCEPTION_OCCUR);
                }
                if (nLen == wave.length) {
                    mCallback.onRecorded(wave);
                } else {
                    recordFailed(IdealConst.RecorderErrorCode.RECORDER_READ_ERROR);
                    isRecord = false;
                }
            }
            Log.i(TAG, "out of the reading while loop,i'm going to stop");
            unInitializeRecord();
            doRecordStop();
        }
    };


    public Recorder(IdealRecorder.RecordConfig config, RecorderCallback callback) {
        this.mCallback = callback;
        this.recordConfig = config;
    }

    public void setRecordConfig(IdealRecorder.RecordConfig config) {
        this.recordConfig = config;
    }


    public boolean start() {
        isRecord = true;
        synchronized (this) {
            if (doRecordReady()) {
                Log.d(TAG, "doRecordReady");
                if (initializeRecord()) {
                    Log.d(TAG, "initializeRecord");
                    if (doRecordStart()) {
                        Log.d(TAG, "doRecordStart");

                        mThread = new Thread(RecordRun);
                        mThread.start();
                        return true;
                    }
                }
            }
        }
        isRecord = false;
        return false;
    }


    public void stop() {
        synchronized (this) {
            mThread = null;
            isRecord = false;
        }
    }

    public void immediateStop() {
        isRecord = false;
        if (mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mThread = null;
    }

    public boolean isStarted() {
        return isRecord;
    }

    private boolean initializeRecord() {
        synchronized (this) {
            try {
                if (mCallback == null) {
                    Log.e(TAG, "Error VoiceRecorderCallback is  null");
                    return false;
                }
                if (recordConfig == null) {
                    Log.e(TAG, "Error recordConfig is null");
                    return false;
                }
                short nChannels;
                int sampleRate;
                short bSamples;
                int audioSource;
                int audioFormat;
                int channelConfig;
                if (recordConfig.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
                    bSamples = 16;
                } else {
                    bSamples = 8;
                }

                if ((channelConfig = recordConfig.getChannelConfig()) == AudioFormat.CHANNEL_IN_MONO) {
                    nChannels = 1;
                } else {
                    nChannels = 2;
                }
                audioSource = recordConfig.getAudioSource();
                sampleRate = recordConfig.getSampleRate();
                audioFormat = recordConfig.getAudioFormat();
                int framePeriod = sampleRate * TIMER_INTERVAL / 1000;
                bufferSize = framePeriod * 2 * bSamples * nChannels / 8;

                wave = new short[framePeriod * bSamples / 8 * nChannels / 2];
                Log.d(TAG, "buffersize = " + bufferSize);
                int nMinSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                if (bufferSize < nMinSize) {
                    bufferSize = nMinSize;

                    Log.d(TAG, "Increasing buffer size to " + Integer.toString(bufferSize));
                }
                if (mAudioRecorder != null) {
                    unInitializeRecord();
                }
                mAudioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
                if (mAudioRecorder.getState() != 1) {
                    mAudioRecorder = null;
                    recordFailed(IdealConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR);
                    Log.e(TAG, "AudioRecord initialization failed,because of no RECORD permission or unavailable AudioRecord ");
                    throw new Exception("AudioRecord initialization failed");
                }
                Log.i(TAG, "initialize  Record");
                return true;
            } catch (Throwable e) {
                if (e.getMessage() != null) {
                    Log.e(TAG, getClass().getName() + e.getMessage());
                } else {
                    Log.e(TAG, getClass().getName() + "Unknown error occured while initializing recording");
                }
                return false;
            }
        }
    }

    private void unInitializeRecord() {
        Log.i(TAG, "unInitializeRecord");
        synchronized (this) {
            if (mAudioRecorder != null) {
                try {
                    mAudioRecorder.stop();
                    mAudioRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "mAudioRecorder release error!");
                }
                mAudioRecorder = null;
            }
        }
    }

    private boolean doRecordStart() {
        if (mCallback != null) {
            return mCallback.onRecorderStart();
        }
        return true;
    }

    private boolean doRecordReady() {
        if (mCallback != null) {
            return mCallback.onRecorderReady();
        }
        return true;
    }

    private void doRecordStop() {
        if (mCallback != null) {
            mCallback.onRecorderStop();
        }
    }

    private void recordFailed(int errorCode) {
        if (mCallback != null) {
            mCallback.onRecordedFail(errorCode);
        }
    }
}
