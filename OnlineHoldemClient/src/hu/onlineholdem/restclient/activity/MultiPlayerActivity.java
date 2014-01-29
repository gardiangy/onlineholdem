package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;
import hu.onlineholdem.restclient.response.Response;
import hu.onlineholdem.restclient.task.RefreshTask;
import hu.onlineholdem.restclient.task.WebServiceTask;
import hu.onlineholdem.restclient.util.GameListAdapter;

public class MultiPlayerActivity extends Activity {

    private static final String TAG = "MultiPlayerActivity";
    private static final String SERVICE_URL = "http://146.110.44.10:8080/rest";

    private ExpandableListView list;
    private GameListAdapter listAdapter;
    private List<Game> games;
    private Map<Game, List<Player>> players;
    private NumberPicker playerNumPicker;
    private EditText startingStackSize;
    private EditText gameName;
    private RefreshGamesTask refreshTask;
    private static long joinedGameId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.multi_player_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        games = new ArrayList<>();
        players = new HashMap<>();

        playerNumPicker = (NumberPicker) findViewById(R.id.playerNumPicker);
        playerNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        playerNumPicker.setMaxValue(9);
        playerNumPicker.setMinValue(2);

        startingStackSize = (EditText) findViewById(R.id.staringStackSize);
        gameName = (EditText) findViewById(R.id.gameName);

        list = (ExpandableListView) findViewById(R.id.listView);
        listAdapter = new GameListAdapter(this, games, players);
        list.setAdapter(listAdapter);

        String getURL = SERVICE_URL + "/game";
        refreshTask = new RefreshGamesTask();
        refreshTask.execute(new String[]{getURL});
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void showGameSettings(View view){

        LinearLayout gameSettings = (LinearLayout) findViewById(R.id.gameSettings);
        if(gameSettings.getVisibility() == View.VISIBLE){
            gameSettings.setVisibility(View.GONE);
        } else {
            gameSettings.setVisibility(View.VISIBLE);
        }

    }

    public void createNewGame(View view){

        if( gameName.getText().toString().length() == 0 ){
            Toast.makeText(this, "Game Name is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if( startingStackSize.getText().toString().length() == 0 ){
            Toast.makeText(this, "Starting Stack Size is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if( Integer.valueOf(startingStackSize.getText().toString()) < 1 ){
            Toast.makeText(this, "Starting Stack Size must be greater than 1!", Toast.LENGTH_SHORT).show();
            return;
        }

        String postURL = SERVICE_URL + "/game/create";

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this,"Posting data...");
        wst.addNameValuePair("gameName", gameName.getText().toString());
        wst.addNameValuePair("startingStackSize", startingStackSize.getText().toString());
        wst.addNameValuePair("maxPlayerNumber", String.valueOf(playerNumPicker.getValue()));

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{postURL});

        LinearLayout gameSettings = (LinearLayout) findViewById(R.id.gameSettings);
        gameSettings.setVisibility(View.GONE);
        gameName.setText("");
        startingStackSize.setText("");
        playerNumPicker.setValue(2);
    }

    public void searchGames(View view){
        refreshTask.stopTask();
        refreshTask.cancel(true);
        EditText searchField = (EditText) findViewById(R.id.searchField);
        String searchString = searchField.getText() == null ? "" : searchField.getText().toString();

        String getURL = "";
        if("".equals(searchString)){
            getURL = SERVICE_URL + "/game";
        } else {
            getURL = SERVICE_URL + "/game/contains/" + searchString;
        }

        refreshTask = new RefreshGamesTask();
        refreshTask.execute(new String[]{getURL});

    }

    public void joinGame(String id){

        String postURL = SERVICE_URL + "/game/join";

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this,"Posting data...");
        wst.addNameValuePair("userId", "3");
        wst.addNameValuePair("gameId", id);

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{postURL});

    }

    public void leaveGame(String id){

        String postURL = SERVICE_URL + "/game/leave";

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this,"Posting data...");
        wst.addNameValuePair("userId", "3");
        wst.addNameValuePair("gameId", id);

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{postURL});

    }

    public void expandGroup(int groupPos){
        list.expandGroup(groupPos);
    }

    public void collapseGroup(int groupPos){
        list.collapseGroup(groupPos);
    }

    public void handleGameResponse(Response gameResponse) {

        List<Game> gameList = (List<Game>) gameResponse.getResponseObject();

        games = gameList;
        players = new HashMap<>();
        for (Game game : gameList) {
            players.put(game, game.getPlayers());
        }

        listAdapter.refreshData(games, players);
        listAdapter.notifyDataSetChanged();


    }

    public Response parseGameJson(JSONObject item) throws JSONException {
        List<Game> gameList = new ArrayList<>();
        Response gameResponse = new Response();

        if (item != null) {
            JSONArray gamesJSON = item.getJSONArray("responseObject");
            for (int i = 0; i < gamesJSON.length(); i++) {

                JSONObject gameItem = gamesJSON.getJSONObject(i);
                Game game = new Game();
                game.setPotSize(gameItem.getInt("potSize"));
                game.setGameName(gameItem.getString("gameName"));
                game.setGameId(gameItem.getLong("gameId"));
                game.setMaxPlayerNumber(gameItem.getInt("maxPlayerNumber"));
                game.setStartingStackSize(gameItem.getInt("startingStackSize"));
                JSONArray playerArray = gameItem.getJSONArray("players");
                List<Player> playerList = new ArrayList<>();
                for (int counter = 0; counter < playerArray.length(); counter++) {
                    Player player = new Player();

                    JSONObject playerItem = playerArray.getJSONObject(counter);
                    player.setPlayerId(playerItem.getLong("playerId"));
                    player.setStackSize(playerItem.getInt("stackSize"));
                    player.setPlayerName(playerItem.getJSONObject("user").getString("userName"));
                    playerList.add(player);
                }
                game.setPlayers(playerList);
                gameList.add(game);
            }

            gameResponse.setResponseObject(gameList);
        }
        return gameResponse;
    }


    private class RefreshGamesTask extends RefreshTask {

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
//            handleGameResponse(response);
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



