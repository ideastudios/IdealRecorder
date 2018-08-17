package tech.oom.idealrecorder.utils;

import android.media.AudioFormat;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import tech.oom.idealrecorder.IdealRecorder;

import static android.content.ContentValues.TAG;

/**
 * pcm转wav工具类
 */
public class PcmToWavUtil {

    /**
     * 根据采样率 通道数 采样位数 获取文件头
     *
     * @param sampleRate           采样率，如44100
     * @param channels             通道数，如立体声为2
     * @param bitsPerSample        采样精度，即每个采样所占数据位数，如16，表示每个采样16bit数据，即2个字节
     * @param fileLenIncludeHeader wav文件总数据大小，包括44字节wave文件头大小
     * @return wavHeader
     */
    public static byte[] getWaveFileHeader(int sampleRate, int channels, int bitsPerSample, long fileLenIncludeHeader) {
        byte[] wavHeader = new byte[44];
        long totalDataLen = fileLenIncludeHeader - 8;
        long audioDataLen = totalDataLen - 36;

        //ckid：4字节 RIFF 标志，大写
        wavHeader[0] = 'R';
        wavHeader[1] = 'I';
        wavHeader[2] = 'F';
        wavHeader[3] = 'F';

        //cksize：4字节文件长度，这个长度不包括"RIFF"标志(4字节)和文件长度本身所占字节(4字节),即该长度等于整个文件长度 - 8
        wavHeader[4] = (byte) (totalDataLen & 0xff);
        wavHeader[5] = (byte) ((totalDataLen >> 8) & 0xff);
        wavHeader[6] = (byte) ((totalDataLen >> 16) & 0xff);
        wavHeader[7] = (byte) ((totalDataLen >> 24) & 0xff);

        //fcc type：4字节 "WAVE" 类型块标识, 大写
        wavHeader[8] = 'W';
        wavHeader[9] = 'A';
        wavHeader[10] = 'V';
        wavHeader[11] = 'E';

        //ckid：4字节 表示"fmt" chunk的开始,此块中包括文件内部格式信息，小写, 最后一个字符是空格
        wavHeader[12] = 'f';
        wavHeader[13] = 'm';
        wavHeader[14] = 't';
        wavHeader[15] = ' ';

        //cksize：4字节，文件内部格式信息数据的大小，过滤字节（一般为00000010H）
        wavHeader[16] = 0x10;
        wavHeader[17] = 0;
        wavHeader[18] = 0;
        wavHeader[19] = 0;

        //FormatTag：2字节，音频数据的编码方式，1：表示是PCM 编码
        wavHeader[20] = 1;
        wavHeader[21] = 0;

        //Channels：2字节，声道数，单声道为1，双声道为2
        wavHeader[22] = (byte) channels;
        wavHeader[23] = 0;

        //SamplesPerSec：4字节，采样率，如44100
        wavHeader[24] = (byte) (sampleRate & 0xff);
        wavHeader[25] = (byte) ((sampleRate >> 8) & 0xff);
        wavHeader[26] = (byte) ((sampleRate >> 16) & 0xff);
        wavHeader[27] = (byte) ((sampleRate >> 24) & 0xff);

        //BytesPerSec：4字节，音频数据传送速率, 单位是字节。其值为采样率×每次采样大小。播放软件利用此值可以估计缓冲区的大小；
        int bytePerSecond = sampleRate * (bitsPerSample / 8) * channels;
        wavHeader[28] = (byte) (bytePerSecond & 0xff);
        wavHeader[29] = (byte) ((bytePerSecond >> 8) & 0xff);
        wavHeader[30] = (byte) ((bytePerSecond >> 16) & 0xff);
        wavHeader[31] = (byte) ((bytePerSecond >> 24) & 0xff);

        //BlockAlign：2字节，每次采样的大小 = 采样精度*声道数/8(单位是字节); 这也是字节对齐的最小单位, 譬如 16bit 立体声在这里的值是 4 字节。
        //播放软件需要一次处理多个该值大小的字节数据，以便将其值用于缓冲区的调整
        wavHeader[32] = (byte) (bitsPerSample * channels / 8);
        wavHeader[33] = 0;

        //BitsPerSample：2字节，每个声道的采样精度; 譬如 16bit 在这里的值就是16。如果有多个声道，则每个声道的采样精度大小都一样的；
        wavHeader[34] = (byte) bitsPerSample;
        wavHeader[35] = 0;

        //ckid：4字节，数据标志符（data），表示 "data" chunk的开始。此块中包含音频数据，小写；
        wavHeader[36] = 'd';
        wavHeader[37] = 'a';
        wavHeader[38] = 't';
        wavHeader[39] = 'a';

        //cksize：音频数据的长度，4字节，audioDataLen = totalDataLen - 36 = fileLenIncludeHeader - 44
        wavHeader[40] = (byte) (audioDataLen & 0xff);
        wavHeader[41] = (byte) ((audioDataLen >> 8) & 0xff);
        wavHeader[42] = (byte) ((audioDataLen >> 16) & 0xff);
        wavHeader[43] = (byte) ((audioDataLen >> 24) & 0xff);
        return wavHeader;
    }

