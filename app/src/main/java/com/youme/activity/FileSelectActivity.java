package com.youme.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.anser.model.FileModel;
import com.youme.R;
import com.youme.adapter.FileListAdapter;
import com.youme.db.DbHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 选择需要备份的文件夹
 * Created by leihtg on 2018/11/25 20:06.
 */
public class FileSelectActivity extends AppCompatActivity implements View.OnClickListener {
    private Button fileBack;
    private Button fileOk;
    private ListView listView;
    private Stack<String> stack = new Stack<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);

        fileBack = (Button) findViewById(R.id.file_back);
        fileOk = (Button) findViewById(R.id.file_ok);
        listView = (ListView) findViewById(R.id.file_select_list);

        fileOk.setOnClickListener(this);
        fileBack.setOnClickListener(this);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();


        dbHelper = new DbHelper(this);

        adapter = new FileListAdapter(this, true);
        adapter.setSelectedFiles(dbHelper.queryAutoBakPath());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(listener);
        enterDir(path);
    }

    private boolean enterDir(String dir) {
        File file = new File(dir);
        if (!file.exists() || file.isFile()) {
            return false;
        }
        List<FileModel> list = new ArrayList<>();
        for (File f : file.listFiles()) {
            FileModel m = new FileModel();
            m.setDir(f.isDirectory());
            m.setName(f.getName());
            m.setPath(f.getAbsolutePath());
            m.setLength(f.length());
            m.setLastModified(f.lastModified());

            list.add(m);
        }
        adapter.refresh(list);
        return true;
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object tag = view.getTag();
            if (null != tag) {
                boolean b = enterDir(tag.toString());
                if (b) {
                    stack.push(tag.toString());//第一个
                }
            }
        }
    };

    FileListAdapter adapter;
    DbHelper dbHelper;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_back:
                break;
            case R.id.file_ok:
                dbHelper.saveAutoBakPath(adapter.getSelectedFiles());
                Intent data = new Intent();
                setResult(RESULT_OK, data);
                break;
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (stack.empty()) {
            super.onBackPressed();
        } else {
            String pop = stack.pop();
            enterDir(pop.substring(0, pop.lastIndexOf(File.separator)));
        }
    }
}
