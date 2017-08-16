package com.haiblee.base.log;

import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dell on 2017/8/1 0001.
 */

public class DefaultLogImpl implements ILog{
    private static final String TAG = "DefaultLogImpl";
    //"TIME [PID:ThreadName][TAG][LEVEL]MSG"
    private static final String FORMAT = "%s [%s:%s][%s][%s]%s\n";
    private final SimpleDateFormat mRowDateFormat;
    private final WriteThread mWriteThread;

    public DefaultLogImpl(String logPathName) {
        mRowDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        mWriteThread = new WriteThread(logPathName);
        mWriteThread.start();
    }

    private String structureRow(int level, String tag, String msg) {
        return String.format(FORMAT,
                mRowDateFormat.format(new Date()),
                Process.myPid(),
                threadStr(),
                tag,
                levelStr(level),
                msg);
    }

    @Override
    public void print(int level, String tag, String msg) {
        switch (level){
            case ILog.LEVEL_V: Log.v(tag,msg);
            case ILog.LEVEL_D: Log.d(tag,msg);
            case ILog.LEVEL_I: Log.i(tag,msg);
            case ILog.LEVEL_W: Log.w(tag,msg);
            case ILog.LEVEL_E: Log.e(tag,msg);
            default: Log.v(tag,msg);
        }
        if(level >= ILog.LEVEL_I){
            String outStr = structureRow(level,tag,msg);
            if(outStr == null) outStr = "NullLog\n";
            boolean success = mWriteThread.offer(outStr);
            if(!success) Log.e(TAG,"print(): mLogQueue.offer result failed !");
        }
    }

    private static String threadStr(){
        return Looper.myLooper() == Looper.getMainLooper() ? "Main" : Thread.currentThread().getName();
    }

    private String levelStr(int level){
        switch (level){
            case ILog.LEVEL_V: return "V";
            case ILog.LEVEL_D: return "D";
            case ILog.LEVEL_I: return "I";
            case ILog.LEVEL_W: return "W";
            case ILog.LEVEL_E: return "E";
        }
        return "Unknown";
    }

    private static class WriteThread extends Thread {
        private static final String TAG = "WriteThread";
        private static final int BUFFER_SIZE = 2 * 1024;
        private static final long INTERVAL_WRITE = 10 * 1000;
        private static final long LAST_WRITE_INTERVAL = 10 * 1000;
        private final BlockingQueue<String> mLogQueue;
        private final String mLogPathName;
        private final ByteBuffer mBuffer;
        private long mTotalWriteLength = 0;
        private boolean isQuit = false;
        private long mLastWriteTime = 0;

        public WriteThread(String pathName) {
            super(TAG);
            mLogPathName = pathName;
            mLogQueue = new LinkedBlockingDeque<>();
            mBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        }

        public boolean offer(String log){
            return mLogQueue.offer(log);
        }

        public void markFlush(){
            mLastWriteTime = 0;
        }

        private int write(FileChannel channel) throws IOException {
            mBuffer.flip();
            int length = mBuffer.limit();
            if(length > 0){
                while (channel.write(mBuffer) > 0){
                    mTotalWriteLength += length;
                }
                mLastWriteTime = System.currentTimeMillis();
            }
            mBuffer.clear();
            return length;
        }

        @Override
        public void run() {
            super.run();
            final boolean DEBUG = ((XLog.flag & XLog.MARK_DEBUG) != 0);
            try {
                File file = createLogFileIfNeed();
                FileOutputStream fos = new FileOutputStream(file,true);
                FileChannel fileChannel = fos.getChannel();
                mBuffer.clear();
                try{
                    while (!isQuit) {
                        String log = mLogQueue.poll(INTERVAL_WRITE, TimeUnit.MILLISECONDS);
                        if(log == null){//time out
                            int length = write(fileChannel);
                            if(DEBUG) Log.d(TAG,"mLogQueue.poll time out execute write length = "+length + ",mTotalWriteLength = "+mTotalWriteLength);
                        }else{
                            byte[] buff = log.getBytes();
                            if(DEBUG) Log.d(TAG, String.format("mBuffer size = %s,log buff length = %s,remaining size = %s",
                                    mBuffer.capacity(),buff.length,mBuffer.remaining()));
                            if(buff.length < mBuffer.remaining()){
                                mBuffer.put(buff);
                                if(System.currentTimeMillis() - mLastWriteTime > LAST_WRITE_INTERVAL){
                                    int length = write(fileChannel);
                                    if(DEBUG) Log.d(TAG,"last write time out execute write,length = "+length+",mTotalWriteLength = "+mTotalWriteLength);
                                }
                            }else{
                                int length = write(fileChannel);
                                if(DEBUG) Log.d(TAG,"mBuffer capacity full execute write,length = "+length+",mTotalWriteLength = "+mTotalWriteLength);
                                mBuffer.put(buff);
                            }
                        }
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                    Log.e(TAG,"run() InterruptedException : "+e.getMessage());
                    isQuit = true;
                }finally {
                    fileChannel.force(false);
                    fos.close();
                    fileChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"run() createLogFile IOException : "+e.getMessage());
            }
        }

        private File createLogFileIfNeed() throws IOException {
            File log = new File(mLogPathName);
            File parent = log.getParentFile();
            if(parent == null || !parent.exists()){
                log.mkdirs();
            }
            log.createNewFile();
            if(!log.exists()){
                Log.e(TAG,"createLogFileIfNeed(),logFile create failed , mLogPathName = "+mLogPathName);
                throw new IOException(TAG + " createLogFileIfNeed failed !");
            }
            return log;
        }
    }


}
