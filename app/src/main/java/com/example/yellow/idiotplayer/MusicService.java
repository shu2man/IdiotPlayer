package com.example.yellow.idiotplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;

import java.util.Random;

/**
 * Created by Yellow on 2017-11-22.
 */

public class MusicService extends Service {
    private MediaPlayer mPlayer;
    private String[] Songs={"melt.mp3","One Republic - Counting Stars.mp3","LINKIN PARK - Numb.mp3","周深 - 大鱼.mp3",
    "金玟岐 - 山丘.mp3","少司命 - 临安记忆.mp3","张信哲 - 焚情.mp3"};
    private String SDPath= Environment.getExternalStorageDirectory().toString();

    @Override
    public void onCreate(){
        super.onCreate();
        mPlayer=MediaPlayer.create(this,R.raw.melt);

        /*try{
            mPlayer.setDataSource("/kgmusic/download/melt.mp3");
            mPlayer.prepare();

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Random rand=new Random();
                    int index=rand.nextInt(7);
                    try{
                        mPlayer.reset();
                        mPlayer.setDataSource("file://data/kgmusic/download/"+Songs[index]);
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }*/
    }
    /*@Override
    public int onStartCommand(Intent intent,int flags,int startId){
        //mPlayer.start();
        //return super.onStartCommand(intent,flags,startId);
        return Service.START_STICKY;
    }*/
    @Override
    public void onDestroy(){
        mPlayer.stop();
        mPlayer.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent){
        return new MusicBinder();
    }
    /*@Override
    public boolean onUnbind(Intent intent){
        mPlayer.stop();
        return super.onUnbind(intent);
    }*/

    public class MusicBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags){
            switch (code){
                case 100:
                    //play
                    if(mPlayer!=null) mPlayer.start();
                    break;
                case 101:
                    //pause
                    if(mPlayer!=null&&mPlayer.isPlaying()) mPlayer.pause();
                    else if(mPlayer!=null) mPlayer.start();
                    break;
                case 102:
                    //stop
                    if(mPlayer!=null){
                        try{
                            mPlayer.stop();
                            mPlayer.reset();
                            //mPlayer=MediaPlayer.create(this,R.raw.melt);
                            setSong();
                            mPlayer.prepare();
                            mPlayer.seekTo(0);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;
                case 103:
                    //quit
                    if(mPlayer!=null){
                        mPlayer.stop();
                        mPlayer.release();
                    }
                    break;
                case 104:
                    //seekTo
                    if(mPlayer!=null){
                        mPlayer.seekTo(data.readInt());
                    }
                    break;
                case 105:
                    //get media player position
                    if(mPlayer!=null){
                        int pos=mPlayer.getCurrentPosition();
                        reply.writeInt(pos);
                    }
                    break;
                case 106:
                    //get duration
                    if(mPlayer!=null){
                        int len=mPlayer.getDuration();
                        reply.writeInt(len);
                    }
                    break;
                case 107:
                    //next
                    if(mPlayer!=null){
                        Random rand=new Random();
                        int index=rand.nextInt(7);
                        try{
                            mPlayer.reset();
                            mPlayer.setDataSource("file://data/kgmusic/download/"+Songs[index]);
                            mPlayer.prepare();
                            mPlayer.start();

                            //reply.writeString(Songs[index]);
                            reply.writeString(SDPath);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;
                case 108:
                    //playing or not
                    if(mPlayer!=null){
                        if(mPlayer.isPlaying()) reply.writeInt(1);
                        else reply.writeInt(0);
                    }
                    break;
            }
            return true;//super.onTransact(code,data,reply,flags)
        }
    }

    public void play(){
        mPlayer.start();
    }
    public void pause(){
        mPlayer.pause();
    }
    public void stop(){
        mPlayer.stop();
    }
    public int getAllTime(){
        return mPlayer.getDuration();
    }
    public int getCurrentTime(){
        return mPlayer.getCurrentPosition();
    }
    public void setTime(int position){
        mPlayer.seekTo(position);
    }
    public void setPlayer(MediaPlayer mp){
        mPlayer=mp;
    }

    public void setSong(){
        mPlayer=MediaPlayer.create(this,R.raw.melt);
    }

}
