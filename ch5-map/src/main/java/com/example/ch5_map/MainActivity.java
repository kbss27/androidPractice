package com.example.ch5_map;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

        //google play service 라이브러리는 구글의 각종 content를
        //서비스하기 위한 여러 API가 추가된 라이브러리이다..
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        providerApi = LocationServices.FusedLocationApi;
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
    //위치 획득 후 호출.. 데이터 출력..
    private void updateInfo(Location location) {
        onOffImageView.setImageResource(R.drawable.on);
        timeTextView.setText(getDateTime(location.getTime()));
        locationTextView.setText("LAT:"+
        convertDouble(location.getLatitude())+
        "LNG:"+convertDouble(location.getLongitude()));
        accuracyTextView.setText(location.getAccuracy()+"m");
    }

    //위치 획득후 호출.. 지도 제어..
    private void showMap(Location location) {
        //지도에서의 위치
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        //지도 옵션 초기 줌 레벨은 16레벨
        CameraPosition position = new CameraPosition.Builder().target(latLng).zoom(16f).build();
        //지도 중앙점 이동..
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        //이전에 그려진 marker 지우고..안지우면 이전것도나오고 현재것도 나온다
        map.clear();
        map.addMarker(new MarkerOptions().position(latLng)
                .title("MyLocation")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        //물방울 거꾸로되어있는거 지정

    }

    @Override
    protected void onResume() {
        super.onResume();
        //LocationProvider 준비.. 결과는 callback으로 알려준다..
        apiClient.connect();
        if(map == null) {
            ((SupportMapFragment)getSupportFragmentManager()
            .findFragmentById(R.id.map_view))
                    .getMapAsync(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(apiClient.isConnected()) {
            apiClient.disconnect();
        }
    }

    //지도 이용가능 상황에 호출..
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        UiSettings settings = map.getUiSettings();
        settings.setZoomControlsEnabled(true);
    }

    //provider이용 가능할때 호출
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //위치획득..
        Location location=providerApi.getLastLocation(apiClient);
        if(location != null) {
            updateInfo(location);
            showMap(location);
        }
    }

    //이용하던 로케이션이 갑자기 이용불가능한 상태되었을때 콜됨!
    @Override
    public void onConnectionSuspended(int i) {
        toast("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        toast("onConnectionFailed");
    }
}