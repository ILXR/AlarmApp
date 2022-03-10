package com.example.instrument.bean;

public class InstrumentItem {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    private       String   name;
    private       int      imgId;
    private final Class<?> activity;

    public Class<?> getActivity() {
        return activity;
    }


    public InstrumentItem(String name, int imgId, Class<?> activity) {
        this.name = name;
        this.imgId = imgId;
        this.activity = activity;
    }

}
