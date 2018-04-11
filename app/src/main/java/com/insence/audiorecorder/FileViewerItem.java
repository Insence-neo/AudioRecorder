package com.insence.audiorecorder;

/**
 * Created by Insence on 2018/4/9.
 */

public class FileViewerItem {
    private int imageId;
    private String option;

    public FileViewerItem(int imageId, String option) {
        this.imageId = imageId;
        this.option = option;
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
