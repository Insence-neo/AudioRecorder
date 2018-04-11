package com.insence.audiorecorder;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Insence on 2018/4/1.
 */

public class RecordingItem implements Parcelable {
    private String mName; // file name
    private String mFilePath; //file path
    private String mTrack;
    private int mImageId;
    private int mSampleRate;
    private int mId; //id in database
    private int mLength; // length of recording in seconds
    private long mTime; // date/time of the recording

    public RecordingItem()
    {
    }

    public RecordingItem(Parcel in) {
        mName = in.readString();
        mFilePath = in.readString();
        mTrack = in.readString();
        mImageId = in.readInt();
        mSampleRate = in.readInt();
        mId = in.readInt();
        mLength = in.readInt();
        mTime = in.readLong();
    }

    public int getImageId() {
        return mImageId;
    }

    public void setImageId(int ImageId) {
        mImageId = ImageId;
    }

    public String getTrack() {
        return mTrack;
    }

    public void setTrack(String Track) {
        mTrack = Track;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void setSampleRate(int SampleRate) {
        mSampleRate = SampleRate;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    //用于实现 Parcelable序列化
    @Override
    public int describeContents() {
        return 0;
    }
    //用于实现 Parcelable序列化
    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(mId);
        dest.writeInt(mLength);
        dest.writeLong(mTime);
        dest.writeString(mFilePath);
        dest.writeString(mName);
        dest.writeString(mTrack);
        dest.writeInt(mSampleRate);
    }

    public static final Creator<RecordingItem> CREATOR = new Creator<RecordingItem>() {
        @Override
        public RecordingItem createFromParcel(Parcel in) {
            return new RecordingItem(in);
        }

        @Override
        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };

}
