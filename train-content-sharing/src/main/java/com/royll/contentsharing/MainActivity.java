package com.royll.contentsharing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.royll.contentsharing.simpledata.SendSimpleDataActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button simpleDataBtn = (Button) findViewById(R.id.sharing_simple_data);
        simpleDataBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sharing_simple_data:
                Intent it = new Intent(MainActivity.this, SendSimpleDataActivity.class);
                startActivity(it);
                break;
            default:
                break;
        }
    }
}
