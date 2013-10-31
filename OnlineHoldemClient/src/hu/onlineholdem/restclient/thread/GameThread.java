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

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.activity.SinglePlayerActivity;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;
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
    private TextView potsize;
    private RelativeLayout board;
    private List<Player> players = new ArrayList<>();
    private Context context;
    private Resources resources;
    private String packageName;
    private SinglePlayerActivity activity;
    private Game game;
    private Player actualPlayer;

    public GameThread(int screenWidth, int screenHeight, ImageView flop1, ImageView flop2, ImageView flop3, ImageView turn, ImageView river,
                      RelativeLayout board, List<Player> players,TextView potsize, Context context, Resources resources, String packageName) {
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
        this.potsize = potsize;
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
        game = new Game();
        game.setPlayers(players);
        game.setPotSize(0);
    }

    public void dealFlop() {

        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);
        int res2Id = resources.getIdentifier(deck.get(1).toString(), "drawable", packageName);
        int res3Id = resources.getIdentifier(deck.get(2).toString(), "drawable", packageName);

        Animation flop1Anim = createAnimation(screenWidth / 2,screenWidth / 2 - 300,0, screenHeight / 4,true);
        flop1.setAnimation(flop1Anim);
        flop1.setImageResource(resId);
        flop1.startAnimation(flop1Anim);
        deck.remove(0);

        Animation flop2Anim = createAnimation(screenWidth / 2,screenWidth / 2 - 200,0, screenHeight / 4,true);
        flop2.setAnimation(flop2Anim);
        flop2.setImageResource(res2Id);
        flop2.startAnimation(flop2Anim);
        deck.remove(1);

        Animation flop3Anim = createAnimation(screenWidth / 2,screenWidth / 2 - 100,0, screenHeight / 4,true);
        flop3.setAnimation(flop3Anim);
        flop3.setImageResource(res3Id);
        flop3.startAnimation(flop3Anim);
        deck.remove(2);

    }

    public void dealTurn() {
        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);

        Animation turnAnim = createAnimation(screenWidth / 2,screenWidth / 2, 0,screenHeight / 4,true);
        turn.setAnimation(turnAnim);
        turn.setImageResource(resId);
        turn.startAnimation(turnAnim);
        deck.remove(0);

    }

    public void dealRiver() {
        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);

        Animation riverAnim = createAnimation(screenWidth / 2,screenWidth / 2 + 100,0, screenHeight / 4,true);
        river.setAnimation(riverAnim);
        river.setImageResource(resId);
        river.startAnimation(riverAnim);
        deck.remove(0);

    }

    public void deal() {

        for (Player player : players) {

            int resId;
            if(player.isUser()){
                resId = resources.getIdentifier(deck.get(deck.size() - 1).toString(), "drawable", packageName);
            }else{
                resId = resources.getIdentifier("back", "drawable", packageName);
            }


            TextView textView = player.getTextView();
            Animation card1Anim = createAnimation(screenWidth / 2,textView.getRight() - 150,0, textView.getTop() - 40,true);
            ImageView card1 = new ImageView(context);
            board.addView(card1);
            card1.setAnimation(card1Anim);
            card1.setImageResource(resId);
            card1.startAnimation(card1Anim);
            deck.remove(deck.size() - 1);
            player.setCard1View(card1);

            int res2Id;
            if(player.isUser()){
                res2Id = resources.getIdentifier(deck.get(deck.size() - 1).toString(), "drawable", packageName);
            }else{
                res2Id = resources.getIdentifier("back", "drawable", packageName);
            }
            Animation card2Anim = createAnimation(screenWidth / 2,textView.getRight() - 110,0, textView.getTop() - 40,true);
            ImageView card2 = new ImageView(context);
            board.addView(card2);
            card2.setAnimation(card2Anim);
            card2.setImageResource(res2Id);
            card2.startAnimation(card2Anim);
            deck.remove(deck.size() - 1);
            player.setCard2View(card2);

        }

    }

    public Animation createAnimation(int xFrom,int xTo,int yFrom, int yTo,boolean fillAfter) {
        TranslateAnimation translateAnimation = new TranslateAnimation(xFrom, xTo, yFrom, yTo);

        translateAnimation.setRepeatMode(0);
        translateAnimation.setDuration(500);
        translateAnimation.setFillAfter(fillAfter);

        return translateAnimation;
    }
    public void makeMoves(){
        for(final Player player : players){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    player.getTextView().setBackgroundResource(R.drawable.seatactive);
                }
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!player.isUser()){
                Random random = new Random();
                int action = random.nextInt(3);
                switch (action){
                    case 0: player.setActionType(ActionType.BET);
                            player.setBetAmount(200);
                            break;
                    case 1: player.setActionType(ActionType.CHECK);
                            break;
                    case 2: player.setActionType(ActionType.FOLD);
                            break;
                    default:player.setActionType(ActionType.CHECK);
                }

                if(player.getActionType().equals(ActionType.BET)){
                    moveBet(player.getBetAmount(),player);
                }
                if(player.getActionType().equals(ActionType.FOLD)){
                    Animation card1Anim = createAnimation(player.getTextView().getRight() - 150,screenWidth / 2,player.getTextView().getTop() - 40,0,false);
                    player.getCard1View().setAnimation(card1Anim);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            player.getCard1View().startAnimation(player.getCard1View().getAnimation());
                            board.removeView(player.getCard1View());
                        }
                    });


                    Animation card2Anim = createAnimation(player.getTextView().getRight() - 110,screenWidth / 2,player.getTextView().getTop() - 40,0,false);
                    player.getCard2View().setAnimation(card2Anim);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            player.getCard2View().startAnimation(player.getCard2View().getAnimation());
                            board.removeView(player.getCard2View());
                        }
                    });
                }
            }
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    player.getTextView().setBackgroundResource(R.drawable.seatnotactive);
                }
            });


        }
    }

    public void moveBet(int amount,final Player player){
        game.setPotSize(game.getPotSize() + amount);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                potsize.setText(game.getPotSize().toString());
            }
        });
        player.setStackSize(player.getStackSize() - amount);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                player.getTextView().setText(player.getStackSize().toString());
            }
        });


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
            Thread.sleep(2000);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    dealFlop();
                }
            });

            makeMoves();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
