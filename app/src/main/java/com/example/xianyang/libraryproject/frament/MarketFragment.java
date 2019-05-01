package com.example.xianyang.libraryproject.frament;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.xianyang.libraryproject.R;

public class MarketFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.market_activity_fragment,container,false);
        Log.d("market", "onCreateView: "+getActivity().getIntent().getStringExtra("notification"));
        return view;
    }
}
