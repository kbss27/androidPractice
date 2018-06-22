package com.example.ch6_http;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView titleView;
    TextView dateView;
    TextView contentView;
    NetworkImageView imageView;

    //네트웍 요청자.. 이곳에 request 객체를 add만 시키면 서버 연동 된다..
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = findViewById(R.id.lab2_title);
        dateView = findViewById(R.id.lab2_date);
        contentView = findViewById(R.id.lab2_content);
        imageView = findViewById(R.id.networkView);

        queue = Volley.newRequestQueue(this);

        //이미지 캐싱 알고리즘을 구현한 객체를 준비하고 알려주면
        //이 캐싱을 이용해 서버 다운로드하는 것을 자동
        final ImageLoader loader = new ImageLoader(queue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {

            }
        });

        //JsonObjectRequest의 결과를 받기 위한 callback...
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    titleView.setText(response.getString("title"));
                    dateView.setText(response.getString("date"));
                    contentView.setText(response.getString("content"));

                    //server로부터 받은 file path 문자열
                    String imageFile = response.getString("file");
                    if (imageFile != null && "".equals(imageFile)) {
                        //이미지 다운로드및 화면 출력
                        imageView.setImageUrl("http://70.12.110.71:8000/files/" + imageFile, loader);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //JSON 타입으로 서버 요청 정보..
        //결과값을 listener에서 받는다.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "http://70.12.110.71:8000/files/test.json",
                null,
                listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //실제 서버 요청 순간..
        queue.add(request);
    }
}
//테스트순서
//node js 설치 후
//파일 복사
//command 창에서
// node myHttpServer.js