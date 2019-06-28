package com.image.get.talkwitharemoteserver;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerHolder {

    private static ServerHolder instance = null;

    public synchronized static ServerHolder getInstance(){
        if(instance != null)
            return instance;

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient)
                .baseUrl("http://hujipostpc2019.pythonanywhere.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MainActivity.MyServer serverInterface = retrofit.create(MainActivity.MyServer.class);
        instance = new ServerHolder(serverInterface);
        return instance;
    }


    public final MainActivity.MyServer serverInterface;

    private ServerHolder(MainActivity.MyServer serverInterface) {
        this.serverInterface = serverInterface;
    }


}

