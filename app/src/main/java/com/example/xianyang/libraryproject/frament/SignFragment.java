package com.example.xianyang.libraryproject.frament;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.xianyang.libraryproject.R;

public class SignFragment extends Fragment {
    private ImageButton imageButton;
    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.sign_fragment,container,false);
        imageButton=view.findViewById(R.id.sign_bt);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageButton.setBackgroundResource(R.drawable.sign_ago);
            }
        });
        return view;
    }
}
