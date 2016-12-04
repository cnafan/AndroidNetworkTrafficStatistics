package com.example.small.flowstatistics;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

public class AboutActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listviewOpensource;
    private ListView listviewUpdatelog;
    private TextView textViewCurVersion;

    private  String getAPPVersion() {
        PackageManager manager;
        PackageInfo info = null;
        manager = this.getPackageManager();
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        assert info != null;
        return info.versionName;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
                Toast.makeText(AboutActivity.this, systemTime + "", Toast.LENGTH_SHORT).show();
                Snackbar.make(getWindow().getDecorView(), myDevice(), LENGTH_SHORT).show();
                //getWindow().getDecorView()
            }
        });
        listviewOpensource = (ListView) findViewById(R.id.opensourceproject);   //组织数据源
        ArrayAdapter<CharSequence> listviewOpensourceAA = ArrayAdapter.createFromResource(this, R.array.opensourceproject, R.layout.opensource_item);
        listviewOpensource.setAdapter(listviewOpensourceAA);

        listviewUpdatelog = (ListView) findViewById(R.id.updatelog);   //组织数据源
        ArrayAdapter<CharSequence> listviewUpdatelogAA = ArrayAdapter.createFromResource(this, R.array.updatelog, R.layout.opensource_item);
        listviewUpdatelog.setAdapter(listviewUpdatelogAA);

        textViewCurVersion = (TextView) findViewById(R.id.curversion);
        textViewCurVersion.setText("当前版本:"+getAPPVersion());
    }

    public static boolean isXIAOMI() {
        String device = Build.MANUFACTURER;
        System.out.println("Build.MANUFACTURER = " + device);
        if (device.equals("Xiaomi")) {
            System.out.println("this is a xiaomi device");
            return true;
        } else {
            return false;
        }
    }

    public static String myDevice() {
        return Build.MANUFACTURER;
    }

}
