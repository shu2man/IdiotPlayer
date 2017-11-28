package com.example.yellow.idiotplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private boolean isPlaying=false;
    private MediaPlayer mPlayer;
    private TextView playtstate;
    private SeekBar bar;
    private MusicService myservice;
    private boolean isFirst=true;
    private IBinder mBinder;
    private boolean hasPermission=true;
    private float rotateF=0f;
    private ServiceConnection sc;

    private static final int REQUEST_EXTERNAL_STORAGE=1;
    private static String[] PERMISSION_STORAGE={
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //myservice=new MusicService();

        initAndBind();

        verifyPermission(this);

        initBar();
        //playerSetText();
    }

    public void initAndBind(){
        //mPlayer=MediaPlayer.create(this,R.raw.melt);
        playtstate=(TextView)findViewById(R.id.play_state);
        playtstate.setText("READY");

        Intent intent=new Intent(this,MusicService.class);
        startService(intent);
        sc=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                //myservice=((MusicService.MusicBinder) iBinder ).getService();
                mBinder=iBinder;
                //myservice.setPlayer(mPlayer);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                sc=null;
            }
        };
        bindService(intent,sc,BIND_AUTO_CREATE);
    }
    public void initBar(){
        bar=(SeekBar)findViewById(R.id.seekbar_music);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try{
                    int code=104;//SeekTo
                    Parcel data=Parcel.obtain();
                    data.writeInt(bar.getProgress()*1000);
                    mBinder.transact(code,data,Parcel.obtain(),0);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        Parcel reply=Parcel.obtain();
        try{
            int code=106;//获取总时长
            mBinder.transact(code,Parcel.obtain(),reply,0);
        }catch(Exception e){
            e.printStackTrace();
        }
        int len=reply.readInt()/1000;//=myservice.getAllTime()/1000;//get到的是ms,换为s
        bar.setMax(len);
        TextView timeAll=(TextView)findViewById(R.id.time_all);
        timeAll.setText(intToTime(len));

        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 111:
                        //更新UI
                        try{
                            Parcel playState=Parcel.obtain();
                            mBinder.transact(108,Parcel.obtain(),playState,0);
                            int state=playState.readInt();
                            if(sc!=null ){//&& state==1
                                updateBar();
                               if(state==1) rotateImg();
                                playerSetText(0);
                            }
                        }catch(Exception e){
                                e.printStackTrace();
                        }
                        break;
                }
            }
        };
        Thread thread=new Thread(){
            @Override
            public void run(){
                while(true){
                    try{
                        Thread.sleep(300);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(sc!=null&&hasPermission) handler.obtainMessage(111).sendToTarget();
                }
            }
        };
        thread.start();
    }


    public void changePlayState(View view){
        initBar();
        playerSetText(1);
        try{
            int code=101;
            mBinder.transact(code,Parcel.obtain(),Parcel.obtain(),0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void playerSetText(int t){
        Button btn=(Button)findViewById(R.id.play_btn);
        Parcel reply=Parcel.obtain();
        try{
            int code=108;
            mBinder.transact(code,Parcel.obtain(),reply,0);
        }catch(Exception e){
            e.printStackTrace();
        }
        if(reply.readInt()==t) {
            //myservice.pause();
            playtstate.setText("PAUSED");
            //isPlaying=false;
            btn.setText("PLAY");
        }
        else {//if(reply.readInt()==t)
            //myservice.play();
            playtstate.setText("PLAYING");
            //isPlaying=true;
            btn.setText("PAUSE");
        }
    }
    public void stopPlay(View view){
        ImageButton iv=(ImageButton)findViewById(R.id.img_cd);
        rotateF=0f;
        //iv.setRotation(rotateF);

        RotateAnimation rotate=new RotateAnimation(rotateF,rotateF, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotate.setDuration(1);
        rotate.setFillAfter(true);
        iv.setAnimation(rotate);

        Button btn=(Button)findViewById(R.id.play_btn);
        btn.setText("PLAY");
        //isPlaying=false;
        //myservice.stop();
        try{
            int code=102;
            Parcel data=Parcel.obtain();
            Parcel reply=Parcel.obtain();
            mBinder.transact(code,data,reply,0);
        }catch(Exception e){
            e.printStackTrace();
        }
        //isFirst=true;
        playtstate.setText("READY");
        TextView currentTime=(TextView)findViewById(R.id.time_current);
        currentTime.setText("00:00");
        bar.setProgress(0);
    }
    public void quitApp(View view){
        unbindService(sc);
        sc=null;
        //myservice.stop();
        try{
            int code=103;//quit
            mBinder.transact(code,Parcel.obtain(),Parcel.obtain(),0);
        }catch(Exception e){
            e.printStackTrace();
        }
        this.finish();
        System.exit(0);
    }
    public void randomNext(View view){
        try{
            int code=107;//next
            Parcel reply=Parcel.obtain();
            mBinder.transact(code,Parcel.obtain(),reply,0);
            TextView tv=(TextView)findViewById(R.id.song_name_singer);
            tv.setText(reply.readString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public String intToTime(int len){
        int min;
        int sec;
        min=len/60;
        sec=len%60;
        String timem;
        String times;
        if(min<10) timem="0"+min;
        else timem=""+min;
        if(sec<10) times="0"+sec;
        else times=""+sec;
        return timem+":"+times;
    }

    public void updateBar(){
        Parcel reply=Parcel.obtain();
        Parcel reply2=Parcel.obtain();
        try{
            int code=105;//获取当前播放时间
            mBinder.transact(code,Parcel.obtain(),reply,0);
            mBinder.transact(106,Parcel.obtain(),reply2,0);
        }catch(Exception e){
            e.printStackTrace();
        }
        int len=reply2.readInt()/1000;
        bar.setMax(len);
        int pos=reply.readInt()/1000;//=myservice.getCurrentTime()/1000;//get到的是ms,换为s
        bar.setProgress(pos);
        TextView tv=(TextView)findViewById(R.id.time_current);
        tv.setText(intToTime(pos));
    }
    public void rotateImg(){
        ImageButton iv=(ImageButton)findViewById(R.id.img_cd);
        rotateF+=1f;
        if(rotateF==360f) rotateF=0f;
        //iv.setRotation(rotateF);
        RotateAnimation rotate=new RotateAnimation(rotateF,rotateF+1f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotate.setDuration(298);
        rotate.setFillAfter(true);
        iv.setAnimation(rotate);
    }

    public void verifyPermission(Activity activity){
        try{
            int permission= ActivityCompat.checkSelfPermission(activity,"android.permission.READ_EXTERNAL_STORAGE");
            if(permission!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,PERMISSION_STORAGE,1);
            }
            else hasPermission=true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permission[],int[] grantResults){
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            //permission granted
        }
        else{
            //permission not set
        }
    }


}
