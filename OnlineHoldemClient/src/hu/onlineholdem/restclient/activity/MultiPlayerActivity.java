package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Action;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;
import hu.onlineholdem.restclient.enums.GameState;
import hu.onlineholdem.restclient.enums.Suit;
import hu.onlineholdem.restclient.response.Response;
import hu.onlineholdem.restclient.task.PostTask;
import hu.onlineholdem.restclient.task.RefreshTask;
import hu.onlineholdem.restclient.thread.MultiPlayerThread;
import hu.onlineholdem.restclient.util.GraphicStuff;
import hu.onlineholdem.restclient.util.PlayerComperator;

public class MultiPlayerActivity extends Activity {


    private long userId;
    private long gameId;

    private static final String IP = "80.98.102.139:8080";
    private String SERVICE_URL = "http://" + IP + "/rest";
    private String newIP;

    private static final String TAG = "MultiplayerActivity";
    private GraphicStuff graphics;
    private Button btnCheck;
    private Button btnBet;
    private Button btnFold;
    private SeekBar betBar;
    private TextView betValue;
    private int betAmount;
    private boolean sendingAction;

    private MultiPlayerThread multiThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.multi_player_layout);

        betBar = (SeekBar) findViewById(R.id.betBar);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        btnBet = (Button) findViewById(R.id.btnBet);
        btnFold = (Button) findViewById(R.id.btnFold);
        betValue = (TextView) findViewById(R.id.betValue);

        betBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                betValue.setText("" + (i + multiThread.getMinBet()));
                betAmount = i + multiThread.getMinBet();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getLong("userId");
        gameId = bundle.getLong("gameId");
        newIP = bundle.getString("IP");
        if(!newIP.equals(IP)){
            SERVICE_URL = "http://" + newIP + "/rest";
        }

        graphics = new GraphicStuff(this);

        multiThread = new MultiPlayerThread(userId,this,btnCheck,btnBet,btnFold,betBar,betValue,gameId,graphics);
        multiThread.start();

    }




    public void postAction(View vw) {

        String postURL = SERVICE_URL + "/action";

        TextView betValue = (TextView) findViewById(R.id.betValue);

        PostTask wst = new PostGamePostTask(this);

        sendingAction = true;

        Button button = (Button) vw;

        ActionType actionType = null;
        int btnId = button.getId();
        if (btnId == R.id.btnCheck)
            actionType = button.getText().toString().equals("CHECK") ? ActionType.CHECK : ActionType.CALL;
        if (btnId == R.id.btnBet) {
            if (button.getText().toString().equals("BET")) {
                actionType = ActionType.BET;
            } else if (button.getText().toString().equals("RAISE")) {
                actionType = ActionType.RAISE;
            } else {
                actionType = ActionType.ALL_IN;
            }
        }
        if (btnId == R.id.btnFold)
            actionType = ActionType.FOLD;

        wst.addNameValuePair("actionType", actionType.name());

        if (actionType.equals(ActionType.BET) || actionType.equals(ActionType.RAISE)) {
            wst.addNameValuePair("betValue", betAmount + "");
        } else if(actionType.equals(ActionType.ALL_IN) ){
            Player user = multiThread.getUser();
            int betAmount = null == user.getBetAmount() ? 0 : user.getBetAmount();
            wst.addNameValuePair("betValue", (user.getStackSize() + betAmount) + "");
        } else {
            wst.addNameValuePair("betValue", "0");
        }

        wst.addNameValuePair("playerId", String.valueOf(multiThread.getPlayerId()));
        wst.addNameValuePair("gameId", String.valueOf(gameId));

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{postURL});
        graphics.showActionButtons(false);
        multiThread.setActionButtonsVisible(false);


    }


    private class PostGamePostTask extends PostTask {

        public PostGamePostTask(Context mContext) {
            super(mContext);
        }

        @Override
        public void handleResponse(Response response) {
            if(null == response){
                graphics.showActionButtons(true);
            } else {
                multiThread.handleGameResponse(response);
                sendingAction = false;
            }

        }

        @Override
        public Response parseJson(JSONObject jsonObject) {
            try {
                return multiThread.parseGameJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean isSendingAction() {
        return sendingAction;
    }

    public void setSendingAction(boolean sendingAction) {
        this.sendingAction = sendingAction;
    }
}
