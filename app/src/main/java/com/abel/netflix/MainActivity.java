package com.abel.netflix;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = null;
    private boolean playing = false;
    private int current = 0;
    private boolean looping = true;
    private boolean repeat_one = false;
    private TextView initMusic;
    private TextView endMusic;

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

    public void repeat(View view) {
        ImageView button = findViewById(R.id.icon_repeat);
        byte n = (byte) Integer.parseInt(view.getTag().toString());
        switch (n) {
            case 0:
                button.setImageResource(R.drawable.aleatorio);
                view.setTag("1");
                looping = !looping;
                break;
            case 1:
                repeat_one = !repeat_one;
                button.setImageResource(R.drawable.repetir_1);
                view.setTag("0");
                break;
            default:
                looping = repeat_one = false;
                button.setImageResource(R.drawable.aleatorio);
                view.setTag("0");
        }
    }

    // Inicia musica, starta time e progressbar
    private void playMusic(MediaPlayer media) {
        ImageView play = findViewById(R.id.buttonPlay);
        TextView initMusic = findViewById(R.id.initMusic);
        TextView endMusic = findViewById(R.id.endMusic);
        TextView statusMusic = findViewById(R.id.tocandoID);

        if (playing) {
            media.start();
            updateTime(media, initMusic);
            initMusic.setVisibility(View.VISIBLE);
            endMusic.setVisibility(View.VISIBLE);
            statusMusic.setVisibility(View.VISIBLE);
            endMusic.setText(milliSecondsToTimer(media.getDuration()));
            play.setImageResource(R.drawable.pause);
        } else {
            media.pause();
            statusMusic.setVisibility(View.INVISIBLE);
            play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        // Ao fim da musica avança para a proxima ou a repete
        media.setOnCompletionListener((res) -> {
            if (repeat_one) {
                playMusic(media);
                repeat_one = !repeat_one;
            } else if (looping) {
                playMusic(media);
            }
            nextMusic();
        });
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


    // Progress Bar e Time
    private void updateTime(MediaPlayer media, TextView textView) {
        final int i = 1;
        new Thread(new Runnable() {
            @Override
            public void run() {

                while(media.isPlaying()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(milliSecondsToTimer((long) media.getCurrentPosition()));
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void updateTime(MediaPlayer media, SeekBar seekBar) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(media.isPlaying()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // Converte Mili para Segundos
    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
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
