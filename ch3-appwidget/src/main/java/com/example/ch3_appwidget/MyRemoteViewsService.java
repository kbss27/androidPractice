package com.example.ch3_appwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

//Factory 획득 목적으로 launcher app에서 실행..
public class MyRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MyRemoteViewsFactory(getApplicationContext());
    }
}
