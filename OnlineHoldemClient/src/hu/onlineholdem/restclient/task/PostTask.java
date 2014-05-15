package hu.onlineholdem.restclient.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import hu.onlineholdem.restclient.response.Response;

public abstract class PostTask extends AsyncTask<String, Response, Response> {

    public abstract void handleResponse(Response response);
    public abstract Response parseJson(JSONObject jsonObject);

    private static final String TAG = "PostTask";

    private static final int CONN_TIMEOUT = 3000;
    private static final int SOCKET_TIMEOUT = 5000;

    private boolean serverNotResponding = false;

    private Context mContext = null;

    private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

    public PostTask(Context mContext) {
        this.mContext = mContext;
    }

    public void addNameValuePair(String name, String value) {

        params.add(new BasicNameValuePair(name, value));
    }

    @Override
    protected void onPreExecute() {

    }

    protected Response doInBackground(String... urls) {

        String url = urls[0];
        Response response = null;
        HttpResponse httpResponse = doResponse(url);

        try {
            String data = EntityUtils.toString(httpResponse.getEntity());
            JSONObject item = new JSONObject(data);
            response = parseJson(item);
        } catch (SocketTimeoutException | NullPointerException e) {
            serverNotResponding = true;
        } catch (IllegalStateException | JSONException | IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        return response;
    }


    @Override
    protected void onPostExecute(Response response) {

        if (serverNotResponding) {
            Toast.makeText(mContext, "Server not responding", Toast.LENGTH_SHORT).show();
        }
        handleResponse(response);


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

                HttpPost httppost = new HttpPost(url);
                JSONObject json = new JSONObject();
                for (NameValuePair pair : params) {
                    json.put(pair.getName(), pair.getValue());
                }
                httppost.setEntity(new StringEntity(json.toString()));
                httppost.setHeader("Accept", "application/json");
                httppost.setHeader("Content-type", "application/json");

                response = httpclient.execute(httppost);


        } catch (SocketTimeoutException | ConnectTimeoutException | NullPointerException e) {
            serverNotResponding = true;
            Log.e(TAG, e.getLocalizedMessage(), e);
        } catch (Exception e) {

            Log.e(TAG, e.getLocalizedMessage(), e);

        }

        return response;
    }
}
