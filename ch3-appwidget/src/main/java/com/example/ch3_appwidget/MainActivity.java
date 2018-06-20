package com.example.ch3_appwidget;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText editText;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText=(EditText)findViewById(R.id.lab2_edit);
        btn=(Button)findViewById(R.id.lab2_btn);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String data=editText.getText().toString();
        if(data != null && !data.equals("")) {
            //add~~~~~~~~~~~~~~~~
            //신규 일정 등록..
            DBHelper helper = new DBHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("insert into tb_data (content) values (?)", new String[]{data});
            db.close();

            //intent로 receiver를 실행시켜 appwidget 목록 update 되게..
            Intent intent = new Intent(this, MyAppWidgetReceiver.class);
            intent.putExtra("mode", "data");
            sendBroadcast(intent);
        }
    }
}
