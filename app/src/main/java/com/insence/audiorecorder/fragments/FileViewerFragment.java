package com.insence.audiorecorder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.insence.audiorecorder.R;
import com.insence.audiorecorder.adapters.FragmentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Insence on 2018/3/29.
 */

public class FileViewerFragment extends Fragment {
    private static final String ARG_POSITION = "position";
//    private List<Fruit> fruitList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_viewer, container, false);


//        //填充recyclerView
//        initFruits();
//        FragmentAdapter adapter = new FragmentAdapter(fruitList);
//        RecyclerView recyclerView = view.findViewById(R.id.rlv);
//        //初始化布局管理器 默认是纵向
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        //设置为横向
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        //设置布局管理器
//        recyclerView.setLayoutManager(layoutManager);
//        //设置适配器
//        recyclerView.setAdapter(adapter);
        return view;
    }

    public static Fragment newInstance(int position) {
        FileViewerFragment fragment = new FileViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_POSITION,position);
        fragment.setArguments(bundle);
        return fragment;
    }

//    private void initFruits() {
//        for (int i = 0; i < 2; i++) {
//            Fruit apple = new Fruit("Apple", R.drawable.apple_pic);
//            fruitList.add(apple);
//            Fruit banana = new Fruit("Banana", R.drawable.banana_pic);
//            fruitList.add(banana);
//            Fruit orange = new Fruit("Orange", R.drawable.orange_pic);
//            fruitList.add(orange);
//            Fruit watermelon = new Fruit("Watermelon", R.drawable.watermelon_pic);
//            fruitList.add(watermelon);
//            Fruit pear = new Fruit("Pear", R.drawable.pear_pic);
//            fruitList.add(pear);
//            Fruit grape = new Fruit("Grape", R.drawable.grape_pic);
//            fruitList.add(grape);
//            Fruit pineapple = new Fruit("Pineapple", R.drawable.pineapple_pic);
//            fruitList.add(pineapple);
//            Fruit strawberry = new Fruit("Strawberry", R.drawable.strawberry_pic);
//            fruitList.add(strawberry);
//            Fruit cherry = new Fruit("Cherry", R.drawable.cherry_pic);
//            fruitList.add(cherry);
//            Fruit mango = new Fruit("Mango", R.drawable.mango_pic);
//            fruitList.add(mango);
//        }
//    }
}
