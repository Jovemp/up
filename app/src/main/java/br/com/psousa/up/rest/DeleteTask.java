package br.com.psousa.up.rest;

import android.os.AsyncTask;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Created by Paulo on 27/12/2016.
 */

public class DeleteTask extends AsyncTask<String, String, String> {

    private final String url;

    DeleteTask(String url) {
        this.url = url;
    }

    @Override
    protected String doInBackground(String... params) {
        final Client client = Client.create();
        final WebResource resource = client.resource(url);
        final ClientResponse response = resource.accept(MIMETypes.APPLICATION_JSON.getName())
                .delete(ClientResponse.class);
        return response.getEntity(String.class);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
