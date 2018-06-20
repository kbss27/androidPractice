package com.example.ch3_aidl_client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.ch3_aidl.IPlayService;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {




    IPlayService pService;

    ImageButton start;
    ImageButton stop;
    ProgressBar mProgress;

    boolean isRunning = true;
    ProgressThread pt;

    Handler handler;


    Intent intent;


    //--add 1---------------
        //bindService의 매개변수로 지정할 ServiceConnection 구현 객체
    ServiceConnection connection = new ServiceConnection() {
        //service 구동되고 bind객체가 넘어온 순간 호출..
        //두번째 매개변수가 service에서 넘긴 객체이다..
        //aidl 대행자.. Stub 객체이다..
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pService = IPlayService.Stub.asInterface(service);
            start.setEnabled(true);
            checkService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            pService=null;
        }
    };
	//------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        start = (ImageButton) findViewById(R.id.start);
        stop = (ImageButton) findViewById(R.id.stop);

        mProgress = (ProgressBar) findViewById(R.id.pb);
        mProgress.setProgress(0);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);

        start.setEnabled(false);
        stop.setEnabled(false);


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case 1:	// stopped media
                        start.setEnabled(true);
                        stop.setEnabled(false);
                        mProgress.setProgress(0);
                        break;
                }
                super.handleMessage(msg);
            }
        };

		//add2-------------------------------------
        //service를 구동시키기 위한 intent 준비.. intent 발생은 다른곳에서..
        //bindService 방식의 intent에는 꼭 대상이 되는 app의 package명이 추가되어야 한다..
        intent=new Intent("com.multi.ACTION_PLAY");
        intent.setPackage("com.example.ch3_aidl");
		//--------------------------------------
    }

    private void checkService() {
        if(pService != null) {
            try {
                if(pService.getMediaStatus() == CommonProperties.MEDIA_STATUS_STOP) {
                    Log.d("kkang", "MEDIA_STATUS_STOP");
                    stop.setEnabled(false);
                } else if(pService.getMediaStatus() == CommonProperties.MEDIA_STATUS_RUNNING) {
                    Log.d("kkang", "MEDIA_STATUS_RUNNING");
                    start.setEnabled(false);
                    isRunning = true;
                    pt = new ProgressThread();
                    pt.start();
                }
            } catch (RemoteException e) {

            }
        }

    }

    //add3-------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        checkService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(connection);
        isRunning=false;
    }

    //---------------------------------

    @Override
    public void onClick(View v) {
        if(v == start) {
            try {
                pService.start();
                mProgress.setMax(pService.getMaxDuration());
                isRunning = true;
                pt = new ProgressThread();
                pt.start();

                start.setEnabled(false);
                stop.setEnabled(true);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if(v == stop) {
            try {
                pService.stop();
                isRunning = false;
                start.setEnabled(true);
                stop.setEnabled(false);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }


    class ProgressThread extends Thread {
        @Override
        public void run() {
            while(isRunning) {
                try {

                    if(pService.getMediaStatus() == CommonProperties.MEDIA_STATUS_COMPLETED) {
                        handler.sendEmptyMessage(1);
                        break;
                    } else {
                        mProgress.setProgress(pService.currentPosition());
                        SystemClock.sleep(1000);
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}