package com.example.restclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

    private Context mContext = this;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeekBar betBar = (SeekBar) findViewById(R.id.chipBar);
        betBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Button betBtn = (Button) findViewById(R.id.btnBet);
                betBtn.setText("Bet " + i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    public void retrieveSampleData(View vw) {

        String sampleURL = SERVICE_URL + "/message";

        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this,
                "GETting data...");

        wst.execute(new String[]{sampleURL});

    }

    public void getMessageList(View vw) {


        String sampleURL = SERVICE_URL + "/message/messages";

        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, mContext,
                "GETting data...");


        wst.execute(new String[]{sampleURL});

    }

    public void postData(View vw) {

        String postURL = SERVICE_URL + "/";

        EditText message = (EditText) findViewById(R.id.editText);

        WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this,
                "Posting data...");

        wst.addNameValuePair("message", message.getText().toString());

        // the passed String is the URL we will POST to
        wst.execute(new String[]{postURL});


    }

    public void handleResponse(List<Message> response) {
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.textView);
        for (Message message : response) {
            listAdapter.add(message.getValue());
        }

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);


    }

    private void hideKeyboard() {

        InputMethodManager inputManager = (InputMethodManager) MainActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(MainActivity.this
                .getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class WebServiceTask extends AsyncTask<String, List<Message>, List<Message>> {

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

            hideKeyboard();
            //showProgressDialog();

        }

        protected List<Message> doInBackground(String... urls) {

            String url = urls[0];
            List<Message> messages = null;
            while(run){

                SystemClock.sleep(500);
                HttpResponse response = doResponse(url);

                try {
                    String data = EntityUtils.toString(response.getEntity());
                    JSONObject item = new JSONObject(data);
                    messages = parseJson(item);
                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishProgress(messages);
            }

            return messages;
        }

        @Override
        protected void onProgressUpdate(List<Message>... response) {
            handleResponse(response[0]);
            //pDlg.dismiss();
        }

        @Override
        protected void onPostExecute(List<Message> response) {

            handleResponse(response);
            pDlg.dismiss();

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
                        httppost.setEntity(new UrlEncodedFormEntity(params));

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


        public List<Message> parseJson(JSONObject item) throws JSONException {
            List<Message> messageList = new ArrayList<Message>();

            JSONArray jsonArray;


            if (item != null) {

                jsonArray = item.getJSONArray("message");

                for (int counter = 0; counter < jsonArray.length(); counter++) {
                    Message message = new Message();

                    JSONObject messageItem = jsonArray.getJSONObject(counter);

                    message.setId(messageItem.getLong("id"));

                    message.setValue(messageItem.getString("value"));


                    messageList.add(message);

                }

            }

            return messageList;

        }
    }


}
