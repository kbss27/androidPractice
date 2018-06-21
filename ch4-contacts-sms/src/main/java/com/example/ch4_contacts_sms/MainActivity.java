package com.example.ch4_contacts_sms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button sendButton;
    Button voiceButton;
    Button contactButton;
    EditText phoneEdit;
    EditText contentEdit;

    boolean contactPermission;
    boolean smsPermission;
    boolean phonePermission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.button_send);
        voiceButton = (Button) findViewById(R.id.button_voice);
        contactButton = (Button) findViewById(R.id.button_contacts);

        phoneEdit = (EditText) findViewById(R.id.edit_phone);
        contentEdit = (EditText) findViewById(R.id.edit_content);

        sendButton.setOnClickListener(this);
        voiceButton.setOnClickListener(this);
        contactButton.setOnClickListener(this);

        //add1~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //<permission>으로 보호된 기능을 <uses-permission>으로 선언하고 사용해야 한다
        //api-level 1부터 있었고 지금까지 변하지 않았다.
        //과거 버전에서는 개발자가 manifest 파일에 선언만 하면 실행되는 일종의 신고제였다..
        //유저에 의해서 거부가 불가능했다..
        //api level 23버전 폰부터는 개발자가 <uses-permission>을 선언했다고 하더라도
        //유저가 환경설정에서 거부가능하다.. 허가제로 바뀌었다..
        //그리고 초기 상태는 모드 disable 상태이다..
        //disable 상태명 <uses-permission>이 선언안된것과 동일하다... 코드 에러난다.
        //개발자 코드에서 직접 enable 시킬수는 없다. 유저만 가능하다..
        //manifest에 선언했다고 끝이 아니라.. disable 상태일수 있다는 것을 참조하는
        //코드를 작성해야 한다..

        //현재 퍼미션의 enable/disable 상태 파악..
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactPermission=true;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            smsPermission=true;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            phonePermission=true;
        }

        if(!contactPermission || !smsPermission || !phonePermission) {
            //퍼미션 enable을 유저에게 요청해야 한다..
            //우리 앱 내에서 dialog 띄워서 퍼미션 조정 의뢰..
            //시스템 다이얼로그이다..
            requestPermission();
        }
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }

    private void requestPermission() {
        //시스템 다이얼로그 띄워주는 함수
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE},
                200);
    }

    //requestPermissions에 의한 dialog가 닫히면 자동 호출.. dialog가 닫힌후에 사후추적 목적으로 자동 호출..
    //dialog에서 유저가 어떻게 퍼미션을 조정한건지 판단 목적..
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==200 && grantResults.length >0) {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                contactPermission = true;
            }
            if(grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                smsPermission = true;
            }
            if(grantResults[2]==PackageManager.PERMISSION_GRANTED) {
                phonePermission = true;
            }
        }
    }

    public void onClick(View v) {
		//add2~~~~~~~~~~~~~~~~~~~~~~~~~
        if(v==contactButton) {
            if(contactPermission) {
                //주소록 앱의 목록 activity를 intent로.. 결과 되돌려 받게..
                Intent intent = new Intent(Intent.ACTION_PICK,
                        Uri.parse("content://com.android.contacts/data/phones"));
                startActivityForResult(intent, 10);
            } else {
                requestPermission();
            }
        } else if (v==voiceButton) {
            //음성인식 앱을 intent로..
            //free_form(사람이말하는것 곧이곧대로), web_search(웹에있는 구글엔진) 두개 제공
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //dialog 타이틀 문자열..
            //언어를 지정하지 않으면 휴대폰은 기본 locale값 따라가게 되어있다.
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "본문을 말하세요.");
            startActivityForResult(intent, 20);
        } else if(v==sendButton) {
            if(smsPermission && phonePermission) {
                //유저 폰 번호 추출..
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                String myNumber = telephonyManager.getLine1Number();

                //sent ack에 반응할 Intent...
                Intent intent = new Intent("SENT_SMS_ACTION");
                //의뢰.. pendingintent로 감싼다.
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, intent, 0
                );
                //sms 전송..
                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage(
                        phoneEdit.getText().toString(),
                        myNumber,
                        contentEdit.getText().toString(),
                        pendingIntent,
                        null
                );
            } else {
                requestPermission();
            }
        }
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }

    //startActivityForResult에 의한 요청이 되돌아올때 자동 호출..
    //requestCode : intent를 발생 시킨곳에서.. 그 intent를 구분하기 위해서 준 개발자 임의 숫자값..
    //되돌아 오긴오는데 뭐가 되돌아온거냐를 확인하기 위해 10번이 되돌아왔다
    //resultCode : intent에 의해 실행된 곳에서 결과를 되돌리기 전에 요청을 어떻게 처리해서 되돌린거다를 지정하기 위한 값
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==10 && resultCode==RESULT_OK) {
            //홍길동을 선택해서 되돌아 오면 주소록에서 홍길동을 식별하는
            //식별자 값만 넘겨준다.. url의 맨 마지막 문자열이 식별자다.
            //uri의 path /~/사이의 하나하나가 segment
            String id = Uri.parse(data.getDataString()).getLastPathSegment();
            //content provider를 이용 원하는 데이터 획득..
            Cursor cursor = getContentResolver().query(
                    //provider 식별자 상수변수 제공
                    ContactsContract.Data.CONTENT_URI,
                    //select 하고자 하는 column 명..
                    new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
                    //where 조건..
                    ContactsContract.Data._ID+"="+id,
                    null,
                    null);
            cursor.moveToFirst();
            //주소록통해서받은 전화번호를 화면에 바로찍는다.
            phoneEdit.setText(cursor.getString(0));
        } else if (requestCode==20 && resultCode==RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
            );
            contentEdit.setText(results.get(0));
        }
    }

    //receiver는 아주 자주 특정 component(activity, service)의 inner 클래스로
    //만들어지고 자주.. 코드에서 동적으로 등록/해제 한다..
    //receiver가 intent만 발생하면 항상 실행되어야 한다면.. 정식 클래스
    //manifest에 등록..
    //receiver가 intent에 실행되는것이 특정 component실행중에만 의미가 있다면
    //그 component 내부에 작성하고 동적 등록하는게 일반적..
    BroadcastReceiver sentReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context _context, Intent _intent) {
                    String msg="";
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            // 전송 성공 처리; break;
                            msg="sms 전송 성공";
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            // 일반적인 실패 처리; break;
                            msg="sms 전송 실패";
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            // 무선 꺼짐 처리; break;
                            msg="무선 꺼짐";
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            // PDU 실패 처리; break;
                            msg="pdu 오류";
                            break;
                    }
                    Toast t = Toast.makeText(MainActivity.this, msg,
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            };

    protected void onResume() {
        super.onResume();
        registerReceiver(sentReceiver, new IntentFilter("SENT_SMS_ACTION"));
    };
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(sentReceiver);
    }

}