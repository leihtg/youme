package com.youme.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.anser.contant.MsgType;
import com.anser.enums.ActionType;
import com.anser.model.FileModel;
import com.anser.model.FileQueryModel_in;
import com.anser.model.FileQueryModel_out;
import com.anser.model.FileTransfer_in;
import com.anser.model.FileTransfer_out;
import com.core.server.FunCall;
import com.youme.R;
import com.youme.adapter.FileListAdapter;
import com.youme.constant.APPFinal;
import com.youme.db.DbHelper;
import com.youme.view.PullRefreshView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 云盘碎片
 */
public class FilePageFragment extends Fragment {
    private View view;
    private Context context;
    private ListView listView;//文件列表
    private String currentPath = ".";//当前文件路径默认为空
    private List<FileModel> listFile;
    private PullRefreshView pullRefreshView;
    private DbHelper dbHelper;
    boolean canEnter = true;

    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = LayoutInflater.from(getContext()).inflate(R.layout.yunpan_activity, null);
        context = view.getContext();
        dbHelper = new DbHelper(context);

        listView = (ListView) view.findViewById(R.id.dirList);
        pullRefreshView = (PullRefreshView) view.findViewById(R.id.pullRefreshView_fileList);
        pullRefreshView.setListviewPosotion(1);//第一个

        //添加监听
        listView.setOnItemClickListener(clickListener);
        pullRefreshView.setPullRefreshListener(new PullRefreshView.PullRefreshListener() {
            @Override
            public void onRefresh(final PullRefreshView view) {
                enterFolder();
            }
        });

        //加载文件列表
        initDirListView();
        //初始化菜单
        initMenuView(view);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    File pf = new File(currentPath).getParentFile();
                    if (null != pf) {
                        currentPath = pf.getPath();
                        inflateListView(dbHelper.queryFileList(currentPath));
                        return true;
                    }
                }
                return false;
            }
        });
        return view;
    }

    private void enterFolder() {
        FileQueryModel_in fi = new FileQueryModel_in();
        fi.setPath(currentPath);
        fi.setBusType(ActionType.FETCH_DIR);
        FunCall<FileQueryModel_in, FileQueryModel_out> fc = new FunCall<>();
        fc.FuncResultHandler = receiveDataHandler;
        fc.call(fi, FileQueryModel_out.class);
    }

    //listView点击事件
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            FileModel fm = listFile.get(position);
            String path = new File(currentPath, fm.getName()).getPath();
            if (fm.isDir() && canEnter) {
                currentPath = path;

                List<FileModel> list = dbHelper.queryFileList(currentPath);
                if (null == list || list.isEmpty()) {
                    canEnter = false;
                    enterFolder();
                } else {
                    inflateListView(list);
                }
            } else if (!fm.isDir()) {
                selectOption(path);
            }
        }
    };

    private void selectOption(final String path) {
        final String[] item = {"下载"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("请选择方式：");
        builder.setItems(item, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        downloadFile(path, 0);
                        break;
                }
            }


        });
        builder.create().show();
    }

    private void downloadFile(String path, long pos) {
        FileTransfer_in in = new FileTransfer_in();
        FileModel model = new FileModel();
        model.setPath(path);

        in.setModel(model);
        in.setPos(pos);
        in.setBusType(ActionType.DOWN_LOAD);

        FunCall<FileTransfer_in, FileTransfer_out> fc = new FunCall<>();
        fc.FuncResultHandler = handler;
        fc.call(in, FileTransfer_out.class);
    }

    //加载弹出框in
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
        inflateListView(dbHelper.queryFileList(currentPath));
    }

    /**
     * 填充文件列表视图
     *
     * @param files
     */
    private void inflateListView(List<FileModel> files) {
        this.listFile = files;
        //为listView设置adapter
        listView.setAdapter(new FileListAdapter(context, listFile));

        TextView tv = (TextView) view.findViewById(R.id.currentPath);
        tv.setText("当前路径:" + currentPath);
    }

    static ConcurrentHashMap<String, RandomAccessFile> map = new ConcurrentHashMap<>();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FileTransfer_out rd = (FileTransfer_out) msg.obj;
            switch (rd.msgType) {
                case MsgType.SUCC:
                    try {
                        FileModel model = rd.getModel();

                        String name = model.getName();
                        RandomAccessFile rw = map.get(name);
                        if (null == rw) {
                            File file = new File(APPFinal.appDir, name);
                            rw = new RandomAccessFile(file, "rw");
                            rw.setLength(model.getLength());
                            map.put(name, rw);
                        }
                        rw.seek(rd.getPos());
                        rw.write(rd.getBuf());
                        long pos = rd.getPos() + rd.getBuf().length;
                        if (pos == model.getLength()) {
                            rw.close();
                            map.remove(name);
                            File file = new File(APPFinal.appDir, name);
                            file.setLastModified(model.getLastModified());
                            Toast.makeText(context, new File(APPFinal.appDir, name).getAbsolutePath() + ",下载完成", Toast.LENGTH_LONG).show();
                        } else {
                            downloadFile(model.getPath(), pos);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case MsgType.ERROR:
                    Toast.makeText(context, rd.msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    Handler receiveDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FileQueryModel_out rd = (FileQueryModel_out) msg.obj;
            switch (rd.msgType) {
                case MsgType.SUCC:
                    List<FileModel> list = rd.getList();
                    canEnter = true;
                    if (null != list) {
                        inflateListView(list);
                        dbHelper.saveDb(list, currentPath);
                    }
                    break;
                case MsgType.ERROR:
                    Toast.makeText(context, rd.msg, Toast.LENGTH_SHORT).show();
                    break;
            }
            if (pullRefreshView.isRefreshing()) {
                pullRefreshView.finishRefresh();//刷新成功
            }

        }
    };
}
