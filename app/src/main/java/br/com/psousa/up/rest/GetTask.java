package br.com.psousa.up.rest;

import android.os.AsyncTask;
import android.util.Log;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Created by Paulo on 01/03/2016.
 */
public class GetTask extends AsyncTask<String, String, String> {
    private final String url;
    private final Callback<String> callback;
    private final String token;

    GetTask(String url, Callback<String> callback) {
        this.url = url;
        this.callback = callback;
        this.token = null;
    }

    GetTask(String url, String token, Callback<String> callback) {
        this.url = url;
        this.callback = callback;
        this.token = token;
    }

    @Override
    protected String doInBackground(String... params) {
        final Client client = Client.create();
        final WebResource resource = client.resource(url);
        ClientResponse response;
        if (token != null){
            response = resource.accept(MIMETypes.APPLICATION_JSON.getName())
                    .header("aut", token)
                    .get(ClientResponse.class);
        } else {
            response = resource.accept(MIMETypes.APPLICATION_JSON.getName())
                    .get(ClientResponse.class);
        }

        return response.getEntity(String.class);
    }

    @Override
    protected void onPostExecute(String result) {
        callback.callback(result, null);
        super.onPostExecute(result);
    }
}
