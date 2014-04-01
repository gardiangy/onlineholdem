package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.GameState;
import hu.onlineholdem.restclient.response.Response;
import hu.onlineholdem.restclient.task.RefreshTask;
import hu.onlineholdem.restclient.task.WebServiceTask;
import hu.onlineholdem.restclient.util.DateTimeDialog;
import hu.onlineholdem.restclient.util.GameListAdapter;

public class GameBrowserActivity extends Activity implements DateTimeDialog.DateTimeDialogListener {

    private static final String TAG = "GameBrowserActivity";
    private static final String SERVICE_URL = "http://192.168.0.105:8080/rest";

    private ExpandableListView list;
    private GameListAdapter listAdapter;
    private List<Game> games;
    private Map<Game, List<Player>> players;
    private NumberPicker playerNumPicker;
    private EditText startingStackSize;
    private EditText gameName;
    private EditText startTime;
    private RefreshGamesTask refreshTask;

    private String newGameStartTime;
    private Long userId;
    private boolean startedMultiPlayerActivity = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.game_browser_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        games = new ArrayList<>();
        players = new HashMap<>();

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getLong("userId");

        playerNumPicker = (NumberPicker) findViewById(R.id.playerNumPicker);
        playerNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        playerNumPicker.setMaxValue(9);
        playerNumPicker.setMinValue(2);

        startingStackSize = (EditText) findViewById(R.id.staringStackSize);
        gameName = (EditText) findViewById(R.id.gameName);
        startTime = (EditText) findViewById(R.id.startTime);
        startTime.setClickable(true);
        startTime.setFocusable(false);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeDialog dialog = new DateTimeDialog();
                dialog.show(getFragmentManager(), "DateTimeDialog");
            }
        });

        list = (ExpandableListView) findViewById(R.id.listView);
        listAdapter = new GameListAdapter(this, games, players);
        list.setAdapter(listAdapter);

        String getURL = SERVICE_URL + "/game";
        refreshTask = new RefreshGamesTask(this);
        refreshTask.execute(new String[]{getURL});
    }

    @Override
    protected void onStop() {
        super.onPause();
        refreshTask.stopTask();
        refreshTask.cancel(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void showGameSettings(View view) {

        LinearLayout gameSettings = (LinearLayout) findViewById(R.id.gameSettings);
        if (gameSettings.getVisibility() == View.VISIBLE) {
            gameSettings.setVisibility(View.GONE);
        } else {
            gameSettings.setVisibility(View.VISIBLE);
        }

    }

    public void createNewGame(View view) {

        if (gameName.getText().toString().length() == 0) {
            Toast.makeText(this, "Game Name is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startingStackSize.getText().toString().length() == 0) {
            Toast.makeText(this, "Starting Stack Size is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.valueOf(startingStackSize.getText().toString()) < 1) {
            Toast.makeText(this, "Starting Stack Size must be greater than 1!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newGameStartTime.length() == 0) {
            Toast.makeText(this, "Start Time is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        String postURL = SERVICE_URL + "/game/create";

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this, "Posting data...");
        wst.addNameValuePair("gameName", gameName.getText().toString());
        wst.addNameValuePair("startingStackSize", startingStackSize.getText().toString());
        wst.addNameValuePair("maxPlayerNumber", String.valueOf(playerNumPicker.getValue()));
        wst.addNameValuePair("startTime", newGameStartTime);

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{postURL});

        LinearLayout gameSettings = (LinearLayout) findViewById(R.id.gameSettings);
        gameSettings.setVisibility(View.GONE);
        gameName.setText("");
        startingStackSize.setText("");
        playerNumPicker.setValue(2);
    }

    public void searchGames(View view) {
        refreshTask.stopTask();
        refreshTask.cancel(true);
        EditText searchField = (EditText) findViewById(R.id.searchField);
        String searchString = searchField.getText() == null ? "" : searchField.getText().toString();

        String getURL = "";
        if ("".equals(searchString)) {
            getURL = SERVICE_URL + "/game";
        } else {
            getURL = SERVICE_URL + "/game/contains/" + searchString;
        }

        refreshTask = new RefreshGamesTask(this);
        refreshTask.execute(new String[]{getURL});

    }

    public void joinGame(String id) {

        String postURL = SERVICE_URL + "/game/join";

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this, "Posting data...");
        wst.addNameValuePair("userId", userId.toString());
        wst.addNameValuePair("gameId", id);

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{postURL});

    }

    public void leaveGame(String id) {

        String postURL = SERVICE_URL + "/game/leave";

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this, "Posting data...");
        wst.addNameValuePair("userId", userId.toString());
        wst.addNameValuePair("gameId", id);

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{postURL});

    }

    public void expandGroup(int groupPos) {
        list.expandGroup(groupPos);
    }

    public void collapseGroup(int groupPos) {
        list.collapseGroup(groupPos);
    }

    public void handleGameResponse(Response gameResponse) {

        if(null != gameResponse){

            List<Game> gameList = (List<Game>) gameResponse.getResponseObject();

            games = gameList;
            players = new HashMap<>();
            for (Game game : gameList) {
                players.put(game, game.getPlayers());
                if (game.getGameState().equals(GameState.STARTED)) {
                    for (Player player : game.getPlayers()) {
                        if (player.getUserId().equals(userId) && ! startedMultiPlayerActivity && game.getPlayers().size() > 1) {
                            refreshTask.stopTask();
                            startedMultiPlayerActivity = true;
                            Intent intent = new Intent(this, MultiPlayerActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putLong("userId", userId);
                            bundle.putLong("gameId", game.getGameId());
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }

            listAdapter.setJoinedGameId(-1);
            for(Game game : games){
                for(Player player : game.getPlayers()){
                    if(player.getUserId().equals(userId)){
                        listAdapter.setJoinedGameId(game.getGameId());
                    }
                }
            }

            listAdapter.refreshData(games, players);
            listAdapter.notifyDataSetChanged();
        }




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
                game.setStartTime(new Date(gameItem.getLong("startTime")));
                GameState gameState = GameState.valueOf(gameItem.getString("gameState"));
                game.setGameState(gameState);

                JSONArray playerArray = gameItem.getJSONArray("players");
                List<Player> playerList = new ArrayList<>();
                for (int counter = 0; counter < playerArray.length(); counter++) {
                    Player player = new Player();

                    JSONObject playerItem = playerArray.getJSONObject(counter);
                    player.setPlayerId(playerItem.getLong("playerId"));
                    player.setStackSize(playerItem.getInt("stackSize"));
                    player.setPlayerName(playerItem.getJSONObject("user").getString("userName"));
                    player.setUserId(playerItem.getJSONObject("user").getLong("userId"));
                    playerList.add(player);
                }
                game.setPlayers(playerList);
                gameList.add(game);
            }

            gameResponse.setResponseObject(gameList);
        }
        return gameResponse;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        DatePicker date = ((DateTimeDialog) dialog).getDate();
        TimePicker time = ((DateTimeDialog) dialog).getTime();
        int year = date.getYear();
        int month = date.getMonth() + 1;
        int day = date.getDayOfMonth();
        int hour = time.getCurrentHour();
        int minute = time.getCurrentMinute();
        startTime.setText(year + "/" + month + "/" + day + " " + hour + ":" + minute);
        newGameStartTime = (year + "-" + month + "-" + day + " " + hour + ":" + minute);
    }


    private class RefreshGamesTask extends RefreshTask {

        private RefreshGamesTask(Context context) {
            super(context);
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

    private class PostGameTask extends WebServiceTask {

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}