    /**
     * pcm数据增加文件头
     *
     * @param headerArray 文件头数组
     * @param pcmArray    pcm数据数组
     * @return wav文件数据
     */
    public static byte[] addFileHeader(byte[] headerArray, byte[] pcmArray) {
        byte[] wavArray = new byte[headerArray.length + pcmArray.length];
        System.arraycopy(headerArray, 0, wavArray, 0, headerArray.length);
        System.arraycopy(pcmArray, 0, wavArray, headerArray.length, pcmArray.length);
        return wavArray;
    }

    /**
     * 根据采样率 通道数 采样位数 pcm数据获取wav文件数据
     *
     * @param sampleRate    采样率，如44100
     * @param channels      通道数，如立体声为2
     * @param bitsPerSample 采样精度，即每个采样所占数据位数，如16，表示每个采样16bit数据，即2个字节
     * @param pcmArray      pcm文件数据
     * @return wav文件数据
     */
    public static byte[] getWaveFile(int sampleRate, int channels, int bitsPerSample, byte[] pcmArray) {
        if (pcmArray == null) {
            return null;
        }
        return addFileHeader(getWaveFileHeader(sampleRate, channels, bitsPerSample, pcmArray.length + 44), pcmArray);
    }

    /**
     * 根据录音的配置信息和pcm数据，获取wav文件数据
     *
     * @param recordConfig 录音配置
     * @param pcmArray     pcm数据
     * @return wav数据
     */
    public static byte[] getWaveFile(IdealRecorder.RecordConfig recordConfig, byte[] pcmArray) {
        short nChannels;
        short bSamples;
        if (recordConfig.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
            bSamples = 16;
        } else {
            bSamples = 8;
        }

        if (recordConfig.getChannelConfig() == AudioFormat.CHANNEL_IN_MONO) {
            nChannels = 1;
        } else {
            nChannels = 2;
        }
        return getWaveFile(recordConfig.getSampleRate(), nChannels, bSamples, pcmArray);
    }

    /**
     * 将pcm文件转换成wav文件
     *
     * @param srcConfig  源PCM音频文件的配置信息
     * @param srcPath    源PCM文件的路径
     * @param targetPath 生成目标wav文件的路径
     */
    public static void transferPcmToWav(IdealRecorder.RecordConfig srcConfig, String srcPath, String targetPath) {

        if (TextUtils.isEmpty(srcPath)) {
            android.util.Log.e(TAG, "pcm src path not set ");
            return;
        }
        if (TextUtils.isEmpty(targetPath)) {
            android.util.Log.e(TAG, "target path not set");
            return;
        }
        if (srcConfig == null) {
            Log.e(TAG, "RecordConfig not set");
            return;
        }
        File targetFile = new File(targetPath);

        if (targetFile.exists()) {
            targetFile.delete();
        } else {
            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
        }
        RandomAccessFile randomAccessFile = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(srcPath);
            randomAccessFile = new RandomAccessFile(targetFile, "rw");
            randomAccessFile.setLength(0);
            byte[] waveFile = getWaveFile(srcConfig, new byte[0]);
            randomAccessFile.write(waveFile);
            byte[] aar = new byte[1024 * 3];
            int len = 0;
            while ((len = inputStream.read(aar)) != -1) {
                randomAccessFile.write(aar, 0, len);
            }
            inputStream.close();
            randomAccessFile.seek(4); // riff chunk size
            randomAccessFile.writeInt(Integer.reverseBytes((int) (randomAccessFile.length() - 8)));
            randomAccessFile.seek(40); // data chunk size
            randomAccessFile.writeInt(Integer.reverseBytes((int) (randomAccessFile.length() - 44)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                    randomAccessFile = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }


    /**
     * 根据pcm数据 获取示例wav文件数据 适用于8K采样率 单通道 16位的录音数据
     *
     * @param pcmArray
     * @return wav文件数据
     */
    public static byte[] getSampleWaveFile(byte[] pcmArray) {
        return getWaveFile(8000, 1, 16, pcmArray);
    }


}
