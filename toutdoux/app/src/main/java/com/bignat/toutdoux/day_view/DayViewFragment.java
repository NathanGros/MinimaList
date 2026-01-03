package com.bignat.toutdoux.day_view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.bignat.toutdoux.R;

public class DayViewFragment extends Fragment {

    public DayViewFragment() {}

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_day_view, container, false);
    }
}
