package br.com.psousa.up.rest;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.spi.service.ServiceFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.psousa.up.model.Erro;
import br.com.psousa.up.model.Servico;

/**
 * Created by Paulo on 01/03/2016.
 */
public class NetworkHandler {
    private static NetworkHandler instance;

    public static synchronized NetworkHandler getInstance() {
        if (instance == null) {
            instance = new NetworkHandler();
        }
        return instance;
    }

    @SuppressWarnings("rawtypes")
    private NetworkHandler() {
        ServiceFinder.setIteratorProvider(new AndroidServiceIteratorProvider());
    }

    public <T> void read(final String url, final Class<T> clazz, final Callback<T> callback) {
        new GetTask(url, new Callback<String>() {

            @Override
            public void callback(String result, Erro erro) {
                callback.callback(new GsonBuilder().create().fromJson(result, clazz), erro);
            }
        }).execute();
    }

    public <T> void read(final String url, String token,final Class<T> clazz, final Callback<T> callback) {
        new GetTask(url, token, new Callback<String>() {

            @Override
            public void callback(String result, Erro erro) {
                callback.callback(new GsonBuilder().create().fromJson(result, clazz), erro);
            }
        }).execute();
    }

    public <T> void delete(final String url) {
        new DeleteTask(url).execute();
    }

    public <T> AsyncTask readList(final String url, String token, final Class<T[]> clazz, final Callback<List<T>> callback) {
        return new GetTask(url, token, new Callback<String>() {

            @Override
            public void callback(String result, Erro erro) {
                final T[] array = new GsonBuilder().create().fromJson(result, clazz);
                callback.callback(new ArrayList<T>(Arrays.asList(array)), erro);
            }
        }).execute();
    }

    public <T> void write(final String url, final Class<T> clazz, final T t, final Callback<T> callback) {
        final Gson gson = new GsonBuilder().create();
        new PostTask(url, gson.toJson(t), new Callback<String>() {

            @Override
            public void callback(String result, Erro erro) {
                if (result.contains("{")) {
                    callback.callback(gson.fromJson(result, clazz), erro);
                }else {
                    erro = new Erro();
                    erro.setDescricao(result);
                    callback.callback(null, erro);
                }
            }
        }).execute();
    }

    public <T> void write(final String url, final String token, final Servico file, final Class<T> clazz, final T t, final Callback<T> callback) {
        final Gson gson = new GsonBuilder().create();
        String r;
        if (file != null){
            r = "";
        } else {
            r = gson.toJson(t);
        }
        new PostTask(url, token, file, r, new Callback<String>() {

            @Override
            public void callback(String result, Erro erro) {
                if (result.contains("{")) {
                    Log.e("UP", result);
                    callback.callback(gson.fromJson(result, clazz), erro);
                }else {
                    erro = new Erro();
                    erro.setDescricao(result);
                    callback.callback(null, erro);
                }
            }
        }).execute();
    }

    public <T, Z> void post(final String url, final Class<T> clazz, final Z t, final Callback<T> callback) throws RuntimeException{
        final Gson gson = new GsonBuilder().create();
        new PostTask(url, gson.toJson(t), new Callback<String>() {

            @Override
            public void callback(String result, Erro erro) {
                if (result.contains("{")) {
                    //result = result.substring(1, result.length() - 1);
                    callback.callback(gson.fromJson(result, clazz), null);
                } else {
                    erro = new Erro();
                    erro.setDescricao(result);
                    callback.callback(null, erro);
                }
            }
        }).execute();
    }

    public <T> void writeRetString(final String url, final Class<T> clazz, final T t, final Callback<String> callback) {
        final Gson gson = new GsonBuilder().create();
        new PostTask(url, gson.toJson(t), new Callback<String>() {

            @Override
            public void callback(String result, Erro erro) {
                    callback.callback(result, erro);
            }
        }).execute();
    }
}
