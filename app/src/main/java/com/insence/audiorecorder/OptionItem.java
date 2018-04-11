package com.insence.audiorecorder;

/**
 * Created by Insence on 2018/4/7.
 */

public class OptionItem {
    private int imageId;
    private String option;
    private String caption;

    public OptionItem(int imageId, String option,String caption) {
        this.imageId = imageId;
        this.option = option;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
