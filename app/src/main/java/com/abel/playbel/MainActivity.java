package com.abel.playbel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = null;
    private static final String TAG = "MainActivity";
    private boolean playing = false;
    private int current = 0;
    private boolean paralells = true;
    private boolean looping = false;
    private boolean repeat_one = false;
    private boolean progressBarSystem = false;
    private ArrayList<Audio> audioList = new ArrayList<Audio>();
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove title bar e menu do android
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 102);
        }

        loadAudio();
        if (audioList.isEmpty()) {
            return;
        }
        mediaPlayer = mediaPlayerFactory(audioList, mediaPlayer);

    }

    public void play(View view) {
        System.out.println(mediaPlayer == null);
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
                looping = true;
                break;
            case 1:
                repeat_one = true;
                looping = false;
                button.setImageResource(R.drawable.repetir_1);
                view.setTag("2");
                break;
            default:
                paralells = true;
                looping = repeat_one = false;
                button.setImageResource(R.drawable.paralelo);
                view.setTag("0");
        }
    }

    // Inicia musica, starta time e progressbar
    private void playMusic(MediaPlayer media) {
        ImageView play = findViewById(R.id.buttonPlay);
        ImageView cover = findViewById(R.id.coverID);
        TextView initMusic = findViewById(R.id.initMusic);
        TextView endMusic = findViewById(R.id.endMusic);
        TextView statusMusic = findViewById(R.id.tocandoID);
        SeekBar seekBar = findViewById(R.id.simpleSeekBar);
        dadosDeMidia(audioList.get(current), media);
        if (TimeUnit.MILLISECONDS.toMillis(media.getDuration()) < 60000) {
            nextMusic();
        } else if (playing) {
            media.start();
            updateTime(media, initMusic);
            updateTime(media, seekBar);
            initMusic.setVisibility(View.VISIBLE);
            endMusic.setVisibility(View.VISIBLE);
            statusMusic.setVisibility(View.VISIBLE);
            endMusic.setText(milliSecondsToTimer(media.getDuration()));
            play.setImageResource(R.drawable.pause);
            cover.setImageBitmap(audioList.get(current).getCover());
        } else {
            media.pause();
            statusMusic.setVisibility(View.INVISIBLE);
            play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        // Ao fim da musica avança para a proxima ou a repete
        media.setOnCompletionListener((res) -> {
            ImageView button = findViewById(R.id.icon_repeat);
            if (repeat_one) {
                playMusic(media);
                paralells = true;
                looping = repeat_one = false;
                button.setImageResource(R.drawable.paralelo);
                button.setTag("0");
            } else if (looping) {
                playMusic(media);
            } else {
                nextMusic();
            }
        });
    }

    private void nextMusic() {
        if (current >= (audioList.size() - 1)) return;
        ImageView button = findViewById(R.id.nextButton);
        current += 1;
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
        if (media != null) media.stop();

        // Instantiating MediaPlayer class
        MediaPlayer facMediaPlayer = new MediaPlayer();
        try {
            facMediaPlayer.setDataSource(getApplication(), Uri.fromFile(new File(playlist.get(current).getData())));
            facMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return facMediaPlayer;
    }

    private final void dadosDeMidia(Audio audio, MediaPlayer media) {
        TextView title = findViewById(R.id.titleID);
        title.setText(audio.getTitle());
        title.setSelected(true);
    }

    // Busca todos arquivos do tipo especificado no dispositivo, requer permissao
    // Cursor obrigatóriamente precisa ser aberto e fechado dentro de try
    private final void loadAudio() {
        ContentResolver contentResolver;
        Uri uri;
        String selection;
        String sortOrder;
        Cursor cursor = null;

        try {
            contentResolver = getContentResolver();
            uri = MediaStore.Audio.Media.getContentUri("EXTERNAL_CONTENT_URI");
            grantUriPermission(null, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(data);
                    byte[] dados = mmr.getEmbeddedPicture();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(dados, 0, dados.length);
                    // Save to audioList
                    audioList.add(new Audio(data, title, album, artist, bitmap));
                }
            }
            try {
                cursor.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(MainActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
