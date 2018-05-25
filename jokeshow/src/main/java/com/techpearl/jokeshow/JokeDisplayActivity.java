package com.techpearl.jokeshow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class JokeDisplayActivity extends AppCompatActivity {
    public static final String EXTRA_JOKE = "joke_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke_display);
        TextView tv = findViewById(R.id.textView);
        if(getIntent().hasExtra(EXTRA_JOKE)){
            tv.setText(getIntent().getStringExtra(EXTRA_JOKE));
        }
    }
}
