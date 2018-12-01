package com.youme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.youme.R;
import com.youme.entity.FileTransfer;
import com.youme.util.FileUtil;

import java.util.List;

/**
 * 文件上传下载列表
 * <p>
 * Created by leihtg on 2018/12/1 12:11.
 */
public class FileTransferListAdapter extends BaseAdapter {
    private Context context;
    private List<FileTransfer> list;

    public FileTransferListAdapter(Context context, List<FileTransfer> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return null == list ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return null == list ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        ImageView fileIcon;
        TextView createTime;
        TextView fileSize;
        TextView fileName;
        TextView filestatus;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.file_transfer_list, parent, false);
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.fileIcon = (ImageView) view.findViewById(R.id.fileIcon);
            viewHolder.createTime = (TextView) view.findViewById(R.id.createTime);
            viewHolder.fileSize = (TextView) view.findViewById(R.id.fileSize);
            viewHolder.fileName = (TextView) view.findViewById(R.id.fileName);
            viewHolder.filestatus = (TextView) view.findViewById(R.id.file_status);

            view.setTag(viewHolder);
        }
        if (null == list || list.size() == 0) {
            return view;
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        render(holder, position);
        return view;
    }

    private void render(ViewHolder holder, int position) {
        ImageView fileIcon = holder.fileIcon;
        TextView createTime = holder.createTime;
        TextView fileSize = holder.fileSize;
        TextView fileName = holder.fileName;
        TextView filestatus = holder.filestatus;

        final FileTransfer ft = list.get(position);

        fileSize.setText(FileUtil.getSize(ft.getPos()) + "/" + FileUtil.getSize(ft.getLength()));
        createTime.setText(FileUtil.formatTime(ft.getLastModified()));
        fileName.setText(ft.getName());
        fileIcon.setImageResource(FileUtil.getImg(ft.getName()));
        filestatus.setText(ft.getFlags().getName());

        switch (ft.getFlags()) {
            case UPLOADING:
                filestatus.setText(FileUtil.getSize(ft.getPerSecondLen()) + "/s");
                break;
        }
    }

    public void updateView(int pos, ListView listView) {
        int first = listView.getFirstVisiblePosition();
        int last = listView.getLastVisiblePosition();
        if (pos >= first & pos <= last) {
            View view = listView.getChildAt(pos);
            if (null == view) {
                return;
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            render(holder, pos);
            notifyDataSetChanged();
        }
    }

    /**
     * 刷新数据
     *
     * @param list
     */
    public void refresh(List<FileTransfer> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
