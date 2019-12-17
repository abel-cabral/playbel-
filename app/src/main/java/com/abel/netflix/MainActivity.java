package com.abel.netflix;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = null;
    private boolean playing = false;
    private int current = 0;

    private ArrayList<Integer> playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title bar e menu do android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        playlist = new ArrayList<>();
        playlist.add(R.raw.stand_proud);
        playlist.add(R.raw.bloody_stream);

        mediaPlayer = mediaPlayerFactory(playlist, mediaPlayer);
    }

    public void play(View view) {
        playing = !playing;
        // play music
        if (mediaPlayer != null) {
            playMusic(mediaPlayer);
        }

    }

    public void next(View view) {
        nextMusic();
    }

    public void previous(View view) {
        previousMusic();
    }

    private void playMusic(MediaPlayer media) {
        ImageView play = findViewById(R.id.buttonPlay);
        if (playing) {
            media.start();
            play.setImageResource(R.drawable.pause);
        } else {
            media.pause();
            play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        // Ao fim da musica avança para a proxima
        media.setOnCompletionListener((res) -> nextMusic());
    }

    private void nextMusic() {
        if (current >= (playlist.size() - 1)) return;
        ImageView button = findViewById(R.id.nextButton);
        current += 1;
        mediaPlayer = mediaPlayerFactory(playlist, mediaPlayer);
        playMusic(mediaPlayer);
        animationClick(button);
    }

    private void previousMusic() {
        if (current <= 0) return;
        ImageView button = findViewById(R.id.previousButton);
        current -= 1;
        mediaPlayer = mediaPlayerFactory(playlist, mediaPlayer);
        playMusic(mediaPlayer);
        animationClick(button);
    }

    private void animationClick(ImageView v) {
        if (android.os.Build.VERSION.SDK_INT > 15) {
            System.out.println("Executado");
            ColorFilter currentColor = v.getColorFilter();
            v.setColorFilter(Color.YELLOW);
            v.setColorFilter(currentColor);
        }
    }



    // UTILITARIOS
    public MediaPlayer mediaPlayerFactory(ArrayList<Integer> playlist, MediaPlayer now) {
        if (now != null) {
            now.stop();
        }
        // Instantiating MediaPlayer class
        MediaPlayer facMediaPlayer = MediaPlayer.create(getApplicationContext(), playlist.get(current));
        facMediaPlayer.setVolume(80, 80);

        // Subscrible para ouvir mudanças na barra de progresso
        // simpleSeekBar.setMax(100);

        /*
        // Adding Listener to value property.
        progressBar.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue, Number newValue) {

                if (progressBarSystem) {
                    return;
                }

                mediaPlayer.pause();
                timeScreen.setText(TimeConvert.convertToMinute(timeMusic, "mm:ss"));
                timeMusic = mediaPlayer.getTotalDuration().toMillis() - mediaPlayer.getCurrentTime().toMillis();
                mediaPlayer.seek(mediaPlayer.getMedia().getDuration().multiply(progressBar.getValue() / 100));
            }
        });
        */
        return facMediaPlayer;
    }


}
