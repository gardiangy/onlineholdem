package hu.onlineholdem.restclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String SERVICE_URL = "http://192.168.1.100:8080/rest";

    private static final String TAG = "MainActivity";


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String getURL = SERVICE_URL + "/game";
        RefreshTask refreshTask = new RefreshTask();
        refreshTask.execute(new String[]{getURL});

        SeekBar betBar = (SeekBar) findViewById(R.id.betBar);
        betBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView betValue = (TextView) findViewById(R.id.betValue);
                betValue.setText("" + i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }


    public void postData(View vw) {

        String postURL = SERVICE_URL + "/game";

        TextView betValue = (TextView) findViewById(R.id.betValue);

        WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this,
                "Posting data...");

        ActionType actionType = null;
        int btnId = vw.getId();
        if (btnId == R.id.btnCheck)
            actionType = ActionType.CHECK;
        if (btnId == R.id.btnBet)
            actionType = ActionType.BET;
        if (btnId == R.id.btnFold)
            actionType = ActionType.FOLD;

        wst.addNameValuePair("actionType", actionType.getName());
        wst.addNameValuePair("betValue", betValue.getText().toString().equals("") ? "0" : betValue.getText().toString());
        wst.addNameValuePair("playerId", "1");
        wst.addNameValuePair("gameId", "1");

        // the passed String is the URL we will POST to
        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{postURL});


    }

    public void handleResponse(Response response) {

        TextView potView = (TextView) findViewById(R.id.potSize);
        potView.setText("" + response.getPotSize());

    }

    public Response parseJson(JSONObject item) throws JSONException {
        List<Player> playerList = new ArrayList<>();
        Response response = new Response();

        if (item != null) {


            response.setPotSize(item.getInt("potSize"));

            JSONArray playerArray = item.getJSONArray("players");

            for (int counter = 0; counter < playerArray.length(); counter++) {
                Player player = new Player();

                JSONObject playerItem = playerArray.getJSONObject(counter);

                player.setPlayerId(playerItem.getLong("playerId"));

                player.setStackSize(playerItem.getInt("stackSize"));


                playerList.add(player);

            }
            response.setPlayers(playerList);

        }

        return response;

    }

    private class WebServiceTask extends AsyncTask<String, Response, Response> {

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
//            while(run){

//                SystemClock.sleep(500);
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
//                publishProgress(messages);
//            }

            return response;
        }

//        @Override
//        protected void onProgressUpdate(List<Message>... response) {
//            handleResponse(response[0]);
//            //pDlg.dismiss();
//        }

        @Override
        protected void onPostExecute(Response response) {

            handleResponse(response);
//            pDlg.dismiss();

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


    private class RefreshTask extends AsyncTask<String, Response, Response> {

        public static final int GET_TASK = 2;

        private static final String TAG = "RefreshTask";

        // connection timeout, in milliseconds (waiting to connect)
        private static final int CONN_TIMEOUT = 3000;

        // socket timeout, in milliseconds (waiting for data)
        private static final int SOCKET_TIMEOUT = 5000;

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


}
