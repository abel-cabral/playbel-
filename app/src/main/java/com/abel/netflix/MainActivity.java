package com.abel.netflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = null;
    private boolean playing = false;
    private int current = 0;
    private boolean looping = true;
    private boolean repeat_one = false;
    private boolean progressBarSystem = false;
    private ArrayList<Audio> audioList = new ArrayList<Audio>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title bar e menu do android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        loadAudio();
        mediaPlayer = mediaPlayerFactory(audioList, mediaPlayer);
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
        SeekBar seekBar = findViewById(R.id.simpleSeekBar);

        if (playing) {
            media.start();
            updateTime(media, initMusic);
            updateTime(media, seekBar);
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
        if (current >= (audioList.size() - 1)) return;
        ImageView button = findViewById(R.id.nextButton);
        current += 1;
        System.out.println(current);
        mediaPlayer = mediaPlayerFactory(audioList, mediaPlayer);
        playMusic(mediaPlayer);
        animationClick(button);
    }

    private void previousMusic() {
        if (current <= 0) return;
        ImageView button = findViewById(R.id.previousButton);
        current -= 1;
        mediaPlayer = mediaPlayerFactory(audioList, mediaPlayer);
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

                while (media.isPlaying()) {
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
        // Progresso Seek
        new Thread(new Runnable() {
            double init;
            final double end = TimeUnit.MILLISECONDS.toMillis(media.getDuration());

            // Se fizer os Timeinit na mesma linha ocorre um bug com resultado sempre zero
            @Override
            public void run() {
                while (media.isPlaying()) {
                    init = TimeUnit.MILLISECONDS.toMillis(media.getCurrentPosition());
                    if (progressBarSystem) continue;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSystem = true;
                            seekBar.setProgress((int) Math.round((init / end) * 1000));
                            progressBarSystem = false;
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

        // Interacoes com a Seek
        new Thread(new Runnable() {
            double init;
            final double end = TimeUnit.MILLISECONDS.toMillis(media.getDuration());
            int seekbar;

            // Se fizer os Timeinit na mesma linha ocorre um bug com resultado sempre zero
            @Override
            public void run() {
                while (media.isPlaying()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress,
                                                              boolean fromUser) {
                                    seekBar.setProgress(progress);
                                    seekbar = media.getDuration() * progress / 1000;
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                    playMusic(media);
                                    progressBarSystem = true;
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    media.seekTo(seekbar);
                                    playMusic(media);
                                    progressBarSystem = false;
                                }
                            });
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
    public final MediaPlayer mediaPlayerFactory(ArrayList<Audio> playlist, MediaPlayer media) {
        if (media != null) {
            media.stop();
        }
        // Instantiating MediaPlayer class
        MediaPlayer facMediaPlayer = new MediaPlayer();
        facMediaPlayer.setVolume(100, 100);
        facMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            facMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(playlist.get(current).getData()));
            facMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return facMediaPlayer;
    }



    private final void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                // Save to audioList
                audioList.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }

}
