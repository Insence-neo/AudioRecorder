package com.insence.audiorecorder.Listener;

/**
 * Created by Insence on 2018/4/1.
 */

public interface OnDatabaseChangedListener {
    void onNewDatabaseEntryAdded();
    void onDatabaseEntryRenamed();
}
