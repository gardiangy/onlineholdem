package hu.onlineholdem.restclient;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        RefreshTask refreshTask = new RefreshGameTask();
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

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this,
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

    public void handleGameResponse(Response response) {

        TextView potView = (TextView) findViewById(R.id.potSize);
        potView.setText("" + response.getPotSize());

    }

    public Response parseGameJson(JSONObject item) throws JSONException {
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

    private class RefreshGameTask extends RefreshTask{

        @Override
        public void handleResponse(Response response) {
            handleGameResponse(response);
        }

        @Override
        public Response parseJson(JSONObject jsonObject) {
            try {
                return parseGameJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class PostGameTask extends WebServiceTask{

        public PostGameTask(int taskType, Context mContext, String processMessage) {
            super(taskType, mContext, processMessage);
        }

        @Override
        public void handleResponse(Response response) {
            handleGameResponse(response);
        }

        @Override
        public Response parseJson(JSONObject jsonObject) {
            try {
                return parseGameJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
