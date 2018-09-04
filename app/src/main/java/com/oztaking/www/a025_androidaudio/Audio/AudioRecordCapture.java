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

        return true;
    }












}
