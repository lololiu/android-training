package com.royll.contentsharing.simpledata;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.royll.contentsharing.R;

public class ReceiveDataActivity extends AppCompatActivity {
    private static final String TAG = "ReceiveDataActivity";
    TextView receiveTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_data);
        receiveTV = (TextView) findViewById(R.id.receive_text);
        Intent it = getIntent();
        String action = it.getAction();
        String type = it.getType();
        Log.d(TAG, "onCreate: action=" + action + " type=" + type);
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(it); // Handle text being sent
            } else if (type.startsWith("image/")) {
                // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        Log.d(TAG, "handleSendText: sharedText=" + sharedText);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            receiveTV.setText("receive text: " + sharedText);
        }
    }

}
