package com.royll.graphicsanimation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mLoadBigBitMapBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoadBigBitMapBtn = (Button) findViewById(R.id.load_large_bitmap_btn);
        mLoadBigBitMapBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_large_bitmap_btn:
                Intent it=new Intent(MainActivity.this,LoadLargeBitmapActivty.class);
                startActivity(it);
                break;
        }
    }
}
