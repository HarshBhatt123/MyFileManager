package com.example.myfilemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void toAct1(View view) {
        Intent i = new Intent(this,MainActivity.class);
//       String download = "download";
//       i.putExtra("download",download);
        startActivity(i);
    }

    public void toAct2(View view) {
        Intent i = new Intent(this,MainActivity.class);
      //  String currentPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
       // i.putExtra("currentPath",currentPath);
        startActivity(i);
    }
}