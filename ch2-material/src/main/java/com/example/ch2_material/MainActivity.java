package com.example.ch2_material;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

//유저에 의해 tab button 조정되는 순간의 이벤트..
//tab button이 조정되는 순간 개발자 코드로 viewpager 조정하려고..
//viewpager 조정시 tab button 조정은 TabLayout이 자동으로..
//반대로는 개발자가 직접..
public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, View.OnClickListener{

    ViewPager viewPager;
    Toolbar toolbar;

    //Navigation drawer...
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    boolean isOpened;

    //appbar....
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton fab;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewpager);

        setSupportActionBar(toolbar);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        //Navigation drawer...
        //유저에 의한 drawer open, close 이벤트 처리가 필요할때만 하위 클래스 정의
        //생성자 매개변수의 리소스 문자열은 화면 출력과 상관없다... ARIA 차원에서
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                isOpened = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                isOpened = false;
            }
        };
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle.syncState(); // drawer와 toggle의 동기화를 위해...

        //TabLayout...
        TabLayout tabLayout=findViewById(R.id.tabs);

        //viewPager 연결만으로.. viewpager 조정시 tab button이 자동 조정된다.
        //tab button 문자열을 직접 이곳에서 TabLayout에 추가해도 되고..
        //ViewPager와 연결되어 있다면.. viewpager의 page title 문자열을
        //자동으로 tab button 문자열로..
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);

        //appbar....
        collapsingToolbarLayout=findViewById(R.id.collapsing);
        fab=findViewById(R.id.fab);
        coordinatorLayout=findViewById(R.id.coordinator);

        collapsingToolbarLayout.setTitle("AppBar Title");
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Snackbar.make(coordinatorLayout, "I am Snackbar", Snackbar.LENGTH_SHORT)
                .setAction("MoreAction", new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Log.d("hyunwoo", "more action click......");
                    }
                }).show();
    }

    //menu 이벤트 처리 함수..
    //toggle 버튼도 menu 이벤트로 처리가 되서..
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //drawer가 open된 상태에서 유저가 폰의 back button을 누른다면???
    //==> back button에 의해 drawer가 close 되게 처리하려고..
    //back button 이벤트 처리..

    @Override
    public void onBackPressed() {
        if(isOpened) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //ViewPager는 AdapterView이다.. ViewPager를 위한 Adapter..
    //ViewPager의 항목이 일반 View라면 PagerAdapter...
    //Fragment라면 FragmentTransaction(remove 등)의한 제어가 구현되어 있는
    //FramgentStatePagerAdapter 상속으로...
    //Fragment 직접 제어안해도 된다 위의 fspa를 쓰면 얘가 알아서 관리해줌
    //Add, Remove, Replace등 fragment의 라이프 사이클 관리를 따로 해준다.
    class MyPagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> fragments = new ArrayList<>();

        //viewpager의 title 문자열..
        String[] titles = new String[]{"TAB1", "TAB2", "TAB3"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm); //항위 클래스에 FragmentManager 전달.. transaction으로 fragment제어해준다..
            fragments.add(new OneFragment());
            fragments.add(new TwoFragment());
            fragments.add(new ThreeFragment());
        }

        //항목 갯수를 판단하기 위해 자동 호출... 모든 어댑터에 있지않을까
        @Override
        public int getCount() {
            return fragments.size();
        }

        //항목을 결정하기 위해 자동 호출.
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
        //PagerTitleStrip, PagerTabStrip, TabLayout을 위한 title 문자열
        //결정을 위해 자동 호출..
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        //유저가 tab button 조정시 viewpager 화면 조정..
        //눌린 탭버튼의 위치값을 가지고 조정..
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
