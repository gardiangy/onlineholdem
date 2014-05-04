package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Action;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;
import hu.onlineholdem.restclient.enums.Difficulty;
import hu.onlineholdem.restclient.enums.PlayStyle;
import hu.onlineholdem.restclient.enums.StartType;
import hu.onlineholdem.restclient.thread.GameThread;
import hu.onlineholdem.restclient.util.DatabaseHandler;
import hu.onlineholdem.restclient.util.GraphicStuff;

public class SinglePlayerActivity extends Activity {

    private static final String TAG = "SinglePlayerActivity";

    private GameThread gameThread;
    private RelativeLayout seats;
    private List<Player> players = new ArrayList<>();
    private int betAmount;
    private Action highestBetAction;
    private SeekBar betBar;
    private DatabaseHandler dbHandler;
    private GraphicStuff graphs;

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onStop");
        dbHandler.resetTables();
        for(Player player : gameThread.getPlayers()){
            dbHandler.addPlayer(player);
        }
        List<Map<String, String>> playerDetails = dbHandler.getPlayerDetails();
        Log.i(TAG,"DataBase");
        for(Map<String, String> player : playerDetails){
            Log.i(TAG, "Player id | " + player.get("player_id"));
            Log.i(TAG, "Player order | " + player.get("player_order"));
            Log.i(TAG, "Player stackSize | " + player.get("player_stack_size"));
            Log.i(TAG, "Player playerSyle | " + player.get("player_style"));
            Log.i(TAG, "Player isUser | " + player.get("player_is_user"));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.single_player_layout);
        TextView potSize = (TextView) findViewById(R.id.potSize);
        seats = (RelativeLayout) findViewById(R.id.seats);
        betBar = (SeekBar) findViewById(R.id.betBar);
        Button btnCheck = (Button) findViewById(R.id.btnCheck);
        Button btnBet = (Button) findViewById(R.id.btnBet);
        Button btnFold = (Button) findViewById(R.id.btnFold);
        final TextView betValue = (TextView) findViewById(R.id.betValue);

        graphs = new GraphicStuff(this);

        gameThread = new GameThread(players,potSize,btnCheck,btnBet,btnFold,betBar,betValue, this,graphs);

        dbHandler = new DatabaseHandler(this);
        Bundle bundle = getIntent().getExtras();
        StartType type = StartType.valueOf(bundle.getString("type"));
        if(type.equals(StartType.NEW)){
            int numOfPlayers = bundle.getInt("numOfPlayers");
            Difficulty difficulty = Difficulty.valueOf(bundle.getString("difficulty"));
            createPlayers(numOfPlayers, difficulty);
        } else {
            List<Map<String, String>> playerDetails = dbHandler.getPlayerDetails();
            loadPlayers(playerDetails);
        }

        gameThread.start();

        betBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                betValue.setText("" + (i + gameThread.getMinBet()));
                betAmount = i + gameThread.getMinBet();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    public void createPlayers(int numberOfPlayers, Difficulty difficulty) {
        Log.i(TAG, "Game started, difficulty: " + difficulty.toString());
        for (int i = 1; i <= numberOfPlayers; i++) {
            Player player = new Player();
            player.setPlayerId((long)i);
            player.setStackSize(1500);
            player.setAmountInPot(0);
            player.setOrder(i);
            player.setPosition(i);
            if (i == numberOfPlayers / 2) {
                player.setIsUser(true);
                player.setPlayStyle(PlayStyle.USER);
            } else {
                player.setIsUser(false);
            }

            if(!player.isUser()){
                switch (difficulty){
                    case EASY:
                        if(i % 2 == 0){
                            player.setPlayStyle(PlayStyle.CALLING_STATION);
                        } else {
                            player.setPlayStyle(PlayStyle.MANIAC);
                        }
                        break;
                    case NORMAL:
                        if(i % 4 == 0){
                            player.setPlayStyle(PlayStyle.SHARK);
                        } else if (i % 2 == 0){
                            player.setPlayStyle(PlayStyle.CALLING_STATION);
                        } else if(i % 3 == 0) {
                            player.setPlayStyle(PlayStyle.ROCK);
                        } else {
                            player.setPlayStyle(PlayStyle.MANIAC);
                        }
                        break;
                    case HARD:
                        if(i % 2 == 0){
                            player.setPlayStyle(PlayStyle.ROCK);
                        } else {
                            player.setPlayStyle(PlayStyle.SHARK);
                        }
                        break;
                }
                Log.i(TAG, "Player id " + player.getPlayerId() + " style: " + player.getPlayStyle().toString());
            }
            player.setTextView(graphs.createPlayerView(player));
            players.add(player);
        }

    }

    public void loadPlayers(List<Map<String,String>> players) {
        for (Map<String,String> pl : players) {
            Player player = new Player();
            player.setStackSize(Integer.valueOf(pl.get("player_stack_size")));
            player.setPlayerId(Long.valueOf(pl.get("player_id")));
            player.setAmountInPot(0);
            player.setOrder(Integer.valueOf(pl.get("player_order")));
            player.setPlayStyle(PlayStyle.valueOf(pl.get("player_style")));
            if (Boolean.valueOf(pl.get("player_is_user"))){
                player.setIsUser(true);
            } else {
                player.setIsUser(false);
            }

            Log.i(TAG, "Player id " + player.getPlayerId() + " style: " + player.getPlayStyle().toString());

            player.setTextView(graphs.createPlayerView(player));
            this.players.add(player);
        }

    }

    public void startGame(View view) {
        gameThread.start();
    }

    public void moveCheck(View view){
        TextView textView = (TextView) view;
        if("CALL".equals(textView.getText())){
            gameThread.setPlayerAction(ActionType.CALL);
            gameThread.setPlayerBetAmount(highestBetAction.getBetValue());
        } else if ("ALL IN".equals(textView.getText())){
            gameThread.setPlayerAction(ActionType.ALL_IN);
        } else {
            gameThread.setPlayerAction(ActionType.CHECK);
        }

    }

    public void moveBet(View view){
        TextView textView = (TextView) view;
        if("RAISE".equals(textView.getText())){
            gameThread.setPlayerAction(ActionType.RAISE);
        }else{
            gameThread.setPlayerAction(ActionType.BET);
        }
        gameThread.setPlayerBetAmount(betAmount);
    }

    public void moveFold(View view){
        gameThread.setPlayerAction(ActionType.FOLD);
    }

    public void removeSeat(TextView textView){
        seats.removeView(textView);
    }

    public Action getHighestBetAction() {
        return highestBetAction;
    }

    public void setHighestBetAction(Action highestBetAction) {
        this.highestBetAction = highestBetAction;
    }

    public SeekBar getBetBar() {
        return betBar;
    }

    public void setBetBar(SeekBar betBar) {
        this.betBar = betBar;
    }
}
