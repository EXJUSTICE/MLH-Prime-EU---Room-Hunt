package com.xu.roomhunter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

/**
 * Created by Omistaja on 01/04/2017.
 */

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_splash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);





        Thread timerThread = new Thread(){
            //Launch our MainActivity after 4s

            public void run(){
                try{

                    sleep(2000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent  = new Intent (Splash.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }

}
