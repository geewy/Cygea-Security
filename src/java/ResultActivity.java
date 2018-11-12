package com.example.geewy.cygea;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

//Permit to get an html page from a website
public class ResultActivity extends AppCompatActivity {

    private final String USER_AGENT = "Mozilla/5.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //Intent intent = getIntent();
        //String packageName;


        String packageName = getIntent().getExtras().getString("apk_package");
        packageName = Application.debug;

        WebView webv = (WebView)this.findViewById(R.id.WebView);
        webv.loadUrl("your_ip_address/"+packageName+".html");

    }


    /*public String readFile(Context context){
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;

        char[] inputBuffer = new char[255];
        String data = null;

        try{
            fileInputStream = context.openFileInput("db.txt");
            inputStreamReader = new InputStreamReader(fileInputStream);
            inputStreamReader.read(inputBuffer);
            data = new String(inputBuffer);



        }catch (Exception e){
            e.printStackTrace();
        }

        finally {
            try {
                inputStreamReader.close();
                fileInputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        return data;
    }*/
}
