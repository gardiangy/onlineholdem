package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_player_layout);
        flop1 = (ImageView) findViewById(R.id.flop1);
        flop2 = (ImageView) findViewById(R.id.flop2);
        flop3 = (ImageView) findViewById(R.id.flop3);
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
    public void deal(View view){
        Collections.shuffle(deck,new Random());
        int resId = getResources().getIdentifier(deck.get(0).toString(), "drawable", this.getPackageName());

        flop1.clearAnimation();
        Animation flop1Anim = createAnimation(screenWidth/2 - 20,screenHight/2);
        flop1.setAnimation(flop1Anim);
        flop1.startAnimation(flop1Anim);
        flop1.setImageResource(resId);

        resId = getResources().getIdentifier(deck.get(1).toString(), "drawable", this.getPackageName());
        flop2.clearAnimation();
        Animation flop2Anim = createAnimation(screenWidth/2,screenHight/2);
        flop2.setAnimation(flop2Anim);
        flop2.startAnimation(flop2Anim);
        flop2.setImageResource(resId);

        resId = getResources().getIdentifier(deck.get(2).toString(), "drawable", this.getPackageName());
        flop3.clearAnimation();
        Animation flop3Anim = createAnimation(screenWidth/2 + 20 ,screenHight/2);
        flop3.setAnimation(flop3Anim);
        flop3.startAnimation(flop3Anim);
        flop3.setImageResource(resId);
    }

    public Animation createAnimation(int xTo, int yTo) {
        TranslateAnimation translateAnimation = new TranslateAnimation(0,xTo,0,yTo);

        translateAnimation.setDuration(1000);

        return  translateAnimation;
    }

}
