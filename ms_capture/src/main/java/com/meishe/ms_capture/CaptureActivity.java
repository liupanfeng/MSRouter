package com.meishe.ms_capture;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.meishe.ms_annotation.MSRouter;
import com.meishe.ms_common.MSRouterManager;

@MSRouter(path="/ms_capture/CaptureActivity")
public class CaptureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
    }

    public void toVideoEdit(View view) {
        MSRouterManager.getInstance().build("/ms_video_edit/VideoEditActivity").
                withString("name","唐三")
                .navigation(this);

    }
}