package com.abel.netflix;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    boolean playing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title bar e menu do android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
    }

    public void playMusic(View view){
        ImageView play = findViewById(R.id.buttonPlay);


        playing = !playing;

        if (playing) {
            play.setImageResource(R.drawable.pause);
        } else {
            play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    public void nextMusic(View view){
        ImageView button = findViewById(R.id.nextButton);
        animationClick(button);
    }

    public void previousMusic(View view){
        ImageView button = findViewById(R.id.previousButton);
        animationClick(button);
    }

    public void animationClick(ImageView v) {
        if (android.os.Build.VERSION.SDK_INT > 15) {
            System.out.println("Executado");
            ColorFilter currentColor = v.getColorFilter();
            v.setColorFilter(Color.YELLOW);
            v.setColorFilter(currentColor);
        }

    }
}
