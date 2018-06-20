package com.example.ch3_appwidget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

//launcher app에 전달될 ListView를 위한 Adapter 역할..
//Service가 생성해서 전달할거다..
public class MyRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    //항복 데이터 추상화 VO 클래\스
    class ItemV0 {
        int _id;
        String content;
    }
    Context context;
    ArrayList<ItemV0> arrayList; //항목 구성 집합 데이터..

    public MyRemoteViewsFactory(Context context) {
        this.context=context;
    }

    //항목 데이터 db select 역할..
    private void selectDB() {
        arrayList = new ArrayList<>();
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select _id, content from tb_data " +
        "order by _id desc limit 5", null);
        while(cursor.moveToNext()) {
            //row 하나씩 지칭.. 지칭된 row의 column data 추출..
            ItemV0 vo = new ItemV0();
            vo._id = cursor.getInt(0);
            vo.content=cursor.getString(1);
            arrayList.add(vo);
        }
        db.close();
    }

    //최초에 한번..
    @Override
    public void onCreate() {
        //목록을 구성하기 위한 데이터 획득..
        selectDB();
    }

    //최후에 한번..
    @Override
    public void onDestroy() {

    }

    //항목 갯수 판단 위해 자동 호출..
    @Override
    public int getCount() {
        return arrayList.size();
    }

    //각 항목을 구성하기 위해서 호출..
    @Override
    public RemoteViews getViewAt(int position) {
        //각 항목을 구성해서 의뢰..
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.item_collection);
        //항목 데이터 담아주고..
        remoteViews.setTextViewText(R.id.text1, arrayList.get(position).content);
        //이벤트 처리를 위해 Receiver에서 의뢰했던 내용에 각 항목 데이터 추가
        //데이터만 담기는 빈 상태의 Intent를 만들고 Receiver가 의뢰했던
        //Intent랑 결합시키는 구조..
        Intent intent = new Intent();
        intent.putExtra("item_id", arrayList.get(position)._id);
        intent.putExtra("item_data", arrayList.get(position).content);
        //receiver에서 의뢰한 intent랑 결합..
        remoteViews.setOnClickFillInIntent(R.id.text1, intent);

        return remoteViews;
    }

    //ListView에 데이터 찍히기까지 시간이 걸리는 경우 빙빙빙 돌려줄까?
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    //기본값 0으로되어있는데 1로 해야한다.
    //각각의 항목의 view타입이 틀리냐? 그렇다면 몇개의 타입항목이 있냐 여기선 모두 동일타입
    //따라서 1개
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    //각각 항목의 식별자를 줘라
    @Override
    public long getItemId(int position) {
        return 0;
    }

    //내부적으로 ID값줄거냐
    @Override
    public boolean hasStableIds() {
        return false;
    }

    //어디선가 항목 추가/제거가 발생한 경우 notifyAppWidgetViewDataChanged함수 호출하면 아래의 함수가 자동 호출..
    @Override
    public void onDataSetChanged() {
        selectDB();
    }
}
