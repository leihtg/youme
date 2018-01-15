package com.youme.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.youme.R;
import com.core.contant.Contant;
import com.youme.fragment.FilePageFragment;
import com.youme.fragment.SecondFragment;
import com.youme.fragment.SpeechFragment;
import com.youme.fragment.StudyFragment;
import com.core.server.TCPSingleton;
import com.youme.view.CircleImageViewCustom;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ViewPager viewPager;

    FragmentPagerAdapter pagerAdapter;
    //云盘碎片
    private FilePageFragment filePageFragment;

    private SecondFragment secondFragment;
    //阅读碎片
    private SpeechFragment speechFragment;
    //study
    private StudyFragment studyFragment;

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

        //初始化DrawerLayout
        initDrawerLayout();

        initViewPage();

        initRadioGroup();

        /**
         * 初始化侧边栏
         */
        initListView();
        TCPSingleton.getInstance().connHandler = connectHandler;
    }

    private void initViewPage() {
        viewPager = (ViewPager) findViewById(R.id.mainViewPage);
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return selectFragment(position);
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setRadioChecked(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
                    case R.id.study:
                        index = 3;
                        break;
                    default:
                        return;
                }
                viewPager.setCurrentItem(index);
            }
        });
        //默认选第一个
        setRadioChecked(0);
    }

    private void setRadioChecked(int position) {
        RadioGroup mGroup = (RadioGroup) findViewById(R.id.main_group);
        RadioButton cutBtn = (RadioButton) mGroup.getChildAt(position);
        cutBtn.setChecked(true);
        //改变标题
        txt_title.setText(cutBtn.getText());
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
    }

    /**
     * 防止fragment重叠
     *
     * @param fragment
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (null == filePageFragment && fragment instanceof FilePageFragment) {
            filePageFragment = (FilePageFragment) fragment;
        } else if (null == secondFragment && fragment instanceof SecondFragment) {
            secondFragment = (SecondFragment) fragment;
        } else if (null == speechFragment && fragment instanceof SpeechFragment) {
            speechFragment = (SpeechFragment) fragment;
        } else if (null == studyFragment && fragment instanceof StudyFragment) {
            studyFragment = (StudyFragment) fragment;
        }

    }


    /**
     * 初始化侧边栏
     */
    public void initListView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        CircleImageViewCustom imageView = (CircleImageViewCustom) headerView.findViewById(R.id.userImage);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent intent = null;

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_about) {
            intent = new Intent(MainActivity.this, AboutSoftWareActivity.class);
        }

        if (null != intent) {
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化DrawerLayout
     */
    public void initDrawerLayout() {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
    }

    /**
     * 显示某个碎片
     *
     * @param index
     */
    private Fragment selectFragment(int index) {

        switch (index) {
            case 0:
                if (null == filePageFragment) {
                    filePageFragment = new FilePageFragment();
                }
                return filePageFragment;
            case 1:
                if (null == secondFragment) {
                    secondFragment = new SecondFragment();
                }
                return secondFragment;
            case 2:
                if (null == speechFragment) {
                    speechFragment = new SpeechFragment();
                }
                return speechFragment;
            case 3:
                if (null == studyFragment) {
                    studyFragment = new StudyFragment();
                }
                return studyFragment;
        }

        return null;
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
        if (studyFragment != null) {
            transaction.hide(studyFragment);
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
        } else {
            super.onBackPressed();
        }
//        exit();
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

    Handler connectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Contant.FIND_HOST_ADDR_MSG:
                    InetAddress addr = (InetAddress) msg.obj;
                    TCPSingleton.getInstance().hasFindHostAddress = true;
                    toast("服务器地址:" + addr.toString() + ",连接成功");
                    break;
                case Contant.FIND_HOST_ADDR_TIMEOUT:
                    toast("服务器IP查询超时");
                    break;
                case Contant.CONN_HOST_SUCC:
                    toast("连接服务器成功");
                    break;
                case Contant.CONN_HOST_FAIL:
                    toast("连接服务器失败");
                    break;
            }
        }
    };

    private void toast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}
