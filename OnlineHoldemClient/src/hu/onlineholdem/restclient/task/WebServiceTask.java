package hu.onlineholdem.restclient.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import java.util.ArrayList;

import hu.onlineholdem.restclient.response.Response;

public abstract class WebServiceTask extends AsyncTask<String, Response, Response> {

    public abstract void handleResponse(Response response);
    public abstract Response parseJson(JSONObject jsonObject);

    public static final int POST_TASK = 1;
    public static final int GET_TASK = 2;

    private static final String TAG = "WebServiceTask";

    // connection timeout, in milliseconds (waiting to connect)
    private static final int CONN_TIMEOUT = 3000;

    // socket timeout, in milliseconds (waiting for data)
    private static final int SOCKET_TIMEOUT = 5000;

    private int taskType = GET_TASK;
    private Context mContext = null;
    private String processMessage = "Processing...";

    private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

    private ProgressDialog pDlg = null;

    private boolean run = true;

    public WebServiceTask(int taskType, Context mContext,
                          String processMessage) {

        this.taskType = taskType;
        this.mContext = mContext;
        this.processMessage = processMessage;
    }

    public void addNameValuePair(String name, String value) {

        params.add(new BasicNameValuePair(name, value));
    }

    private void showProgressDialog() {

        pDlg = new ProgressDialog(mContext);
        pDlg.setMessage(processMessage);
        pDlg.setProgressDrawable(mContext.getWallpaper());
        pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDlg.setCancelable(false);
        pDlg.show();

    }

    @Override
    protected void onPreExecute() {

        //hideKeyboard();
        //showProgressDialog();

    }

    protected Response doInBackground(String... urls) {

        String url = urls[0];
        Response response = null;
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

        return response;
    }



    @Override
    protected void onPostExecute(Response response) {

        handleResponse(response);

    }

    // Establish connection and socket (data retrieval) timeouts
    private HttpParams getHttpParams() {

        HttpParams htpp = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
        HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

        return htpp;
    }


    private HttpResponse doResponse(String url) {

        // Use our connection and data timeouts as parameters for our
        // DefaultHttpClient
        HttpClient httpclient = new DefaultHttpClient(getHttpParams());

        HttpResponse response = null;

        try {
            switch (taskType) {

                case POST_TASK:
                    HttpPost httppost = new HttpPost(url);
                    // Add parameters
                    JSONObject json = new JSONObject();
                    for (NameValuePair pair : params) {
                        json.put(pair.getName(), pair.getValue());
                    }
                    httppost.setEntity(new StringEntity(json.toString()));
                    httppost.setHeader("Accept", "application/json");
                    httppost.setHeader("Content-type", "application/json");

                    response = httpclient.execute(httppost);
                    break;
                case GET_TASK:
                    HttpGet httpget = new HttpGet(url);
                    response = httpclient.execute(httpget);
                    break;
            }
        } catch (Exception e) {

            Log.e(TAG, e.getLocalizedMessage(), e);

        }

        return response;
    }
}