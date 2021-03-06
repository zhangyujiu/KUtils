package com.kiven.kutils.activityHelper.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kiven.kutils.R;
import com.kiven.kutils.callBack.Consumer;
import com.kiven.kutils.logHelper.KShowLog;
import com.kiven.kutils.tools.KUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习文档：TODO http://blog.csdn.net/stevenhu_223/article/details/8504058
 * <p>
 * Created by kiven on 2016/11/1.
 */

public class DebugView {
    // todo 自定义选项
    private final static List<DebugEntity> customAction = new ArrayList<DebugEntity>();

    public static void addAction(@DrawableRes int resId, Consumer<Activity> callBack) {
        customAction.add(new DebugEntity(resId, callBack));
    }

    public static void addAction(@NonNull String text, Consumer<Activity> callBack) {
        customAction.add(new DebugEntity(text, callBack));
    }


    // todo 所有选项，初始化FloatView，装载数据
    private List<DebugEntity> actions = new ArrayList<DebugEntity>();

    //定义浮动窗口布局
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;

//    Button mFloatView;

    Activity activity;
    private static final String TAG = "FloatView";

    public DebugView(@NonNull Activity context) {
        this.activity = context;
        mWindowManager = context.getWindowManager();

        // todo 装载自定义选项
        actions.addAll(customAction);
        // todo 固定选项：日志，关闭
        actions.add(new DebugEntity(R.mipmap.k_ic_text_log, new Consumer<Activity>() {
            @Override
            public void callBack(Activity param) {
                new KShowLog().startActivity(activity);
            }
        }));
        actions.add(new DebugEntity(R.drawable.k_ic_close, new Consumer<Activity>() {
            @Override
            public void callBack(Activity param) {
                hideFloat();
            }
        }));

        createParams();
        createFloatView();
    }

    private void createParams() {
        wmParams = new WindowManager.LayoutParams();
        //获取的是WindowManagerImpl.CompatModeWrapper
//        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        Log.i(TAG, "mWindowManager--->" + mWindowManager);
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;// TYPE_APPLICATION 才是activity内
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
//        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.gravity = Gravity.CENTER;

        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0 - KUtil.getScreenHeight(activity) / 2;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
         /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/
    }

    private LinearLayout.LayoutParams createButtonParam(int childSize, int margin) {
        int size = childSize - 2 * margin;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, margin, margin, margin);
        return params;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createFloatView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        //获取浮动窗口视图所在布局
//        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        mFloatLayout = new LinearLayout(activity);
        //浮动窗口按钮
//        mFloatView = (Button)mFloatLayout.findViewById(R.id.float_id);

        final int childSize = KUtil.dip2px(35);
        int padding = KUtil.dip2px(2);
        for (DebugEntity action : actions) {
            if (action.isIcon()) {
                ImageView imageView = new ImageView(activity);
                imageView.setImageResource(action.getResId());
                mFloatLayout.addView(imageView, createButtonParam(childSize, padding));
            } else {
                TextView textView = new TextView(activity);
                textView.setText(action.getText());
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.BLUE);
                textView.setBackgroundResource(R.mipmap.k_bg_blank_circle);
                mFloatLayout.addView(textView, createButtonParam(childSize, padding));
            }
        }


        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动
        mFloatLayout.setOnTouchListener(new View.OnTouchListener() {

            float oldX, oldY;
            int oldX1, oldY1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        oldX = event.getRawX();
                        oldY = event.getRawY();

                        oldX1 = wmParams.x;
                        oldY1 = wmParams.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x = event.getRawX() - oldX;
                        float y = event.getRawY() - oldY;
                        if (Math.abs(x) > 1 && Math.abs(y) > 1) {
                            wmParams.x = (int) (oldX1 + x);
                            wmParams.y = (int) (oldY1 + y);
                        }
                        break;
                    default:
                        int a = KUtil.dip2px(2);
                        if (Math.abs(wmParams.x - oldX1) <= a && Math.abs(wmParams.y - oldY1) <= a) {// 认定单点击
                            int position = ((int) event.getX()) / childSize;
                            if (position >= 0 && position < actions.size()) {
                                actions.get(position).onClick(activity);
                            }
                        }
                        break;
                }

                //刷新
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);

                return true;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });

        mFloatLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(activity, "onClick", Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean isShow = false;

    public void showFloat() {
        if (!isShow) {
            //添加mFloatLayout
            mWindowManager.addView(mFloatLayout, wmParams);
            isShow = true;
        }
    }

    public void hideFloat() {
        if (isShow) {
            mWindowManager.removeView(mFloatLayout);
            isShow = false;
        }
    }

    public boolean isShow() {
        return isShow;
    }
}
