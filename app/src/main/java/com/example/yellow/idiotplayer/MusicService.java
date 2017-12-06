package com.example.yellow.idiotplayer;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.provider.MediaStore;
import android.widget.Toast;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Yellow on 2017-11-22.
 */

public class MusicService extends Service {
    private MediaPlayer mPlayer;
    private String[] Songs={"melt.mp3","One Republic - Counting Stars.mp3","LINKIN PARK - Numb.mp3","周深 - 大鱼.mp3",
    "金玟岐 - 山丘.mp3","少司命 - 临安记忆.mp3","张信哲 - 焚情.mp3"};
    private String SDPath= Environment.getExternalStorageDirectory().toString();

    private boolean isStopped=true;//reply 2
    private Boolean isFirstLaunch=true;

    @Override
    public void onCreate(){
        super.onCreate();
        mPlayer=MediaPlayer.create(this,R.raw.melt);
        mPlayer.setLooping(true);
        MusicList();
        /*try{
            mPlayer.setDataSource("/data/kgmusic/download/少司命 - 临安记忆.mp3");
            mPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }*/

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
                    else if(mPlayer!=null) {
                        mPlayer.start();
                        isStopped=false;
                    }
                    isFirstLaunch=false;
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
                            isStopped=true;
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
                    nextSong();
                    //next
                    /*if(mPlayer!=null){
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
                    }*/
                    break;
                case 108:
                    //play state, playing pause,stop
                    if(mPlayer!=null){
                        if(mPlayer.isPlaying()) {
                            reply.writeInt(1);
                            //Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                        }
                        else if(isStopped) {
                            reply.writeInt(-1);
                            //Toast.makeText(getApplicationContext(),"-1",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            reply.writeInt(0);
                            //Toast.makeText(getApplicationContext(),"0",Toast.LENGTH_SHORT).show();
                        }

                    }
                    else reply.writeInt(-1);
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

    public class MusicInfo{
        public String title;
        public String url;
        public String artist;
        public int duration;
        public long id;
    }

    private Cursor mCursor;
    private List<MusicInfo> mMusicInfos = new ArrayList<>();
    public void MusicList(){
        mCursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA}, null, null, null);
        for (int i = 0; i < mCursor.getCount(); ++i) {
            MusicInfo musicInfo = new MusicInfo();  //MusicInfo类是数据储存单元
            mCursor.moveToNext();   //读取下一行，moveToNext()有boolean返回值，执行成功返回ture,反之false，可用于判断是否读取完毕。

/*            int idcol=mCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titlecol=mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int atistcol=mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationcol=mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int urlcol=mCursor.getColumnIndex(MediaStore.Audio.Media.DATA);*/


            //long id = mCursor.getLong(idcol);
/*            String title = mCursor.getString(titlecol);
            String artist = mCursor.getString(atistcol);
            int duration = mCursor.getInt(durationcol);
            String url = mCursor.getString(urlcol);*/
/*            String album = mCursor.getString(2);
            String displayName = mCursor.getString(3);
            long size = mCursor.getLong(6);*/
//musicInfo.id=id;
            String title = mCursor.getString(0);
            String artist = mCursor.getString(3);
            int duration = mCursor.getInt(4);
            String url = mCursor.getString(6);

            musicInfo.title=title;
            musicInfo.artist=artist;
            musicInfo.duration=duration;
            musicInfo.url=url;

           if(duration>=12000) mMusicInfos.add(musicInfo);  //添加到List
        }
    }

    public void nextSong(){
        Random rand=new Random();
        int index=rand.nextInt(mCursor.getCount());
        try{
            mPlayer.stop();
            mPlayer.setDataSource(mMusicInfos.get(index).url);
            mPlayer.prepare();
            mPlayer.start();

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Random Next Failed",Toast.LENGTH_SHORT).show();
        }

    }

}
