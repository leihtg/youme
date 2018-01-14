package com.youme.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

import com.youme.R;
import com.youme.contant.Contant;
import com.youme.contant.FileModel;
import com.youme.contant.FileParam;
import com.youme.server.ClientThread;
import com.youme.view.PullRefreshView;

import java.io.File;
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
    private String currentPath = "";//当前文件路径默认为空
    private File[] currentFiles;
    private PullRefreshView pullRefreshView;
    ClientThread clientThread;

    //向服务器查询数据
    Message msg;

    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = LayoutInflater.from(getContext()).inflate(R.layout.yunpan_activity, null);
        context = view.getContext();

        listView = (ListView) view.findViewById(R.id.dirList);
        pullRefreshView = (PullRefreshView) view.findViewById(R.id.pullRefreshView_fileList);
        pullRefreshView.setListviewPosotion(1);//第一个

        //添加监听
        pullRefreshView.setPullRefreshListener(new PullRefreshView.PullRefreshListener() {
            @Override
            public void onRefresh(final PullRefreshView view) {
                pullRefreshView.finishRefresh();//刷新成功
                FileParam fp = new FileParam();
                fp.setMsgType(Contant.FETCH_DIR);
                fp.setPath(currentPath);

                msg = new Message();
                msg.obj = fp;

                clientThread = new ClientThread(fileHandler);
                new Thread(clientThread).start();
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

                    return true;
                }
                return false;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    //listView点击事件
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

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
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 填充文件列表视图
     *
     * @param files
     */
    private void inflateListView(List<FileModel> files) {
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (FileModel f : files) {
            Map<String, Object> item = new HashMap<>();
            if (f.isDir()) {
                item.put("fileIcon", R.mipmap.folder);
            } else {
                item.put("fileIcon", R.mipmap.file);
            }
            item.put("fileName", f.getName());
            item.put("createTime", sdf.format(new Date(f.getLastModified())));

            listItems.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(context, listItems, R.layout.dir_item,
                new String[]{"fileIcon", "fileName", "createTime"}, new int[]{R.id.fileIcon, R.id.fileName, R.id.createTime});
        //为listView设置adapter
        listView.setAdapter(adapter);

        TextView tv = (TextView) view.findViewById(R.id.currentPath);
        tv.setText("当前路径:" + currentPath);
    }

    Handler fileHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    List<FileModel> list = (List<FileModel>) msg.obj;
                    if (null != list) {
                        inflateListView(list);
                    }
                    break;
                case 1://连接服务器成功
                    if (null != msg)
                        clientThread.recvHandler.sendMessage(msg);
                    msg = null;//置空表明已处理


            }
            super.handleMessage(msg);
        }
    };
}
