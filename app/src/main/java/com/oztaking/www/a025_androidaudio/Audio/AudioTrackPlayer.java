package com.oztaking.www.a025_androidaudio.Audio;

/***********************************************
 * 文 件 名: 
 * 创 建 人: OzTaking
 * 功    能：
 * 创建日期: 
 * 修改时间：
 * 修改备注：
 ***********************************************/

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.orhanobut.logger.Logger;


/*************************************************
 * AudioTrack 则更接近底层，提供了非常强大的控制能力， 支持低延迟播放，适合流媒体和VoIP语音电话等场景
 * 工作流程：
 * 【1】配置参数，初始化内部的音频播放缓冲区
 * 【2】开始播放
 * 【3】开启线程，不断向AudioTrack缓冲区书写数据。否则数据读取不及时会出现"underRun"错误
 * 【4】停止播放，释放资源
 *************************************************/
public class AudioTrackPlayer {


    /**
     * 采集铃声为音乐铃声
     */
    private static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    /**
     * 采集速率：44.1kHz
     */
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    /**
     * 双声道采集
     */
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    /**
     * 采集深度：16Bit
     */
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 播放模式：Streaming
     */
    private static final int DEFAULT_PLAY_MODE = AudioTrack.MODE_STREAM;


    private boolean mIsPlayStarted = false;
    private int mMinBufferSize = 0;
    private AudioTrack mAudioTrack;
    private int mMinBufferSize1;

    public boolean startPlayer(){
        return startPlayer(DEFAULT_STREAM_TYPE,DEFAULT_SAMPLE_RATE,
                DEFAULT_CHANNEL_CONFIG,DEFAULT_AUDIO_FORMAT, DEFAULT_PLAY_MODE);
    }

    private boolean startPlayer(int streamType, int sampleRate, int
            channelConfig, int audioFormat, int playMode) {

        if (mIsPlayStarted){
            Logger.i("AudioTrack is already start...");
            return false;
        }


        mMinBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        if (mMinBufferSize == AudioTrack.ERROR_BAD_VALUE){
            Logger.i("invalid parameter!!!");
            return false;
        }

        mAudioTrack = new AudioTrack(streamType, sampleRate, channelConfig,
                audioFormat, mMinBufferSize, playMode);
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
            Logger.i("AudioTrack initialize fail...");
            return false;
        }

        mIsPlayStarted = true;

        Logger.i("AudioTrack player success!!!");

        return true;

    }

    public int getMinBufferSize(){
        return mMinBufferSize;
    }

    public void stopPlayer(){

        if (!mIsPlayStarted){
            return;
        }

        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
            mAudioTrack.stop();
        }

        mAudioTrack.release();
        mIsPlayStarted = false;

        Logger.i("Stop AudioTrack Player success...");

    }

    public boolean play(byte[] audioData,int offsetInBytes, int sizeInBytes){
        if (!mIsPlayStarted){
            Logger.i("Player is not started...");
            return false;
        }

        if (offsetInBytes<mMinBufferSize){
            Logger.i("AudioTrack data is not enough...");
            return false;
        }

        if (mAudioTrack.write(audioData,offsetInBytes,sizeInBytes) != sizeInBytes){
            Logger.i("Could not write all the samples to the audio " +
                    "device...");
        }

        mAudioTrack.play();

        Logger.i("AudioTrack played"+sizeInBytes+"bytes...");

        return true;
    }

    /**
     * 没有增加断点保存的记录
     */
    public void pause(){
        mAudioTrack.pause();
    }







}
