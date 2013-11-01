package hu.onlineholdem.restclient.thread;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.activity.SinglePlayerActivity;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;
import hu.onlineholdem.restclient.enums.Suit;
import hu.onlineholdem.restclient.util.Position;

public class GameThread extends Thread {

    private List<Card> deck = new ArrayList<>();
    private int screenWidth;
    private int screenHeight;
    private ImageView flop1;
    private ImageView flop2;
    private ImageView flop3;
    private ImageView turn;
    private ImageView river;
    private Button btnCheck;
    private Button btnBet;
    private Button btnFold;
    private SeekBar betBar;
    private TextView potsize;
    private TextView betValue;
    private RelativeLayout board;
    private List<Player> players = new ArrayList<>();
    private List<Player> playersInRound;
    private Context context;
    private Resources resources;
    private String packageName;
    private SinglePlayerActivity activity;
    private Game game;
    private boolean isPlayerTurn;
    private ActionType playerAction;
    private ActionType previousAction;
    private int playerBetAmount;
    private int previousBetAmount;

    public GameThread(int screenWidth, int screenHeight, ImageView flop1, ImageView flop2, ImageView flop3, ImageView turn, ImageView river,
                      RelativeLayout board, List<Player> players,TextView potsize,Button btnCheck,Button btnBet,Button btnFold,
                      SeekBar betBar,TextView betValue,Context context, Resources resources, String packageName) {
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
        this.btnCheck = btnCheck;
        this.btnBet = btnBet;
        this.btnFold = btnFold;
        this.betBar = betBar;
        this.betValue = betValue;
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

        playersInRound = new ArrayList<>();
        playersInRound.addAll(players);
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
        List<Player> playerList = new ArrayList<>();
        playerList.addAll(playersInRound);
        for(final Player player : playerList){
            final List<ActionType> availableActions = getAvailableActions();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    player.getTextView().setBackgroundResource(R.drawable.seatactive);
                }
            });
            if(!player.isUser()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Random random = new Random();
                int action = random.nextInt(availableActions.size());
                ActionType randomAction = availableActions.get(action);
                if(randomAction.equals(ActionType.CHECK)){
                    previousAction = ActionType.CHECK;
                    player.setActionType(ActionType.CHECK);
                }
                if(randomAction.equals(ActionType.BET)){
                    player.setActionType(ActionType.BET);
                    player.setBetAmount(200);
                    previousBetAmount = player.getBetAmount();
                    activity.setPreviousBetAmount(previousBetAmount);
                    previousAction = ActionType.BET;
                    moveBet(player.getBetAmount(),player);
                }
                if(randomAction.equals(ActionType.FOLD)){
                    player.setActionType(ActionType.FOLD);
                    moveFold(player);
                }
                if(randomAction.equals(ActionType.CALL)){
                    previousAction = ActionType.CALL;
                    player.setActionType(ActionType.CALL);
                    player.setBetAmount(previousBetAmount);
                    moveBet(previousBetAmount,player);
                }
                if(randomAction.equals(ActionType.RAISE)){
                    previousAction = ActionType.RAISE;
                    player.setActionType(ActionType.RAISE);
                    if(previousBetAmount * 2 > 1500){
                        player.setBetAmount(1500);
                        previousBetAmount = 1500;
                        moveBet(1500,player);
                    }else{
                        player.setBetAmount(previousBetAmount * 2);
                        moveBet(previousBetAmount * 2,player);
                        previousBetAmount = previousBetAmount *2;
                    }

                }
            }else{
                isPlayerTurn = true;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        if(availableActions.contains(ActionType.CALL)){
                            btnCheck.setText("CALL");
                        }else{
                            btnCheck.setText("CHECK");
                        }
                        if(availableActions.contains(ActionType.RAISE)){
                            btnBet.setText("RAISE");
                        }else{
                            btnBet.setText("BET");
                        }
                    }
                });

                showActionButtons(true);

                while (isPlayerTurn){
                    try {
                        if(null != playerAction){
                            if(playerAction.equals(ActionType.BET)){
                                moveBet(playerBetAmount,player);
                                previousAction = ActionType.BET;
                                previousBetAmount = playerBetAmount;
                            }
                            if(playerAction.equals(ActionType.CALL)){
                                moveBet(previousBetAmount,player);
                                previousAction = ActionType.CALL;
                            }
                            if(playerAction.equals(ActionType.RAISE)){
                                moveBet(player.getBetAmount(),player);
                                previousAction = ActionType.RAISE;
                                previousBetAmount = player.getBetAmount();
                            }
                            if(playerAction.equals(ActionType.FOLD)){
                                moveFold(player);
                            }
                            isPlayerTurn = false;
                            showActionButtons(false);
                            playerAction = null;
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
        player.setBetAmount(amount);
        player.setStackSize(player.getStackSize() - amount);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                potsize.setText(game.getPotSize().toString());
                TextView chipsView = new TextView(context);
                chipsView.setBackgroundResource(R.drawable.chips);
                chipsView.setText(player.getBetAmount().toString());

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(40,40);
                Position position = getChipsPostion(player);
                layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);
                chipsView.setLayoutParams(layoutParams);
                board.addView(chipsView);

                player.getTextView().setText(player.getStackSize().toString());
            }
        });

    }

    public void moveFold(final Player player){
        Animation card1Anim = createAnimation(player.getTextView().getRight() - 150,screenWidth / 2,player.getTextView().getTop() - 40,0,false);
        player.getCard1View().setAnimation(card1Anim);

        Animation card2Anim = createAnimation(player.getTextView().getRight() - 110,screenWidth / 2,player.getTextView().getTop() - 40,0,false);
        player.getCard2View().setAnimation(card2Anim);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                player.getCard1View().startAnimation(player.getCard1View().getAnimation());
                board.removeView(player.getCard1View());

                player.getCard2View().startAnimation(player.getCard2View().getAnimation());
                board.removeView(player.getCard2View());
            }
        });
        playersInRound.remove(player);
    }

    public void showActionButtons(final boolean show){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if(show){
                    btnCheck.setVisibility(View.VISIBLE);
                    btnBet.setVisibility(View.VISIBLE);
                    btnFold.setVisibility(View.VISIBLE);
                    betBar.setVisibility(View.VISIBLE);
                    betValue.setVisibility(View.VISIBLE);
                }else{
                    btnCheck.setVisibility(View.INVISIBLE);
                    btnBet.setVisibility(View.INVISIBLE);
                    btnFold.setVisibility(View.INVISIBLE);
                    betBar.setVisibility(View.INVISIBLE);
                    betValue.setVisibility(View.INVISIBLE);
                    betValue.setText("");
                }

            }
        });
    }

    @Override
    public void run() {

        createGame();
        try {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    deal();
                }
            });
            Thread.sleep(1000);
            makeMoves();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    dealFlop();
                }
            });
            makeMoves();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    dealTurn();
                }
            });
            makeMoves();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    dealRiver();
                }
            });
            makeMoves();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public Position getChipsPostion(Player player) {
        switch (player.getOrder()) {
            case 1:
                return new Position(player.getTextView().getLeft() - 70, player.getTextView().getTop() + 20);
            case 2:
                return new Position(player.getTextView().getLeft() - 50, player.getTextView().getTop() - 20);
            case 3:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 100);
            case 4:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 100);
            case 5:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 100);
            case 6:
                return new Position(player.getTextView().getLeft() + 220, player.getTextView().getTop() + 20);
            case 7:
                return new Position(player.getTextView().getLeft() + 220, player.getTextView().getTop() + 20);
        }
        return null;
    }

    public List<ActionType> getAvailableActions(){
        List<ActionType> availableActions = new ArrayList<>();
        if(null == previousAction || previousAction.equals(ActionType.CHECK) || previousAction.equals(ActionType.FOLD)){
            availableActions.add(ActionType.CHECK);
            availableActions.add(ActionType.BET);
            availableActions.add(ActionType.FOLD);
        }
        if(null != previousAction){
            if(previousAction.equals(ActionType.BET) || previousAction.equals(ActionType.CALL) || previousAction.equals(ActionType.RAISE)){
                availableActions.add(ActionType.CALL);
                availableActions.add(ActionType.RAISE);
                availableActions.add(ActionType.FOLD);
            }
        }

        return availableActions;
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public void setPlayerTurn(boolean isPlayerTurn) {
        this.isPlayerTurn = isPlayerTurn;
    }

    public ActionType getPlayerAction() {
        return playerAction;
    }

    public void setPlayerAction(ActionType playerAction) {
        this.playerAction = playerAction;
    }

    public int getPlayerBetAmount() {
        return playerBetAmount;
    }

    public void setPlayerBetAmount(int playerBetAmount) {
        this.playerBetAmount = playerBetAmount;
    }
}
