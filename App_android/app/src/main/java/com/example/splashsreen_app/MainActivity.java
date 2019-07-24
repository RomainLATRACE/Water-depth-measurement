package com.example.splashsreen_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ActionBar actionBar;

    private static int timeout=2250; //duration of the home screen display
    ImageView img;
    ImageView img2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*action bar*/
        actionBar = getSupportActionBar();
        actionBar.hide();
        /*end action bar*/

        img = findViewById(R.id.img);
        img2 = findViewById(R.id.img2);


        /*animations creation*/
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.myanim);
        Animation animation2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.myanim2);

        /*animations assignment */
        img.startAnimation(animation);
        img2.startAnimation(animation2);

        Handler hdlr = new Handler();
        hdlr.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
                finish();
            }
        },timeout); //end after timeout duration

    }
}
