package com.example.xianyang.libraryproject.frament;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.xianyang.libraryproject.FristActivity;
import com.example.xianyang.libraryproject.R;
import com.example.xianyang.libraryproject.books.Lend_book;

public class BooksFragment extends Fragment {
    private ImageButton lend_book;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.books_fragment,container,false);
        lend_book=view.findViewById(R.id.lend_book);
        lend_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),Lend_book.class);
                getActivity().startActivity(intent);
            }
        });
        return view;
    }
}
