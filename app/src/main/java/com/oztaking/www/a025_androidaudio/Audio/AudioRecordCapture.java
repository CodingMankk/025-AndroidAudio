package com.oztaking.www.a025_androidaudio.Audio;

/***********************************************
 * 文 件 名: 
 * 创 建 人: OzTaking
 * 功    能：音频采集封转类
 * 创建日期: 
 * 修改时间：
 * 修改备注：
 ***********************************************/

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.orhanobut.logger.Logger;

/*****************************************
 * AudioRecord的工作流程
 * 【1】配置参数，初始化内部的音频缓冲区
 * 【2】开始采集
 * 【3】开辟线程，不断的从AudioRecord的缓冲区将音频数据"读出来"这个过程一定要及时，
 *  否则就会出现“overrun”的错误，该错误在音频开发中比较常见，意味着应用层没有及时地“取走”音频数据，导
 *  致内部的音频缓冲区溢出
 * 【4】停止采集、释放资源
 * 【注意】配置权限：<uses-permission android:name="android.permission.RECORD_AUDIO" />
 ******************************************/

public class AudioRecordCapture {


    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord;
    private int mMinBufferSize = 0;
    /**
     * 是否已经开始采集声音
     */
    private boolean mIsCaptureStarted;

    private OnAudioFrameCaptureListener mOnAudioFrameCaptureListener;
    private int mMinBufferSize1;
    private boolean mIsLoopExit;
    private Thread mCaptureThread;

    /**
     * 设置音频监听
     */
    public interface OnAudioFrameCaptureListener{
        public void onAudioFrameCaptured(byte[] audioData);
    }

    public boolean isCapturedStated(){
        return mIsCaptureStarted;
    }

    public void setOnAudioFrameCaptureListener(OnAudioFrameCaptureListener listener){
        this.mOnAudioFrameCaptureListener = listener;
    }

    public boolean startCapture(){
        return startCapture();
    }

    public boolean startCapture(int audioSource,int sampleRateInHz,
                                int channelConfig,int audioFormat){
        if (mIsCaptureStarted){
            Logger.i("Capture already started!");
            return false;
        }

        mMinBufferSize1 = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig,
                audioFormat);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE){
            return false;
        }

        Logger.i("getMinBufferSize = "+mMinBufferSize+"bytes");

        /**
         * 创建AudioRecord实例
         */
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig,
                audioFormat, mMinBufferSize);

        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED){
            Logger.i("AudioRecord initialize fail...");
            return false;
        }

        /**
         * 开始录制
         */
        mAudioRecord.startRecording();

        mIsLoopExit = false;

        /**
         * 开启采集线程
         */
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();

        /**
         * 设置标志位
         */
        mIsCaptureStarted = true;

        Logger.i("Start audio capture success!");

        return true;

    }


    /**
     * 停止采集
     */
    public void stopCapture(){

        if (!mIsCaptureStarted){
            return;
        }

        mIsLoopExit = true;

        try {
            /**
             * 设置中断位
             */
            mCaptureThread.interrupt();
            /**
             * main主线程等待该线程1s
             */
            mCaptureThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * 核心--->如果处理录制状态则停止
         */
        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
            mAudioRecord.stop();
        }

        /**
         * 释放资源
         */
        mAudioRecord.release();
        mIsCaptureStarted = false;
        mOnAudioFrameCaptureListener = null;

        Logger.i("Stop audio Capture success!");

    }



    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {

            while (!mIsLoopExit){
                byte[] buffer = new byte[mMinBufferSize];
                int read = mAudioRecord.read(buffer, 0, mMinBufferSize);
                if (read == AudioRecord.ERROR_INVALID_OPERATION){
                    Logger.i("ERROR_INVALID_OPERATION");
                }else if(read == AudioRecord.ERROR_BAD_VALUE){
                    Logger.i("ERROR_BAD_VALUE");
                }else {
                    if (mOnAudioFrameCaptureListener != null){
                        mOnAudioFrameCaptureListener.onAudioFrameCaptured(buffer);
                    }
                    Logger.i("Data reading"+read+"bytes");
                }
            }
        }
    }
}
