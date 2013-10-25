package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.enums.Suit;

public class SinglePlayerActivity extends Activity{

    private static final String TAG = "SinglePlayerActivity";

    private List<Card> deck = new ArrayList<>();
    private int screenWidth;
    private int screenHight;
    private int scaledCardWidth;
    private int scaledCardHeight;
    private ImageView flop1;
    private ImageView flop2;
    private ImageView flop3;
    private ImageView turn;
    private ImageView river;
    private RelativeLayout board;
    private RelativeLayout seats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.single_player_layout);
        flop1 = (ImageView) findViewById(R.id.flop1);
        flop2 = (ImageView) findViewById(R.id.flop2);
        flop3 = (ImageView) findViewById(R.id.flop3);
        turn = (ImageView) findViewById(R.id.turn);
        river = (ImageView) findViewById(R.id.river);
        board = (RelativeLayout) findViewById(R.id.board);
        seats = (RelativeLayout) findViewById(R.id.seats);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHight = size.y;

        for(int i=1;i<=13;i++){
            for(Suit suit : Suit.values()){
                Card card = new Card();
                card.setSuit(suit);
                card.setValue(i);
                deck.add(card);
            }
        }
        Collections.shuffle(deck,new Random());
        TextView player1 = new TextView(this);
        player1.setBackgroundResource(R.drawable.seatnotactive);

        RelativeLayout.LayoutParams player1layoutParams = new RelativeLayout.LayoutParams(200,100);
        player1layoutParams.setMargins(screenWidth/18,screenHight/14,0,0);
        player1.setLayoutParams(player1layoutParams);

        seats.addView(player1);

        TextView player2 = new TextView(this);
        player2.setBackgroundResource(R.drawable.seatnotactive);
        RelativeLayout.LayoutParams player2layoutParams = new RelativeLayout.LayoutParams(200,100);
        player2layoutParams.setMargins(screenWidth/40,screenHight/3,0,0);
        player2.setLayoutParams(player2layoutParams);

        seats.addView(player2);

        TextView player3 = new TextView(this);
        player3.setBackgroundResource(R.drawable.seatnotactive);
        RelativeLayout.LayoutParams player3layoutParams = new RelativeLayout.LayoutParams(200,100);
        player3layoutParams.setMargins(screenWidth/6,screenHight/7*4,0,0);
        player3.setLayoutParams(player3layoutParams);

        seats.addView(player3);

        TextView player4 = new TextView(this);
        player4.setBackgroundResource(R.drawable.seatnotactive);
        RelativeLayout.LayoutParams player4layoutParams = new RelativeLayout.LayoutParams(200,100);
        player4layoutParams.setMargins(screenWidth/5*2,screenHight/7*4,0,0);
        player4.setLayoutParams(player4layoutParams);

        seats.addView(player4);


        TextView player5 = new TextView(this);
        player5.setBackgroundResource(R.drawable.seatnotactive);
        RelativeLayout.LayoutParams player5layoutParams = new RelativeLayout.LayoutParams(200,100);
        player5layoutParams.setMargins(screenWidth/3*2,screenHight/7*4,0,0);
        player5.setLayoutParams(player5layoutParams);

        seats.addView(player5);

        TextView player6 = new TextView(this);
        player6.setBackgroundResource(R.drawable.seatnotactive);
        RelativeLayout.LayoutParams player6layoutParams = new RelativeLayout.LayoutParams(200,100);
        player6layoutParams.setMargins(screenWidth/7*6,screenHight/3,0,0);
        player6.setLayoutParams(player6layoutParams);

        seats.addView(player6);

        TextView player7 = new TextView(this);
        player7.setBackgroundResource(R.drawable.seatnotactive);
        RelativeLayout.LayoutParams player7layoutParams = new RelativeLayout.LayoutParams(200,100);
        player7layoutParams.setMargins(screenWidth/6*5,screenHight/14,0,0);
        player7.setLayoutParams(player7layoutParams);

        seats.addView(player7);

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
    public void dealFlop(View view){

        int resId = getResources().getIdentifier(deck.get(0).toString(), "drawable", this.getPackageName());
        int res2Id = getResources().getIdentifier(deck.get(1).toString(), "drawable", this.getPackageName());
        int res3Id = getResources().getIdentifier(deck.get(2).toString(), "drawable", this.getPackageName());

        Animation flop1Anim = createAnimation(screenWidth/2 - 300,screenHight/4);
        flop1.setAnimation(flop1Anim);
        flop1.setImageResource(resId);
        flop1.startAnimation(flop1Anim);
        deck.remove(0);

        Animation flop2Anim = createAnimation(screenWidth/2 - 200,screenHight/4);
        flop2.setAnimation(flop2Anim);
        flop2.setImageResource(res2Id);
        flop2.startAnimation(flop2Anim);
        deck.remove(1);

        Animation flop3Anim = createAnimation(screenWidth/2 - 100,screenHight/4);
        flop3.setAnimation(flop3Anim);
        flop3.setImageResource(res3Id);
        flop3.startAnimation(flop3Anim);
        deck.remove(2);

    }

    public void dealTurn(View view){
        int resId = getResources().getIdentifier(deck.get(0).toString(), "drawable", this.getPackageName());

        Animation turnAnim = createAnimation(screenWidth/2,screenHight/4);
        turn.setAnimation(turnAnim);
        turn.setImageResource(resId);
        turn.startAnimation(turnAnim);
        deck.remove(0);

    }

    public void dealRiver(View view){
        int resId = getResources().getIdentifier(deck.get(0).toString(), "drawable", this.getPackageName());

        Animation riverAnim = createAnimation(screenWidth/2 + 100,screenHight/4);
        river.setAnimation(riverAnim);
        river.setImageResource(resId);
        river.startAnimation(riverAnim);
        deck.remove(0);

    }

    public void deal(View view){

        for(int i = 0; i< seats.getChildCount();i++){
            if(seats.getChildAt(i) instanceof TextView){
                int resId = getResources().getIdentifier(deck.get(deck.size() - 1).toString(), "drawable", this.getPackageName());

                TextView player = (TextView)seats.getChildAt(i);
                Animation card1Anim = createAnimation(player.getRight() - 150,player.getTop() - 40);
                ImageView card1 = new ImageView(this);
                board.addView(card1);
                card1.setAnimation(card1Anim);
                card1.setImageResource(resId);
                card1.startAnimation(card1Anim);
                deck.remove(deck.size() - 1);

                int res2Id = getResources().getIdentifier(deck.get(deck.size() - 1).toString(), "drawable", this.getPackageName());
                Animation card2Anim = createAnimation(player.getRight() - 110,player.getTop() - 40);
                ImageView card2 = new ImageView(this);
                board.addView(card2);
                card2.setAnimation(card2Anim);
                card2.setImageResource(res2Id);
                card2.startAnimation(card2Anim);
                deck.remove(deck.size() - 1);
            }
        }

    }

    public Animation createAnimation(int xTo, int yTo) {
        TranslateAnimation translateAnimation = new TranslateAnimation(screenWidth/2,xTo,0,yTo);

        translateAnimation.setRepeatMode(0);
        translateAnimation.setDuration(500);
        translateAnimation.setFillAfter(true);

        return  translateAnimation;
    }

}
