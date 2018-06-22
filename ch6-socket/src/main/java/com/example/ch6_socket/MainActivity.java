package com.example.ch6_socket;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<ChatMessage> list;
    MyAdapter ap;

    ListView listView;
    ImageView sendBtn;
    EditText msgEdit;

    boolean flagConnection = true;
    boolean isConnected = false;
    boolean flagRead = true;

    Handler writeHandler;

   //add~~~~~~~~~~~~~~~~~~~
    Socket socket;
    BufferedInputStream bin;
    BufferedOutputStream bout;

    SocketThread st;
    ReadThread rt;
    WriteThread wt;

    String serverIp = "70.12.110.50";
    int serverPort = 7070;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.lab1_list);
        sendBtn = (ImageView) findViewById(R.id.lab1_send_btn);
        msgEdit = (EditText) findViewById(R.id.lab1_send_text);

        sendBtn.setOnClickListener(this);

        list = new ArrayList<ChatMessage>();
        ap = new MyAdapter(this, R.layout.chat_item, list);
        listView.setAdapter(ap);

        sendBtn.setEnabled(false);
        msgEdit.setEnabled(false);

    }

    //채팅 메시지(read, write) 발생 후 호출..
    //ListView의 항목 추가로 출력역할...
    private void addMessage(String who, String msg) {
        ChatMessage vo = new ChatMessage();
        vo.who = who;
        vo.msg = msg;
        list.add(vo);
        ap.notifyDataSetChanged();
        //자동 스크롤 되게..
        listView.setSelection(list.size() - 1);
    }


    @Override
    public void onClick(View v) {
        if (!msgEdit.getText().toString().trim().equals("")) {
            Message msg=new Message();
            msg.obj=msgEdit.getText().toString();
            writeHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        st=new SocketThread(); //연결 시도 스레드 구동.. 나머지 스레드는
        //서버 연결이 성공한 순간 구동..
        st.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        flagConnection = false;
        isConnected = false;

        if (socket != null) {
            flagRead = false;
            writeHandler.getLooper().quit();
            try {
                bout.close();
                bin.close();
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void showToast(String message){
        Toast toast=Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    Handler mainHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==10){
                //connection ok~~
                showToast("connection ok~~");
                sendBtn.setEnabled(true);
                msgEdit.setEnabled(true);
            }else if(msg.what==20){
                //connection fail~~~
                showToast("connection fail~~");
                sendBtn.setEnabled(false);
                msgEdit.setEnabled(false);
            }else if(msg.what==100){
                //message read....
                addMessage("you", (String)msg.obj);
            }else if(msg.what==200){
                //message write...
                addMessage("me", (String)msg.obj);
            }
        }
    };


    //연결 관리 스레드.. 연결 상태 체크해서 연결이 안된경우 연결 시도..
    //지속적으로 연결이 유지되게 하기 위한 용도..
    class SocketThread extends Thread {

        public void run() {

            //add~~~~~~~~~~~~~~~~~
            while(flagConnection) {
                if(!isConnected) {
                    //현시점 연결 안된 상태..
                    //새로운 연결 시도..
                    socket=new Socket();
                    SocketAddress remoteAddr = new InetSocketAddress(serverIp, serverPort);
                    //연결시도..
                    try {
                        socket.connect(remoteAddr, 10000);
                        bout=new BufferedOutputStream(socket.getOutputStream());
                        bin=new BufferedInputStream(socket.getInputStream());

                        //새로운 연결이 성공한거다..
                        //만약 read thread, write thread가 동작 중이라면
                        //이전 연결정보로 동작하고 있는거다
                        //종료후 새로 구동해야 한다
                        if(rt != null) {
                            flagRead = false;
                        }
                        if(wt != null) {
                            writeHandler.getLooper().quit();
                        }

                        //새로운 연결정보로 스레드 구동..
                        wt = new WriteThread();
                        wt.start();
                        rt = new ReadThread();
                        rt.start();

                        isConnected = true;
                        //유저 화면에도 연결 성공 상태 표현..
                        //여기는 개발자 thread이다.. 이곳에서 view 객체 접근
                        //못한다..
                        //Handler에 메시지 전달해서.. ui thread에게 의뢰..

                        //Message : ui thread에게 의뢰시 넘기는 데이터 VO
                        //what : int - 이번 요청의 구분자..
                        //obj : Object - 실제 넘기는 데이터..
                        Message msg = new Message();
                        msg.what = 10;
                        mainHandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //현재 연결이 잘 되고 있는 상태..
                    SystemClock.sleep(10000);
                }
            }
        }
    }

    //android 7.1부터는 socket write도 thread에 의해 처리되야..
    //안그러면 에러난다.

    class WriteThread extends Thread {

        @Override
        public void run() {
            //add~~~~~~~~~~
            //외부 thread에서(UI thread) 작업을 의뢰하기 위한 Queue 준비..
            //Queue를 감지하는 Looper 준비...

            //Looper만 준비하면 message queue는 자동 준비..
            Looper.prepare();
            //queue에 message가 담기면 아래 객체 함수 자동 호출..
            writeHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    try {
                        bout.write(((String)msg.obj).getBytes());
                        bout.flush();

                        //전송 성공 후.. 화면 출력..
                        Message message = new Message();
                        message.what=200;
                        message.obj=msg.obj;
                        mainHandler.sendMessage(message);
                    } catch(IOException e) {
                        e.printStackTrace();
                        //SocketThread가 다시 connection 맺게..
                        isConnected=false;
                    }
                }
            };
            Looper.loop(); //queue감지 시작.. 이 looper가 종료되면 thread 자동 종료
            //이 looper죽지않는한 writethread 안죽는다.
        }
    }

    class ReadThread extends Thread {

        @Override
        public void run() {
            //add~~~~~~~~~~~~~~~~~~~
            byte[] buffer = null;
            while (flagRead) {
                //한꺼번에 1024바이트씩 받겠다.
                buffer = new byte[1024];
                try {
                    String message = null;
                    //아래 코드만 만나면 대기상태.. 서버로부터 데이터 넘어올때까지..
                    //실제 데이터 넘어오면 buffer에 담아주고 몇 바이트 읽었는지 리턴
                    int size = bin.read(buffer);
                    if(size > 0) {
                        message = new String(buffer, 0, size, "utf-8");

                        //메인 스레드에 화면 출력 의뢰
                        Message msg = new Message();
                        msg.what=100;
                        msg.obj=message;
                        mainHandler.sendMessage(msg);
                    } else {
                        //읽었는데 음수가 떨어졌다? 커넥션 다시 맺어라
                        isConnected = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isConnected = false;
                }
            }
            Message msg = new Message();
            msg.what=20;
            mainHandler.sendMessage(msg);
        }
    }


}

class ChatMessage {
    String who;
    String msg;
}

class MyAdapter extends ArrayAdapter<ChatMessage> {
    ArrayList<ChatMessage> list;
    int resId;
    Context context;

    public MyAdapter(Context context, int resId, ArrayList<ChatMessage> list) {
        super(context, resId, list);
        this.context = context;
        this.resId = resId;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resId, null);


        TextView msgView = (TextView) convertView.findViewById(R.id.lab1_item_msg);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msgView
                .getLayoutParams();

        ChatMessage msg = list.get(position);
        if (msg.who.equals("me")) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                    RelativeLayout.TRUE);
            msgView.setTextColor(Color.WHITE);
            msgView.setBackgroundResource(R.drawable.chat_right);
        } else if (msg.who.equals("you")) {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                    RelativeLayout.TRUE);
            msgView.setBackgroundResource(R.drawable.chat_left);
        }
        msgView.setText(msg.msg);

        return convertView;

    }
}

