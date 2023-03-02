package com.xiao.embeddedcar.Utils.Network;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    private static final String TAG = "SerialPort";
    private static final String DEFAULT_SU_PATH = "/system/xbin/su";
    private static String sSuPath = DEFAULT_SU_PATH;
    private final FileInputStream mFileInputStream;
    private final FileOutputStream mFileOutputStream;

    public static void setSuPath(String suPath) {
        if (suPath == null) return;
        sSuPath = suPath;
    }

    static {
        System.loadLibrary("serial_port");
    }

    public SerialPort(File device, int baudRate, int flags) throws SecurityException, IOException {
        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec(sSuPath);
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        FileDescriptor mFd = open(device.getAbsolutePath(), baudRate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    public SerialPort(String devicePath, int baudRate, int flags) throws SecurityException, IOException {
        this(new File(devicePath), baudRate, flags);
    }

    public SerialPort(File device, int baudRate) throws SecurityException, IOException {
        this(device, baudRate, 0);
    }

    public SerialPort(String devicePath, int baudRate) throws SecurityException, IOException {
        this(new File(devicePath), baudRate, 0);
    }

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudRate, int flags);

    public native void close();
}
