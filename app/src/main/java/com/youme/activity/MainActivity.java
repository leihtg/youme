package com.youme.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.youme.R;
import com.youme.fragment.FilePageFragment;
import com.youme.fragment.SecondFragment;
import com.youme.fragment.SpeechFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //云盘碎片
    private FilePageFragment filePageFragment;

    private SecondFragment secondFragment;
    //阅读碎片
    private SpeechFragment speechFragment;
    //碎片管理Manager
    private FragmentManager fragmentManager;
    //标题栏
    private Toolbar toolbar;
    //标题栏中的标题
    private TextView txt_title;
    //标题栏中的下拉列表
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化toolBar
        initToolBar();

        initRadioGroup();

        //初始化DrawerLayout
        initDrawerLayout();

        /**
         * 初始化侧边栏
         */
        initListView();
    }

    /**
     * 初始化radioGroup
     */
    private void initRadioGroup() {
        RadioGroup mGroup = (RadioGroup) findViewById(R.id.main_group);
        //添加监听
        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int index = 0;
                switch (checkedId) {
                    case R.id.yunPan:
                        index = 0;
                        break;
                    case R.id.trade:
                        index = 1;
                        break;
                    case R.id.more:
                        index = 2;
                        break;
                    default:
                        return;
                }
                setTabSelection(index);
            }
        });
        //默认选第一个
        ((RadioButton) mGroup.getChildAt(0)).setChecked(true);
    }

    /**
     * 初始化toolBar
     */
    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //设置toolbar不显示标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //获得toolbar中自定义的标题控件对象
        txt_title = (TextView) toolbar.findViewById(R.id.txt_toolbar_title);
        spinner = (Spinner) toolbar.findViewById(R.id.spinner_toolbar);

        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        Map<String, String> map1 = new HashMap<>();
        map.put("name", "tom");
        map1.put("name", "kitty");
        data.add(map);
        data.add(map1);
        SimpleAdapter spinnerAdapter = new SimpleAdapter(this, data, R.layout.spinner_item, new String[]{"name"}, new int[]{R.id.tv_main_spinner_title});
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 初始化侧边栏
     */
    public void initListView() {
        ListView listView = (ListView) findViewById(R.id.listview_main_nav);
        //头部布局控件绑定的赋值
        View headView = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
        TextView tv_userName = (TextView) headView.findViewById(R.id.tv_nav_userName);
        TextView tv_userAccountName = (TextView) headView.findViewById(R.id.tv_nav_accountName);

        tv_userName.setText("齐天大圣");
        tv_userAccountName.setText("老大");

        listView.addHeaderView(headView);
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        Map<String, String> map1 = new HashMap<>();
        map.put("name", "tom");
        map1.put("name", "kitty");
        data.add(map);
        data.add(map1);
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.spinner_item, new String[]{"name"}, new int[]{R.id.tv_main_spinner_title});

        listView.setAdapter(adapter);
    }

    /**
     * 初始化DrawerLayout
     */
    public void initDrawerLayout() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
    }

    /**
     * 显示某个碎片
     *
     * @param index
     */
    private void setTabSelection(int index) {
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //全部隐藏
        hideFragment(transaction);

        //改变标题
        actionbarTitleChange(index);

        switch (index) {
            case 0:
                if (null == filePageFragment) {
                    filePageFragment = new FilePageFragment();
                    transaction.add(R.id.realtabcontent, filePageFragment);
                } else {
                    transaction.show(filePageFragment);
                }
                break;
            case 1:
                if (null == secondFragment) {
                    secondFragment = new SecondFragment();
                    transaction.add(R.id.realtabcontent, secondFragment);
                } else {
                    transaction.show(secondFragment);
                }
                break;
            case 2:
                if (null == speechFragment) {
                    speechFragment = new SpeechFragment();
                    transaction.add(R.id.realtabcontent, speechFragment);
                } else {
                    transaction.show(speechFragment);
                }
                break;
        }

        transaction.commit();
    }

    /**
     * 改变actionBar中控件信息
     *
     * @param index
     */
    private void actionbarTitleChange(int index) {
        spinner.setVisibility(View.GONE);
        txt_title.setVisibility(View.VISIBLE);

        switch (index) {
            case 0://首页
                txt_title.setText("首页");
                break;
            case 1://行情
                txt_title.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
                break;
            case 2://自选
                txt_title.setText("读取");
                break;
        }
    }

    /**
     * 隐藏碎片
     *
     * @param transaction
     */
    private void hideFragment(FragmentTransaction transaction) {
        if (filePageFragment != null) {
            transaction.hide(filePageFragment);
        }
        if (secondFragment != null) {
            transaction.hide(secondFragment);
        }
        if (speechFragment != null) {
            transaction.hide(speechFragment);
        }
    }

    /**
     * 重写返回按键事件
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
//        exit();
        super.onBackPressed();
    }

    boolean isExit = false;

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            backHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    Handler backHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };
}



