package com.qinshou.passwordedittext;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ((PasswordEditText) findViewById(R.id.password_edit_text)).setOnCompleteListener(new PasswordEditText.onCompletionListener() {
//            @Override
//            public void onCompletion(String code) {
//                Toast.makeText(MainActivity.this, "code:" + code, Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}
