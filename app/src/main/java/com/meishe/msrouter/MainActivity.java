package com.meishe.msrouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.meishe.ms_annotation.MSRouter;
import com.meishe.ms_common.MSRouterManager;

@MSRouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startCapture(View view) {
        MSRouterManager.getInstance().build("/ms_capture/CaptureActivity")
                .navigation(this);
    }
}