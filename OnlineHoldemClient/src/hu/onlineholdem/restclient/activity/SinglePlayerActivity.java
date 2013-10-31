package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.thread.GameThread;
import hu.onlineholdem.restclient.util.TablePosition;

public class SinglePlayerActivity extends Activity{

    private static final String TAG = "SinglePlayerActivity";

    private GameThread gameThread;
    private int screenWidth;
    private int screenHeight;
    private RelativeLayout seats;
    private List<Player> players = new ArrayList<>();

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
        RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
        seats = (RelativeLayout) findViewById(R.id.seats);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        gameThread = new GameThread(screenWidth, screenHeight, flop1, flop2, flop3, turn, river, board, players,this,getResources(),this.getPackageName());
        createPlayers(7);

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

    public void createPlayers(int numberOfPlayers){
        for(int i=1;i<=numberOfPlayers;i++){
            Player player = new Player();
            player.setStackSize(1500);
            player.setOrder(i);
            if(i == 5){
                player.setUser(true);
            }

            TextView textView = new TextView(this);
            if(player.isUser()){
                textView.setBackgroundResource(R.drawable.seatactive);
            }else{
                textView.setBackgroundResource(R.drawable.seatnotactive);
            }


            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(200,100);
            TablePosition tablePosition = getPlayerPostion(i);
            layoutParams.setMargins(tablePosition.getLeft(),tablePosition.getTop(),0,0);
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

    public void startGame(View view){
        gameThread.start();
    }

    public TablePosition getPlayerPostion(int order){
        switch (order){
            case 1: return new TablePosition(screenWidth/6*5,screenHeight/14);
            case 2: return new TablePosition(screenWidth/7*6,screenHeight/3);
            case 3: return new TablePosition(screenWidth/3*2,screenHeight/7*4);
            case 4: return new TablePosition(screenWidth/5*2,screenHeight/7*4);
            case 5: return new TablePosition(screenWidth/6,screenHeight/7*4);
            case 6: return new TablePosition(screenWidth/40,screenHeight/3);
            case 7: return new TablePosition(screenWidth/18,screenHeight/14);
        }
        return null;
    }




}
