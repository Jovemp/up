package br.com.psousa.up.rest;

import android.os.AsyncTask;
import android.util.Log;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import br.com.psousa.up.model.Servico;

/**
 * Created by Paulo on 01/03/2016.
 */
public class PostTask extends AsyncTask<String, String, String> {
    private final String url;
    private final String requestBody;
    private final Callback<String> callback;
    private final String token;
    private final Servico serv;

    PostTask(String url, String requestBody, Callback<String> callback) {
        this.url = url;
        this.requestBody = requestBody;
        this.callback = callback;
        this.token = null;
        this.serv = null;
    }

    PostTask(String url, String token, Servico serv, String requestBody, Callback<String> callback) {
        this.url = url;
        this.requestBody = requestBody;
        this.callback = callback;
        this.token = token;
        this.serv = serv;
    }

    @Override
    protected String doInBackground(String... params) {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(MultiPartBeanProvider.class);
        final Client client = Client.create(config);
        final WebResource resource = client.resource(url);
        String responseEntity = "";
        ClientResponse response;
        if (token != null && serv.getFoto() != null) {

            /*MultiPart multiPart = new MultiPart();
            byte[] logo = FileUtils.readFileToByteArray(serv.getFoto());
            multiPart.bodyPart( new BodyPart(serv.getFoto(),MediaType.APPLICATION_OCTET_STREAM_TYPE));


            // POST request final
            response = resource
                    .type("multipart/form-data")
                    .header("aut", token)
                    .header("descricao", serv.getDescricao())
                    .header("contato", serv.getContato())
                    .header("categoria", serv.getCategoria().get_id())
                    .post(ClientResponse.class,
                            multiPart);*/
            String fileName = serv.getFoto().getPath();

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(serv.getFoto().getPath());

            if (!sourceFile.isFile()) {

                Log.e("uploadFile", "Source File not exist :"
                        + url + "" + serv.getFoto().getPath());


                return "Erro";

            }
            else
            {
                try{
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL urll = new URL(url);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) urll.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("aut", token);
                    conn.setRequestProperty("descricao", serv.getDescricao());
                    conn.setRequestProperty("contato", serv.getContato());
                    conn.setRequestProperty("categoria", serv.getCategoria().get_id());
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name='uploaded_file';filename='"
                                + fileName + "'" + lineEnd);

                        dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = conn.getResponseCode();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    responseEntity = sb.toString();

                Log.i("uploadFile", "HTTP Response is : "
                        + responseEntity + ": " + serverResponseCode);



                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                ex.printStackTrace();

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();

            }

        }


        } else if (token != null) {
            response = resource.type(MIMETypes.APPLICATION_JSON.getName())
                    .header("aut", token)
                    .post(ClientResponse.class, requestBody);
            responseEntity = response.getEntity(String.class).replaceAll("\\\\", "");
        } else {
            response = resource.type(MIMETypes.APPLICATION_JSON.getName())
                    .post(ClientResponse.class, requestBody);
            responseEntity = response.getEntity(String.class).replaceAll("\\\\", "");
        }



        Log.i("TESTE", responseEntity);
        return responseEntity;
    }

    @Override
    protected void onPostExecute(String result) {
        callback.callback(result, null);
        super.onPostExecute(result);
    }
}
