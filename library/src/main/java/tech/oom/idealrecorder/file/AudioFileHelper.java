package tech.oom.idealrecorder.file;

import android.media.AudioFormat;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import tech.oom.idealrecorder.IdealRecorder;
import tech.oom.idealrecorder.utils.Log;


/**
 * 语音文件帮助类 用户保存相关文件
 */

public class AudioFileHelper {

    public static final String TAG = "AudioFileHelper";
    private AudioFileListener listener;
    private String savePath;
    private RandomAccessFile randomAccessFile;
    private File targetFile;
    private IdealRecorder.RecordConfig config;
    private boolean isWav = true;

    public AudioFileHelper(AudioFileListener listener) {
        this.listener = listener;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public void setRecorderConfig(IdealRecorder.RecordConfig config) {
        this.config = config;
    }

    public void setWav(boolean wav) {
        this.isWav = wav;
    }

    public void start() {
        try {

            open(savePath);

        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.toString());
            }
        }
    }

    public void save(byte[] data, int offset, int size) {
        if (randomAccessFile == null) {
            return;
        }
        try {
            write(randomAccessFile, data, offset, size);
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.toString());
            }

        }
    }

    public void finish() {
        try {
            close();

        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.toString());
            }

        }
    }


    private void open(String path) throws IOException {
        if (TextUtils.isEmpty(path)) {
            Log.d(TAG, "Path not set , data will not save");
            return;
        }
        if (this.config == null) {
            Log.d(TAG, "RecordConfig not set , data will not save");
            return;
        }
        targetFile = new File(path);

        if (targetFile.exists()) {
            targetFile.delete();
        } else {
            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
        }
        short bSamples;
        short nChannels;
        int sRate;
        if (config.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
            bSamples = 16;
        } else {
            bSamples = 8;
        }

        if (config.getChannelConfig() == AudioFormat.CHANNEL_IN_MONO) {
            nChannels = 1;
        } else {
            nChannels = 2;
        }
        sRate = config.getSampleRate();
        randomAccessFile = new RandomAccessFile(targetFile, "rw");
        randomAccessFile.setLength(0);
        if (isWav) {
            // Set file length to
            // 0, to prevent unexpected behavior in case the file already existed
            // 16K、16bit、单声道
            /* RIFF header */
            randomAccessFile.writeBytes("RIFF"); // riff id
            randomAccessFile.writeInt(0); // riff chunk size *PLACEHOLDER*
            randomAccessFile.writeBytes("WAVE"); // wave type

            /* fmt chunk */
            randomAccessFile.writeBytes("fmt "); // fmt id
            randomAccessFile.writeInt(Integer.reverseBytes(16)); // fmt chunk size
            randomAccessFile.writeShort(Short.reverseBytes((short) 1)); // AudioFormat,1 for PCM
            randomAccessFile.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
            randomAccessFile.writeInt(Integer.reverseBytes(sRate)); // Sample rate
            randomAccessFile.writeInt(Integer.reverseBytes(sRate * bSamples * nChannels / 8)); // Byte rate,SampleRate*NumberOfChannels*BitsPerSample/8
            randomAccessFile.writeShort(Short.reverseBytes((short) (nChannels * bSamples / 8))); // Block align, NumberOfChannels*BitsPerSample/8
            randomAccessFile.writeShort(Short.reverseBytes(bSamples)); // Bits per sample

            /* data chunk */
            randomAccessFile.writeBytes("data"); // data id
            randomAccessFile.writeInt(0); // data chunk size *PLACEHOLDER*
        }


        Log.d(TAG, "saved file path: " + path);

    }

    private void write(RandomAccessFile file, byte[] data, int offset, int size) throws IOException {
        file.write(data, offset, size);
//        Log.d(TAG, "fwrite: " + size);
    }

    private void close() throws IOException {
        try {
            if (randomAccessFile == null) {
                if (listener != null) {
                    listener.onFailure("File save error exception occurs");
                }
                return;
            }
            if (isWav) {
                randomAccessFile.seek(4); // riff chunk size
                randomAccessFile.writeInt(Integer.reverseBytes((int) (randomAccessFile.length() - 8)));
                randomAccessFile.seek(40); // data chunk size
                randomAccessFile.writeInt(Integer.reverseBytes((int) (randomAccessFile.length() - 44)));
            }

            Log.d(TAG, "file size: " + randomAccessFile.length());
            if (listener != null) {
                listener.onSuccess(savePath);
            }

        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
                randomAccessFile = null;
            }

        }
    }

    public void cancel() {
        if (randomAccessFile == null) {
            return;
        }
        if (targetFile == null) {
            return;
        }
        if (targetFile.exists()) {
            targetFile.delete();
        }
        randomAccessFile = null;
        targetFile = null;

    }


}
