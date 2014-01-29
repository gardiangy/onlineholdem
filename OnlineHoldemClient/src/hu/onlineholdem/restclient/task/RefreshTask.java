package hu.onlineholdem.restclient.task;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import hu.onlineholdem.restclient.response.Response;

public abstract class RefreshTask extends AsyncTask<String, Response, Response> {

    public abstract void handleResponse(Response response);
    public abstract Response parseJson(JSONObject jsonObject);

    private static final String TAG = "RefreshTask";

    // connection timeout, in milliseconds (waiting to connect)
    private static final int CONN_TIMEOUT = 3000;

    // socket timeout, in milliseconds (waiting for data)
    private static final int SOCKET_TIMEOUT = 10000;

    private boolean run = true;

    protected Response doInBackground(String... urls) {

        String url = urls[0];
        Response response = null;
        while (run) {

            SystemClock.sleep(500);
            HttpResponse httpResponse = doResponse(url);

            try {
                String data = EntityUtils.toString(httpResponse.getEntity());
                JSONObject item = new JSONObject(data);
                response = parseJson(item);
            } catch (IllegalStateException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            publishProgress(response);
        }

        return response;
    }

    @Override
    protected void onProgressUpdate(Response... response) {
        handleResponse(response[0]);
    }

    @Override
    protected void onPostExecute(Response response) {
        handleResponse(response);
    }

    public void stopTask(){
        run = false;
    }

    // Establish connection and socket (data retrieval) timeouts
    private HttpParams getHttpParams() {

        HttpParams htpp = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
        HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

        return htpp;
    }


    private HttpResponse doResponse(String url) {

        HttpClient httpclient = new DefaultHttpClient(getHttpParams());
        HttpResponse response = null;

        try {

            HttpGet httpget = new HttpGet(url);
            response = httpclient.execute(httpget);

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return response;
    }
}
