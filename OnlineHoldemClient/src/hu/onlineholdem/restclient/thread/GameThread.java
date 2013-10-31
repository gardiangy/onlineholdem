package hu.onlineholdem.restclient.thread;

import android.content.Context;
import android.content.res.Resources;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import hu.onlineholdem.restclient.activity.SinglePlayerActivity;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.Suit;

public class GameThread extends Thread {

    private List<Card> deck = new ArrayList<>();
    private int screenWidth;
    private int screenHeight;
    private ImageView flop1;
    private ImageView flop2;
    private ImageView flop3;
    private ImageView turn;
    private ImageView river;
    private RelativeLayout board;
    private List<Player> players = new ArrayList<>();
    private Context context;
    private Resources resources;
    private String packageName;
    private SinglePlayerActivity activity;

    public GameThread(int screenWidth, int screenHeight, ImageView flop1, ImageView flop2, ImageView flop3, ImageView turn, ImageView river,
                      RelativeLayout board, List<Player> players, Context context, Resources resources, String packageName) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.flop1 = flop1;
        this.flop2 = flop2;
        this.flop3 = flop3;
        this.turn = turn;
        this.river = river;
        this.board = board;
        this.players = players;
        this.context = context;
        this.resources = resources;
        this.packageName = packageName;
        activity = (SinglePlayerActivity) context;
    }

    public void createGame() {
        for (int i = 1; i <= 13; i++) {
            for (Suit suit : Suit.values()) {
                Card card = new Card();
                card.setSuit(suit);
                card.setValue(i);
                deck.add(card);
            }
        }
        Collections.shuffle(deck, new Random());
    }

    public void dealFlop() {

        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);
        int res2Id = resources.getIdentifier(deck.get(1).toString(), "drawable", packageName);
        int res3Id = resources.getIdentifier(deck.get(2).toString(), "drawable", packageName);

        Animation flop1Anim = createAnimation(screenWidth / 2 - 300, screenHeight / 4);
        flop1.setAnimation(flop1Anim);
        flop1.setImageResource(resId);
        flop1.startAnimation(flop1Anim);
        deck.remove(0);

        Animation flop2Anim = createAnimation(screenWidth / 2 - 200, screenHeight / 4);
        flop2.setAnimation(flop2Anim);
        flop2.setImageResource(res2Id);
        flop2.startAnimation(flop2Anim);
        deck.remove(1);

        Animation flop3Anim = createAnimation(screenWidth / 2 - 100, screenHeight / 4);
        flop3.setAnimation(flop3Anim);
        flop3.setImageResource(res3Id);
        flop3.startAnimation(flop3Anim);
        deck.remove(2);

    }

    public void dealTurn() {
        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);

        Animation turnAnim = createAnimation(screenWidth / 2, screenHeight / 4);
        turn.setAnimation(turnAnim);
        turn.setImageResource(resId);
        turn.startAnimation(turnAnim);
        deck.remove(0);

    }

    public void dealRiver() {
        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);

        Animation riverAnim = createAnimation(screenWidth / 2 + 100, screenHeight / 4);
        river.setAnimation(riverAnim);
        river.setImageResource(resId);
        river.startAnimation(riverAnim);
        deck.remove(0);

    }

    public void deal() {

        for (Player player : players) {

            int resId = resources.getIdentifier(deck.get(deck.size() - 1).toString(), "drawable", packageName);

            TextView textView = player.getTextView();
            Animation card1Anim = createAnimation(textView.getRight() - 150, textView.getTop() - 40);
            ImageView card1 = new ImageView(context);
            board.addView(card1);
            card1.setAnimation(card1Anim);
            card1.setImageResource(resId);
            card1.startAnimation(card1Anim);
            deck.remove(deck.size() - 1);

            int res2Id = resources.getIdentifier(deck.get(deck.size() - 1).toString(), "drawable", packageName);
            Animation card2Anim = createAnimation(textView.getRight() - 110, textView.getTop() - 40);
            ImageView card2 = new ImageView(context);
            board.addView(card2);
            card2.setAnimation(card2Anim);
            card2.setImageResource(res2Id);
            card2.startAnimation(card2Anim);
            deck.remove(deck.size() - 1);

        }

    }

    public Animation createAnimation(int xTo, int yTo) {
        TranslateAnimation translateAnimation = new TranslateAnimation(screenWidth / 2, xTo, 0, yTo);

        translateAnimation.setRepeatMode(0);
        translateAnimation.setDuration(500);
        translateAnimation.setFillAfter(true);

        return translateAnimation;
    }

    @Override
    public void run() {

        createGame();
        try {
            Thread.sleep(1000);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    deal();
                }
            });
            Thread.sleep(3000);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    dealFlop();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
