package com.youme.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.youme.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Thinkpad on 2018/1/13.
 */
public class StudyFragment extends Fragment {
    private static final int X_OFFSET = 5;
    private static final int HEIGHT = 320;
    private static final float WIDTH = 960;
    //实际的Y轴的位置
    private int centerY = HEIGHT / 2;
    private int cx = X_OFFSET;
    Timer timer = new Timer();
    TimerTask task = null;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_study, null);

        useSurfaceView(view);
        return view;
    }

    SurfaceHolder holder;
    Paint paint;

    private void useSurfaceView(View view) {
        SurfaceView surface = (SurfaceView) view.findViewById(R.id.show);
        holder = surface.getHolder();
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);

        Button sin = (Button) view.findViewById(R.id.sin);
        Button cos = (Button) view.findViewById(R.id.cos);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                drawBack(holder);
                cx = X_OFFSET;
                if (task != null) {
                    task.cancel();
                }
                task = new TimerTask() {
                    @Override
                    public void run() {
                        int cy = v.getId();
                        if (cy == R.id.sin) {
                            cy = centerY - (int) (100 * Math.sin((cx - 5) * 2 * Math.PI / 150));
                        } else {
                            cy = centerY - (int) (100 * Math.cos((cx - 5) * 2 * Math.PI / 150));
                        }
                        Canvas canvas = holder.lockCanvas(new Rect(cx, cy - 2, cx + 2, cy + 2));
                        canvas.drawPoint(cx, cy, paint);
                        cx++;
                        if (cx > WIDTH) {
                            task.cancel();
                            task = null;
                        }
                        holder.unlockCanvasAndPost(canvas);
                    }
                };
                timer.schedule(task, 0, 10);
            }
        };
        sin.setOnClickListener(listener);
        cos.setOnClickListener(listener);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                drawBack(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                timer.cancel();
            }
        });


    }

    private void drawBack(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        //绘制白色背景
        canvas.drawColor(Color.WHITE);
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStrokeWidth(2);
        //绘制坐标轴
        canvas.drawLine(X_OFFSET, centerY, WIDTH, centerY, p);
        canvas.drawLine(X_OFFSET, 40, X_OFFSET, HEIGHT, p);

        holder.unlockCanvasAndPost(canvas);

        holder.lockCanvas(new Rect(0, 0, 0, 0));
        holder.unlockCanvasAndPost(canvas);
    }
}
