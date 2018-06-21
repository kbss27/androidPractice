package com.example.ch5_map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,    //위치정보 제공자 이용가능시
        GoogleApiClient.OnConnectionFailedListener, //위치정보 제공자 이용 불가시
        OnMapReadyCallback  //지도 이용 가능시.. 이때 지도 객체 초기화 위해
{

    TextView providerTextView;
    ImageView onOffImageView;
    TextView timeTextView;
    TextView locationTextView;
    TextView accuracyTextView;

    //provider 준비 역할..결과에 따른 callback 호출 역할..
    GoogleApiClient apiClient;
    //실제 위치 정보 획득
    FusedLocationProviderApi providerApi;
    GoogleMap map;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        providerTextView = (TextView) findViewById(R.id.txt_location_provider);
        onOffImageView = (ImageView) findViewById(R.id.img_location_on_off);
        timeTextView = (TextView) findViewById(R.id.gps_time);
        locationTextView = (TextView) findViewById(R.id.gps_location);
        accuracyTextView = (TextView) findViewById(R.id.gps_accuracy);


    }

    private void toast(String msg){
        Toast t=Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        t.show();
    }

    private String getDateTime(long time) {
        if (time == 0)
            return "";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return formatter.format(new java.util.Date(time));
    }

    private String convertDouble(double input) {
        DecimalFormat format = new DecimalFormat(".######");
        return format.format(input);
    }
    //위의 세개 개발자 util함수



}