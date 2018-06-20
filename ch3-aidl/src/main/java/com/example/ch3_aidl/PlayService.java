package com.example.ch3_aidl;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;

public class PlayService extends Service {

    MediaPlayer player; //음원, 영상 play..
    //음원, 영상 녹화는 MediaRecoder

    int status = 0;

    public PlayService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //bindService 한 곳에 객체 공유..
        //실제 업무가 구현된 객체가 넘어가는게 아니라 aidl을 대행해주는
        //stub 객체가 만들어져서 넘어간다..
        return new IPlayService.Stub() {
            @Override
            public int currentPosition() throws RemoteException {
                if(player.isPlaying())
                    return player.getCurrentPosition();
                else
                    return 0;
            }

            @Override
            public int getMaxDuration() throws RemoteException {
                if(player.isPlaying())
                    return player.getDuration();
                else
                    return 0;
            }

            @Override
            public void start() throws RemoteException {
                if(!player.isPlaying()) {
                    player=MediaPlayer.create(PlayService.this, R.raw.music);
                    player.start();
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            status=CommonProperties.MEDIA_STATUS_COMPLETED;
                        }
                    });
                }
                status=CommonProperties.MEDIA_STATUS_RUNNING;
            }

            @Override
            public void stop() throws RemoteException {
                if(player.isPlaying()) {
                    player.stop();
                }
                status = CommonProperties.MEDIA_STATUS_STOP;
            }

            @Override
            public int getMediaStatus() throws RemoteException {
                return status;
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
