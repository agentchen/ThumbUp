package com.sunday.thumbup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ThumbUpView thumbUpView = (ThumbUpView) findViewById(R.id.thumb_up);
        thumbUpView.setCount(1023);
    }
}
