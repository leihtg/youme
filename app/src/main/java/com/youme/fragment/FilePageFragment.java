package com.youme.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.youme.R;
import com.youme.view.PullRefreshView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 云盘碎片
 */
public class FilePageFragment extends Fragment {
    private View view;
    private Context context;
    private ListView listView;//文件列表
    private File currentParent;//当前文件路径
    private File sdCardDir;//根目录
    private File[] currentFiles;
    private PullRefreshView pullRefreshView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.yunpan_activity, null);
        context = view.getContext();
        listView = (ListView) view.findViewById(R.id.dirList);
        pullRefreshView = (PullRefreshView) view.findViewById(R.id.pullRefreshView_fileList);
        pullRefreshView.setListviewPosotion(1);//第一个

        //添加监听
        pullRefreshView.setPullRefreshListener(new PullRefreshView.PullRefreshListener() {
            @Override
            public void onRefresh(final PullRefreshView view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.finishRefresh();
                        Toast.makeText(context, "刷新成功", Toast.LENGTH_LONG).show();
                    }
                },3000);
            }
        });
        listView.setOnItemClickListener(clickListener);

        //加载文件列表
        initDirListView();
        //初始化菜单
        initMenuView(view);

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (!sdCardDir.equals(currentParent)) {
                        currentParent = currentParent.getParentFile();
                        currentFiles = currentParent.listFiles();
                        inflateListView(currentFiles);
                    }
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    //listView点击事件
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (currentFiles[position].isFile()) {
                return;//是文件不处理
            }
            File[] tmp = currentFiles[position].listFiles();
            if (null == tmp || tmp.length == 0) {
                Toast.makeText(context, "当前路径不可访问或该路径下没有文件", Toast.LENGTH_LONG).show();
            } else {
                currentParent = currentFiles[position];
                currentFiles = tmp;
                //再次更新视图
                inflateListView(currentFiles);
            }


        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //加载弹出框
    private void initMenuView(View view) {
        final PopupMenu popupMenu = new PopupMenu(getActivity(), view.findViewById(R.id.fenLei));
        getActivity().getMenuInflater().inflate(R.menu.file_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    default:
                        popupMenu.dismiss();
                }
                return true;
            }
        });
        view.findViewById(R.id.fenLei).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    private void initDirListView() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //没有sd卡或没有访问权限
            return;
        }
        //获取SD卡目录
        sdCardDir = Environment.getExternalStorageDirectory();
        if (sdCardDir.exists()) {
            currentParent = sdCardDir;
            currentFiles = currentParent.listFiles();
            inflateListView(currentFiles);
        }
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 填充文件列表视图
     *
     * @param files
     */
    private void inflateListView(File[] files) {
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (File f : files) {
            Map<String, Object> item = new HashMap<>();
            if (f.isDirectory()) {
                item.put("fileIcon", R.mipmap.folder);
            } else {
                item.put("fileIcon", R.mipmap.file);
            }
            item.put("fileName", f.getName());
            item.put("createTime", sdf.format(new Date(f.lastModified())));

            listItems.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(context, listItems, R.layout.dir_item,
                new String[]{"fileIcon", "fileName", "createTime"}, new int[]{R.id.fileIcon, R.id.fileName, R.id.createTime});
        //为listView设置adapter
        listView.setAdapter(adapter);

        TextView tv = (TextView) view.findViewById(R.id.currentPath);
        try {
            tv.setText("当前路径:" + currentParent.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
