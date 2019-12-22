package com.abel.playbel;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Audio implements Serializable {

    private String data;
    private String title;
    private String album;
    private String artist;
    private String album_ID;

    public Audio(String data, String title, String album, String artist, String album_ID) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.album_ID = album_ID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum_ID() {
        return album_ID;
    }

    public void setAlbum_ID(String cover) {
        this.album_ID = album_ID;
    }
}
