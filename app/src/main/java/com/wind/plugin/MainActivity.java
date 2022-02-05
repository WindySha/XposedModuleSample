package com.wind.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.wind.plugin.sample.R;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("injected_plugin");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.sample_text);
        tv.setText("Hello World");
    }
}