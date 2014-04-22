package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
import hu.onlineholdem.restclient.util.Position;

public class SinglePlayerActivity extends Activity {

    private static final String TAG = "SinglePlayerActivity";

    private GameThread gameThread;
    private int screenWidth;
    private int screenHeight;
    private RelativeLayout seats;
    private List<Player> players = new ArrayList<>();
    private int betAmount;
//    private int previousBetAmount;
    private Action highestBetAction;
    private SeekBar betBar;
    private DatabaseHandler dbHandler;

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG,"onRestart");
        Intent intent = new Intent(this, SinglePlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type", StartType.LOAD.name());
        intent.putExtras(bundle);

        startActivity(intent);
        finish();
    }

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
        gameThread.setRoundOver(true);
        gameThread.setStopThread(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.single_player_layout);
        ImageView flop1 = (ImageView) findViewById(R.id.flop1);
        ImageView flop2 = (ImageView) findViewById(R.id.flop2);
        ImageView flop3 = (ImageView) findViewById(R.id.flop3);
        ImageView turn = (ImageView) findViewById(R.id.turn);
        ImageView river = (ImageView) findViewById(R.id.river);
        TextView potSize = (TextView) findViewById(R.id.potSize);
        RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
        seats = (RelativeLayout) findViewById(R.id.seats);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        betBar = (SeekBar) findViewById(R.id.betBar);
        Button btnCheck = (Button) findViewById(R.id.btnCheck);
        Button btnBet = (Button) findViewById(R.id.btnBet);
        Button btnFold = (Button) findViewById(R.id.btnFold);
        final TextView betValue = (TextView) findViewById(R.id.betValue);

        gameThread = new GameThread(screenWidth, screenHeight, flop1, flop2, flop3, turn, river, board, players,
                potSize,btnCheck,btnBet,btnFold,betBar,betValue, this, getResources(), this.getPackageName());

        dbHandler = new DatabaseHandler(this);
        Bundle bundle = getIntent().getExtras();
        StartType type = StartType.valueOf(bundle.getString("type"));
        if(type.equals(StartType.LOAD)){
            List<Map<String, String>> playerDetails = dbHandler.getPlayerDetails();
            loadPlayers(playerDetails);
        } else {
            int numOfPlayers = bundle.getInt("numOfPlayers");
            Difficulty difficulty = Difficulty.valueOf(bundle.getString("difficulty"));
            createPlayers(numOfPlayers, difficulty);
        }

        gameThread.start();

        betBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                betValue.setText("" + (i + gameThread.getMinBet()));
                betAmount = i + gameThread.getMinBet();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
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

            TextView textView = new TextView(this);
            textView.setBackgroundResource(R.drawable.seatnotactive);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 5, screenHeight / 7);
            Position position = getPlayerPostion(i);
            layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);

            textView.setTop(position.getTop());
            textView.setLeft(position.getLeft());
            textView.setLayoutParams(layoutParams);
            textView.setText(player.getStackSize().toString());
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(0xFF000000);
            textView.setTextSize(15);

            player.setTextView(textView);
            players.add(player);

            seats.addView(textView);
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


            TextView textView = new TextView(this);
            textView.setBackgroundResource(R.drawable.seatnotactive);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 5, screenHeight / 6);
            Position position = getPlayerPostion(player.getPlayerId().intValue());
            layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);

            textView.setTop(position.getTop());
            textView.setLeft(position.getLeft());
            textView.setLayoutParams(layoutParams);
            textView.setText(player.getStackSize().toString());
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(0xFF000000);
            textView.setTextSize(15);

            player.setTextView(textView);
            this.players.add(player);

            seats.addView(textView);
        }

    }

    public void startGame(View view) {
        gameThread.start();
    }

    public Position getPlayerPostion(int pos) {
        switch (pos) {
            case 1:
                return new Position(screenWidth / 5 * 3, screenHeight / 15);
            case 2:
                return new Position(screenWidth / 10 * 8, screenHeight / 5);
            case 3:
                return new Position(screenWidth / 10 * 8, screenHeight / 7 * 3);
            case 4:
                return new Position(screenWidth / 25 * 16, screenHeight / 5 * 3);
            case 5:
                return new Position(screenWidth / 5 * 2, screenHeight / 5 * 3);
            case 6:
                return new Position(screenWidth / 6, screenHeight / 5 * 3);
            case 7:
                return new Position(screenWidth / 70, screenHeight / 7 * 3);
            case 8:
                return new Position(screenWidth / 70, screenHeight / 5);
            case 9:
                return new Position(screenWidth / 5, screenHeight / 15);
        }
        return null;
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
