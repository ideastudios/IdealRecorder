package tech.oom.idealrecorder.utils;

import java.nio.ByteOrder;

public class BytesTransUtil {
    private static BytesTransUtil instance = null;
    private String TAG = "BytesTransUtil";

    public static BytesTransUtil getInstance() {
        if (instance == null) {
            instance = new BytesTransUtil();
        }
        return instance;
    }

    public boolean testCPU() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return true;
        }
        return false;
    }

    public byte[] getBytes(short s, boolean bBigEnding) {
        byte[] buf = new byte[2];
        if (bBigEnding) {
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = ((byte) (s & 0xFF));
                s = (short) (s >> 8);
            }
        } else {
            for (int i = 0; i < buf.length; i++) {
                buf[i] = ((byte) (s & 0xFF));
                s = (short) (s >> 8);
            }
        }
        return buf;
    }

    public byte[] getBytes(int s, boolean bBigEnding) {
        byte[] buf = new byte[4];
        if (bBigEnding) {
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = ((byte) (s & 0xFF));
                s >>= 8;
            }
        } else {
            for (int i = 0; i < buf.length; i++) {
                buf[i] = ((byte) (s & 0xFF));
                s >>= 8;
            }
        }
        return buf;
    }

    public byte[] getBytes(long s, boolean bBigEnding) {
        byte[] buf = new byte[8];
        if (bBigEnding) {
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = ((byte) (int) (s & 0xFF));
                s >>= 8;
            }
        } else {
            for (int i = 0; i < buf.length; i++) {
                buf[i] = ((byte) (int) (s & 0xFF));
                s >>= 8;
            }
        }
        return buf;
    }

    public short getShort(byte[] buf, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 2) {
            throw new IllegalArgumentException("byte array size > 2 !");
        }
        short r = 0;
        if (bBigEnding) {
            for (int i = 0; i < buf.length; i++) {
                r = (short) (r << 8);
                r = (short) (r | buf[i] & 0xFF);
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r = (short) (r << 8);
                r = (short) (r | buf[i] & 0xFF);
            }
        }
        return r;
    }

    public int getInt(byte[] buf, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        int r = 0;
        if (bBigEnding) {
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= buf[i] & 0xFF;
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= buf[i] & 0xFF;
            }
        }
        return r;
    }

    public long getLong(byte[] buf, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 8) {
            throw new IllegalArgumentException("byte array size > 8 !");
        }
        long r = 0L;
        if (bBigEnding) {
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= buf[i] & 0xFF;
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= buf[i] & 0xFF;
            }
        }
        return r;
    }

    public byte[] getBytes(int i) {
        return getBytes(i, testCPU());
    }

    public byte[] getBytes(short s) {
        return getBytes(s, testCPU());
    }

    public byte[] getBytes(long l) {
        return getBytes(l, testCPU());
    }

    public int getInt(byte[] buf) {
        return getInt(buf, testCPU());
    }

    public short getShort(byte[] buf) {
        return getShort(buf, testCPU());
    }

    public long getLong(byte[] buf) {
        return getLong(buf, testCPU());
    }

    public short[] Bytes2Shorts(byte[] buf) {
        byte bLength = 2;
        short[] s = new short[buf.length / bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = new byte[bLength];
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                temp[jLoop] = buf[(iLoop * bLength + jLoop)];
            }
            s[iLoop] = getShort(temp);
        }
        return s;
    }

    public byte[] Shorts2Bytes(short[] s) {
        byte bLength = 2;
        byte[] buf = new byte[s.length * bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = getBytes(s[iLoop]);
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                buf[(iLoop * bLength + jLoop)] = temp[jLoop];
            }
        }
        return buf;
    }

    public int[] Bytes2Ints(byte[] buf) {
        byte bLength = 4;
        int[] s = new int[buf.length / bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = new byte[bLength];
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                temp[jLoop] = buf[(iLoop * bLength + jLoop)];
            }
            s[iLoop] = getInt(temp);
        }
        return s;
    }

    public byte[] Ints2Bytes(int[] s) {
        byte bLength = 4;
        byte[] buf = new byte[s.length * bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = getBytes(s[iLoop]);
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                buf[(iLoop * bLength + jLoop)] = temp[jLoop];
            }
        }
        return buf;
    }

    public long[] Bytes2Longs(byte[] buf) {
        byte bLength = 8;
        long[] s = new long[buf.length / bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = new byte[bLength];
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                temp[jLoop] = buf[(iLoop * bLength + jLoop)];
            }
            s[iLoop] = getLong(temp);
        }
        return s;
    }

    public byte[] Longs2Bytes(long[] s) {
        byte bLength = 8;
        byte[] buf = new byte[s.length * bLength];
        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = getBytes(s[iLoop]);
            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                buf[(iLoop * bLength + jLoop)] = temp[jLoop];
            }
        }
        return buf;
    }
}
