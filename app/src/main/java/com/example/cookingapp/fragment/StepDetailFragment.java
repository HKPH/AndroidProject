package com.example.cookingapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.cookingapp.R;


public class StepDetailFragment extends Fragment {

    private TextView textViewStepDetail;

    public StepDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_step_detail, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String value = bundle.getString("detail");
            textViewStepDetail = view.findViewById(R.id.textView_step_detail);
            textViewStepDetail.setText(value);
        }
        return view;
    }
}
