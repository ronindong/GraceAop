package com.ronin.grace;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ronin.grace.annotation.Cost;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Cost
    private void test() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }
}
