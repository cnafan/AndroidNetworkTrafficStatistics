package com.example.small.flowstatistics;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Calendar;

public class AboutActivity extends AppCompatActivity {

    LinearLayout root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar bar = (Toolbar) findViewById(R.id.toolbars);
        // App Logo
        bar.setLogo(R.mipmap.ic_launcher);
        // Title
        bar.setTitle("关于");
        // Sub Title
        bar.setSubtitle("Sub title");
        setSupportActionBar(bar);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button icon = (Button) findViewById(R.id.icon_about);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int systemTime = calendar.get(Calendar.HOUR_OF_DAY);
                Toast.makeText(AboutActivity.this, systemTime+"", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
