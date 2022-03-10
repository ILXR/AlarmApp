package com.example.instrument.activity;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.instrument.R;
import com.example.instrument.model.SoundPoolHelper;

import java.util.Random;
import java.util.Vector;

public class GuitarActivity extends Activity {

    private static final String TAG          = "GuitarActivity";
    private static final int    string_num   = 6;
    private static final int    image_id     = new Random().nextInt();
    private static final int[]  guitarSounds = new int[]{R.raw.guitar0, R.raw.guitar1, R.raw.guitar2, R.raw.guitar3, R.raw.guitar4, R.raw.guitar5};

    private static final LinearLayout.LayoutParams linerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);

    private final SoundPoolHelper soundPoolHelper = new SoundPoolHelper(this);
    private final Vector<View>    guitarLayouts   = new Vector<>();
    private final Vector<Rect>    layoutRect      = new Vector<>();
    private final Vector<Boolean> touchFlag       = new Vector<>();


    private void playGuitar(View layout) {
        int id = layout.getId();
        ImageView img = layout.findViewById(image_id);
        if (img != null) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.string_scale);
            animation.setDuration(1000);
            animation.setInterpolator(new DecelerateInterpolator());
            img.startAnimation(animation);
            soundPoolHelper.playSounds(id);
        }
    }

    private void playGuitar(int id) {
        View layout = guitarLayouts.get(id);
        if (layout != null) {
            playGuitar(layout);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument);
        soundPoolHelper.loadSounds(guitarSounds);
        initLayout();
    }

    private void getLayoutArea() {
        if (layoutRect.size() < string_num) {
            layoutRect.clear();
            touchFlag.clear();
            for (View layout : guitarLayouts) {
                Rect area = new Rect();
                layout.getDrawingRect(area);
                int[] location = new int[2];
                layout.getLocationOnScreen(location);
                area.left = location[0];
                area.top = location[1];
                area.right = area.right + location[0];
                area.bottom = area.bottom + location[1];
                Log.i(TAG, "initLayout: " + area.toString());
                layoutRect.add(area);
                touchFlag.add(false);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getLayoutArea();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            for (int i = 0; i < string_num; i++) {
                touchFlag.set(i, false);
            }
        } else {
            int num = event.getPointerCount();
            for (int i = 0; i < num; i++) {
                float x = event.getX(i), y = event.getY(i);
                for (int j = 0; j < string_num; j++) {
                    if (layoutRect.get(j).contains((int) x, (int) y)) {
                        if (!touchFlag.get(j)) {
                            playGuitar(j);
                        }
                        touchFlag.set(j, true);
                    } else {
                        touchFlag.set(j, false);
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void initLayout() {
        LinearLayout container = findViewById(R.id.stringContainer);
        for (int i = 0; i < string_num; i++) {
            LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(linerParams);
            layout.setGravity(Gravity.CENTER);
            layout.setId(i);

            ImageView img = new ImageView(this);
            img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5 + i * 2));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setImageResource(R.drawable.string);
            img.setId(image_id);

            layout.addView(img);
            container.addView(layout);
            guitarLayouts.add(layout);
        }
    }
}
