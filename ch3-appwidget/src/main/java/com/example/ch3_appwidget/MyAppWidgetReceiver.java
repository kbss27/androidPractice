package com.example.ch3_appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

//BroadcastReceiver 서브 클래스인 AppWidgetProvider를 상속받아서..
public class MyAppWidgetReceiver extends AppWidgetProvider {

    //provider의 lifecycle 함수
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        //의뢰서..
        //어느 앱의 의뢰서인지, 어느 layout xml을 이용하는 의뢰인지 명시..
        //package명으로 구분 이 앱의 의뢰서인데 어느 layout xml을 건드리는 의뢰다.
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.my_collection_widget);

        //Factory(Adapter)를 획득하기 위해서 실행시켜야 하는 Service 위한
        //Intent의뢰..
        //Intent의뢰시 Activity, Receiver는 부가정보를 담기 위해 꼭
        //PendingIntent로 의뢰..
        //Service Intent 의뢰는 부가정보가 없어서 그냥 Intent로 의뢰..
        Intent sintent = new Intent(context, MyRemoteViewsService.class);
        remoteViews.setRemoteAdapter(R.id.list, sintent);

        //항목 선택 이벤트 처리 Intent..
        //이곳에서는 어떤 Component(DetailActivity)가 실행되어야 한다는
        //정보만 의뢰서에 담기고..
        //Factory에서 그 의뢰내용에 항목 데이터를 추가해 주는 구조..
        //위의 두근데가 잘맞아야 이벤트가 된다.
        Intent aIntent = new Intent(context, DetailActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, aIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.list, pIntent);

        //의뢰서 launcher app에 전달..
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    }

    //BR의 lifecycle 함수
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        super.onReceive(context, intent);

        //우리의 경우 MainActivity에서 일정이 등록된다..
        //신규 일정이 등록되는 경우 appwidget이 update 되어야 한다..
        if(intent.getStringExtra("mode") != null) {
            //MainActivity에서 appwidget update 목적으로 receiver를 실행시킨 경우
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int ids[] = manager.getAppWidgetIds(new ComponentName(context, MyAppWidgetReceiver.class));

            //목록 update는 이곳에서 못한다.. Factory가 작업해야 하는 거다..
            //이곳에서는 notify 명령만 내린다..
            //아래의 함수가 호출되면.. launcher app에서는 자신에게 전달된
            //Factory의 onDataSetChanged 함수를 자동으로 호출한다..

            //notify명령만 날라오면 launcher 쪽에서 원래 자동으로 호출해줘야한다.
            manager.notifyAppWidgetViewDataChanged(ids, R.id.list);
        }
    }
}
