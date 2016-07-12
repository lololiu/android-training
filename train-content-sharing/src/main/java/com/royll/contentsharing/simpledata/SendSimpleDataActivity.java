package com.royll.contentsharing.simpledata;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.royll.contentsharing.R;

import java.util.ArrayList;

/**
 * Created by Roy on 2016/7/12.
 * desc:
 */
public class SendSimpleDataActivity extends AppCompatActivity {

    private Button btn;
    private EditText mInputData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_simple_data);
        mInputData = (EditText) findViewById(R.id.input_data);
        btn = (Button) findViewById(R.id.send_data_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextContent();
            }
        });
    }


    /**
     * 发送文本内容
     */
    private void sendTextContent() {
        String inputData = mInputData.getText().toString().trim();
        if (TextUtils.isEmpty(inputData)) {
            Toast.makeText(this, "input is null", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, inputData);
        sendIntent.setType("text/plain");
        //startActivity(sendIntent);
        //使用createChooser有三个优点：
        //1.Even if the user has previously selected a default action for this intent, the chooser will still be displayed.
        //2.If no applications match, Android displays a system message.
        //3.You can specify a title for the chooser dialog.
        startActivity(Intent.createChooser(sendIntent, "Send Message Title"));

    }

    /**
     * 发送二进制数据
     */
    private void sendBinaryContent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, "");
        sendIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(sendIntent, "send pic"));
    }

    /**
     * 发送多个内容
     */
    private void sendMultipleContent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<Parcelable>());
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share images to.."));
    }
}
