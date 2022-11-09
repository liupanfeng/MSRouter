package com.meishe.ms_video_edit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.meishe.ms_annotation.MSParameter;
import com.meishe.ms_annotation.MSRouter;

@MSRouter(path="/ms_video_edit/VideoEditActivity")
public class VideoEditActivity extends AppCompatActivity {

    @MSParameter
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);


    }
}