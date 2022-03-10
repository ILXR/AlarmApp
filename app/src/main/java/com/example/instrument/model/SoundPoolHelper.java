package com.example.instrument.model;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;

public class SoundPoolHelper {
    private static final String TAG      = "SoundPoolHelper";
    private final static int    priority = 1;
    private final static float  volumn   = 1f;
    private final static float  rate     = 1f;
    private final static int    loop     = 0;

    private final Context   context;
    private       SoundPool soundPool;

    private final AudioAttributes           audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)// 设置音效的类型
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
    private final HashMap<Integer, Integer> soundMap        = new HashMap<>();


    private void init() {
        soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes) // 设置音效池的属性
                .setMaxStreams(10) // 设置最多可容纳10个音频流
                .build();
    }

    public SoundPoolHelper(Context context) {
        this.context = context;
        init();
    }

    public SoundPoolHelper(Context context, int[] sounds) {
        this.context = context;
        init();
        loadSounds(sounds);
    }

    public void loadSounds(int[] sounds) {
        for (Integer id : soundMap.values()) {
            if (id != null)
                soundPool.unload(id);
        }
        soundMap.clear();
        for (int i = 0; i < sounds.length; i++) {
            soundMap.put(i, soundPool.load(context, sounds[i], priority));
        }
    }

    public boolean playSounds(int index) {
        Integer id = soundMap.get(index);
        if (id != null) {
            soundPool.play(id, volumn, volumn, priority, loop, rate);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        soundMap.clear();
        soundPool.release();
    }
}
