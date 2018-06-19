package com.example.ch2_material;


import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class OneFragment extends Fragment {

    RecyclerView recyclerView;

    public OneFragment() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        recyclerView=(RecyclerView)inflater.inflate(R.layout.fragment_one, container, false);

        //항목 구성 가상 데이터..
        List<String> list = new ArrayList<>();

        for(int i = 0; i < 20; i++) {
            list.add("item : "+i);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new MyAdapter(list));
        recyclerView.addItemDecoration(new MyItemDecoration());
        return recyclerView;
    }

    //findViewById 성능이슈.. view를 find 해주는 역할..
    //holder 자체를 adapter에서 저장 재사용 해줌으로 한번만 find하게 된다.
    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView;
        public MyViewHolder(View root) {
            super(root);
            titleView = root.findViewById(android.R.id.text1);
        }
    }

    //항목 구성. adapter...
    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        List<String> list; //항목구성 집합 데이터..
        public MyAdapter(List<String> list) {
            this.list = list;
        }

        //항목을 위한 layout xml 초기화 및 find를 대행하는 holder 지정..
        //이곳에서 리턴시킨 holder를 내부적으로 메모리에 유지했다가 재사용하게
        //연결해줌으로.. holder 내에서 find가 한번만 되게 된다..
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new MyViewHolder(root);
        }

        //항목 하나를 구성하기 위해서 호출... ListView의 getView에 해당되는 함수
        //매개변수 viewHolder가 onCreateViewHolder에서 리턴시켰던 객체..
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.titleView.setText(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class MyItemDecoration extends RecyclerView.ItemDecoration {

        //모든 항목이 찍힌후 마지막에 호출..
        //항목위에 무언가를 그리기 위해서..
        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
            //이미지 그리는 좌표값 계산..
            //view 사이즈 판단..
            int width = parent.getWidth();
            int height = parent.getHeight();

            //이미지 사이즈
            Drawable dr = getActivity().getResources().getDrawable(R.drawable.kbo);
            int drWidth = dr.getIntrinsicWidth();
            int drHeight = dr.getIntrinsicHeight();

            int left = width/2 - drWidth/2;
            int top = height/2 - drHeight/2;

            //이미지 그리기..
            c.drawBitmap(BitmapFactory.decodeResource(
                    getActivity().getResources(), R.drawable.kbo), left, top, null);
        }

        //항목 하나를 꾸미기 위해서 호출..
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //항목의 index 값 획득..
            int index = parent.getChildAdapterPosition(view)+1;
            if(index%3 == 0) {
                outRect.set(20,20,20,60);
            } else {
                outRect.set(20,2,20,20);
            }

            view.setBackgroundColor(0xFFECE9E9);
            ViewCompat.setElevation(view, 20.0f);
        }
    }
}
