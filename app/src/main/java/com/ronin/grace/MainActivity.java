package com.ronin.grace;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ronin.grace.annotation.Cost;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Cost
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Cost
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Cost
    @Override
    protected void onResume() {
        super.onResume();
    }
}
