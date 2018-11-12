package com.example.geewy.cygea;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView imageView1, imageView2, imageView3, imageView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = (ImageView)findViewById(R.id.cygea);
        imageView2 = (ImageView)findViewById(R.id.applications);
        imageView3 = (ImageView)findViewById(R.id.parametres);
        imageView4 = (ImageView)findViewById(R.id.siteweb);

        imageView1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Cygea.class);

                MainActivity.this.startActivity(intent);
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Application.class);

                MainActivity.this.startActivity(intent);
            }
        });

        imageView3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Settings.class);
                MainActivity.this.startActivity(intent);
            }

        });

        imageView4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Uri uri = Uri.parse("your_ip_address");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }


}
