package com.example.gestoriapp;


import java.util.HashMap;

// Patrón de diseño SingletonMap para la compartición de información entre las activities

public class SingletonMap extends HashMap< String , Object > {

    private static class SingletonHolder {
        private static final SingletonMap ourInstance = new SingletonMap();
    }

    public static SingletonMap getInstance () {
        return SingletonHolder.ourInstance;
    }

    private SingletonMap() {}

}
