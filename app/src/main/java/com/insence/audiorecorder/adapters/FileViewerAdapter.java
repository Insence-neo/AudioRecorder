package com.insence.audiorecorder.adapters;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.insence.audiorecorder.FileViewerItem;
import com.insence.audiorecorder.Helper.DBHelper;
import com.insence.audiorecorder.Listener.OnDatabaseChangedListener;
import com.insence.audiorecorder.OptionItem;
import com.insence.audiorecorder.R;
import com.insence.audiorecorder.RecordingItem;
import com.insence.audiorecorder.Services.PlayService;
import com.insence.audiorecorder.activities.MainActivity;
import com.insence.audiorecorder.libs.FillSeekBar;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Insence on 2018/4/1.
 */

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
    implements OnDatabaseChangedListener{

    private static final String LOG_TAG = "FileViewerAdapter";
    private MediaPlayer mMediaPlayer = null;
    private DBHelper mDatabase;
    RecordingItem item;
    Context mContext;
    LinearLayoutManager llm;



    //构造函数 用于接收数据集合
    public FileViewerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        llm = linearLayoutManager;
    }

    //ViewHolder 封装了 控件实例    Serializable 实现序列化
    public class RecordingsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageState;
        ImageView imageQuality;
        TextView vName;
        TextView vLength;
        TextView vDateAdded;
        View cardView;
        TextView playProgress;
        FillSeekBar fillSeekBar;
        RecordingsViewHolder(View itemView) {
            super(itemView);
            imageState = itemView.findViewById(R.id.image_state);
            imageQuality = itemView.findViewById(R.id.image_quality);
            vName = itemView.findViewById(R.id.file_name_text);
            vLength = itemView.findViewById(R.id.file_length_text);
            vDateAdded = itemView.findViewById(R.id.file_date_added_text);
            cardView = itemView.findViewById(R.id.card_view);
            playProgress = itemView.findViewById(R.id.play_progress_text);
            fillSeekBar = itemView.findViewById(R.id.FillSeekBar);
        }
    }

    //返回ViewHolder 实现了ViewHolder的复用和回收
    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //填充view 完成ViewHolder构造
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.cardview, parent, false);
        //装饰为一个viewHolder
        RecordingsViewHolder viewHolder = new RecordingsViewHolder(itemView);
        mContext = parent.getContext();
        return viewHolder;
    }

    //通过ViewHolder内封装的控件实例 对view 进行数据渲染
    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, final int position) {

        //通过getItem方法 获取实例
        item = getItem(position);
        long itemDuration = item.getLength();
        //计算分秒
        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);

        holder.imageQuality.setImageResource(item.getImageId());
        holder.vName.setText(item.getName());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vDateAdded.setText(
                DateUtils.formatDateTime(
                        mContext,
                        item.getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
                )
        );

        //click to play and pause
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onClick(position,holder,getItem(holder.getPosition()));
            }
        });

        //longClick to dialog
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                List<FileViewerItem> items = new ArrayList<>();
                items.add(new FileViewerItem(R.drawable.ic_share, mContext.getString(R.string.dialog_file_share)));
                items.add(new FileViewerItem(R.drawable.ic_rename, mContext.getString(R.string.dialog_file_rename)));
                items.add(new FileViewerItem(R.drawable.ic_delete, mContext.getString(R.string.dialog_file_delete)));
                items.add(new FileViewerItem(R.drawable.ic_details, mContext.getString(R.string.dialog_file_detail)));
                FileViewerItemAdapter adapter = new FileViewerItemAdapter(items,mContext);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                shareFileDialog(holder.getPosition());
                                break;
                            case 1:
                                renameFileDialog(holder.getPosition());
                                break;
                            case 2:
                                deleteFileDialog(holder.getPosition());
                                break;
                        }
                    }
                });
//                builder.setItems(items, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int item) {
//                        if (item == 0) {
//                            shareFileDialog(holder.getPosition());
//                        } if (item == 1) {
//                            renameFileDialog(holder.getPosition());
//                        } else if (item == 2) {
//                            deleteFileDialog(holder.getPosition());
//                        }
//                    }
//                });
                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });
    }

    //返回项目数
    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    //拿到item实例
    public RecordingItem getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    //TODO
    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }

    //实现监听接口的方法 实现动画同步
    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    //TODO
    @Override
    public void onDatabaseEntryRenamed() {

    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage

        //delete file from storage
        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(mContext, String.format(
                mContext.getString(R.string.toast_file_delete),
                        getItem(position).getName()
                ),
                Toast.LENGTH_SHORT
        ).show();

        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }

    public void rename(int position, String name) {
        //rename a file

        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/AudioRecorder/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(mContext,
                    String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();

        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    public void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        File file = new File(getItem(position).getFilePath());
        Uri uri;
        //解决 7.0 文件读取的权限 使用内容提供者解决 需要要在Manifest.xml中添加provider
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(mContext,"com.insence.audiorecorder.provider",file);
        } else {
            uri = Uri.fromFile(file);
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
        shareIntent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
    }

    public void renameFileDialog (final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim() + ".wav";
                            rename(position, value);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    public void deleteFileDialog (final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            remove(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }




    OnItemClickListener mOnItemClickListener = null;
    public interface OnItemClickListener{
        void onClick(int position,RecordingsViewHolder holder,RecordingItem item);
        //void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener ){
        this. mOnItemClickListener=onItemClickListener;
    }
}
