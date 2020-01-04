package com.abel.playbel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abel.playbel.Utility.Util;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = null;
    private int current = 0;
    private Util util = new Util();
    private boolean playing = false,
            paralells = true,
            looping = false,
            repeat_one = false,
            progressBarSystem = false;
    private ArrayList<Audio> audioList = new ArrayList<Audio>();
    private static final int CAMERA_PERMISSION_CODE = 100,
            STORAGE_PERMISSION_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove title bar e menu do android
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        while (true) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            } else {
                break;
            }
        }

        Thread threadA = loadAudio();
        threadA.start();

        synchronized (threadA) {
            try {
                threadA.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (audioList.isEmpty()) {
            util.showMessage("Nenhuma mídia encontrada", this);
            return;
        } else {
            Random rand = new Random();
            current = rand.nextInt(audioList.size());
        }
    }

    public void play(View view) {
        if (mediaPlayer == null) {
            Thread threadB = mediaPlayerFactory();
            threadB.start();
            synchronized (threadB) {
                try {
                    threadB.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // play music
        playing = !playing;
        playMusic(mediaPlayer);

    }

    private void play() {
        if (mediaPlayer == null) {
            Thread threadB = mediaPlayerFactory();
            threadB.start();
            synchronized (threadB) {
                try {
                    threadB.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // play music
        playing = !playing;
        playMusic(mediaPlayer);

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
                Random rand = new Random();
                current = rand.nextInt(audioList.size());
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
        ImageView play = findViewById(R.id.buttonPlay),
                cover = findViewById(R.id.coverID);
        TextView initMusic = findViewById(R.id.initMusic),
                endMusic = findViewById(R.id.endMusic),
                statusMusic = findViewById(R.id.tocandoID);
        SeekBar seekBar = findViewById(R.id.simpleSeekBar);
        dadosDeMidia(audioList.get(current), media);

        if (TimeUnit.MILLISECONDS.toMillis(media.getDuration()) < 30000) {
            nextMusic();
        } else if (playing) {
            media.start();
            updateTime(media, initMusic);
            updateTime(media, seekBar);

            if (util.getEmbeddedPicture(audioList.get(current).getData()) == null) {
                Glide.with(MainActivity.this).load(R.drawable.giphy).into(cover);
            } else {
                Glide.with(MainActivity.this).load(util.getEmbeddedPicture(audioList.get(current).getData())).into(cover);
            }

            initMusic.setVisibility(View.VISIBLE);
            endMusic.setVisibility(View.VISIBLE);
            statusMusic.setVisibility(View.VISIBLE);
            endMusic.setText(util.milliSecondsToTimer(media.getDuration()));
            play.setImageResource(R.drawable.pause);

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
                Random rand = new Random();
                current = rand.nextInt(audioList.size());
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
        Thread threadB = mediaPlayerFactory();
        threadB.start();
        synchronized (threadB) {
            try {
                threadB.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        playMusic(mediaPlayer);
    }

    private void previousMusic() {
        if (current <= 0) return;
        ImageView button = findViewById(R.id.previousButton);
        current -= 1;
        Thread threadB = mediaPlayerFactory();
        threadB.start();
        synchronized (threadB) {
            try {
                threadB.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        playMusic(mediaPlayer);
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
                            textView.setText(util.milliSecondsToTimer((long) media.getCurrentPosition()));
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

    // UTILITARIOS
    public final synchronized Thread mediaPlayerFactory() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                    }

                    mediaPlayer = new MediaPlayer();

                    try {
                        mediaPlayer.setDataSource(getApplication(), Uri.fromFile(new File(audioList.get(current).getData())));
                        mediaPlayer.prepare();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                playMusic(mp);
                            }
                        });
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    notify();
                }
            }

            ;
        });
    }

    private final void dadosDeMidia(Audio audio, MediaPlayer media) {
        TextView title = findViewById(R.id.titleID);
        title.setText(audio.getTitle());
        title.setSelected(true);
    }

    // Busca todos arquivos do tipo especificado no dispositivo, requer permissao
    // Cursor obrigatóriamente precisa ser aberto e fechado dentro de try
    private final synchronized Thread loadAudio() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
                    String[] projection = {
                            MediaStore.Audio.Media.ALBUM_ID, //0
                            MediaStore.Audio.Media.ARTIST,  //1
                            MediaStore.Audio.Media.TITLE,   //2
                            MediaStore.Audio.Media.DATA,    //3
                            MediaStore.Audio.Media.ALBUM,    //4
                            MediaStore.Audio.Media.DURATION //5
                    };
                    try {
                        Cursor cursor = getContentResolver().query(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                projection,
                                selection,
                                null,
                                null);

                        while (cursor.moveToNext()) {
                            audioList.add(new Audio(cursor.getString(3), cursor.getString(2), cursor.getString(4), cursor.getString(1), cursor.getString(0)));
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    notify();
                }
            }
        });
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{permission},
                    requestCode);
        } else {
            Toast.makeText(MainActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            play();
            return true;
        } else if (keyCode == KeyEvent.ACTION_DOWN) {
            System.out.println("Aumentar volume");
        }
        return super.onKeyDown(keyCode, event);
    }

}
