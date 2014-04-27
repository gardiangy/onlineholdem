package hu.onlineholdem.restclient.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import hu.onlineholdem.restclient.response.Response;

public abstract class RefreshTask extends AsyncTask<String, Response, Response> {

    public abstract void handleResponse(Response response);
    public abstract Response parseJson(JSONObject jsonObject);

    private static final String TAG = "RefreshTask";

    private static final int CONN_TIMEOUT = 10000;
    private static final int SOCKET_TIMEOUT = 10000;

    private boolean serverNotResponding = false;

    private Context context;

    private boolean run = true;

    private int wait = 0;

    protected RefreshTask(Context context) {
        this.context = context;
    }

    protected Response doInBackground(String... urls) {

        String url = urls[0];
        Response response = null;
        while (run) {

            SystemClock.sleep(500 + wait);
            HttpResponse httpResponse = doResponse(url);

            try {
                String data = EntityUtils.toString(httpResponse.getEntity());
                JSONObject item = new JSONObject(data);
                response = parseJson(item);
            } catch (SocketTimeoutException | NullPointerException | JSONException e) {
                serverNotResponding = true;
                Log.e(TAG, e.getLocalizedMessage(), e);
            } catch (IllegalStateException | IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
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
        if(serverNotResponding){
            Toast.makeText(context, "Server not responding", Toast.LENGTH_SHORT).show();
        } else {
            handleResponse(response);
        }
    }

    public void stopTask(){
        run = false;
    }

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

        } catch (SocketTimeoutException | ConnectTimeoutException | NullPointerException e) {
            serverNotResponding = true;
            Log.e(TAG, e.getLocalizedMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return response;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }
}
