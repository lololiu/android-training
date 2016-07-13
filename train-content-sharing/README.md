# Building Apps with Content Sharing
通过Intent向其他应用传递简单的数据：文本、图片流

### 关键代码(以发送文本信息为例)

**发送文本信息：**
```
Intent sendIntent = new Intent();
sendIntent.setAction(Intent.ACTION_SEND);
sendIntent.putExtra(Intent.EXTRA_TEXT, inputData);
sendIntent.setType("text/plain");
//startActivity(sendIntent);
startActivity(Intent.createChooser(sendIntent, "Send Message Title"));
```
使用createChooser有三个优点：
>1.Even if the user has previously selected a default action for this intent, the chooser will still be displayed.
>2.If no applications match, Android displays a system message.
>3.You can specify a title for the chooser dialog.

**配置Activity的intent-filter**
```
<activity android:name=".simpledata.ReceiveDataActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="image/*" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.SEND_MULTIPLE" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="image/*" />
    </intent-filter>
</activity>
```

**在接收界面接收传过来的信息**
```
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
```
