package hu.onlineholdem.restclient.thread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.List;
import java.util.Random;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.activity.MenuActivity;
import hu.onlineholdem.restclient.activity.SinglePlayerActivity;
import hu.onlineholdem.restclient.activity.SinglePlayerSettingsActivity;
import hu.onlineholdem.restclient.entity.Action;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;
import hu.onlineholdem.restclient.enums.Suit;
import hu.onlineholdem.restclient.util.EvaluatedHand;
import hu.onlineholdem.restclient.util.HandEvaluator;
import hu.onlineholdem.restclient.util.Position;

public class GameThread extends Thread {

    private static final String TAG = "GameThread";

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
    //    private ActionType previousAction;
    private int playerBetAmount;
    //    private int previousBetAmount;
    private Action highestBetAction;
    private int minBet;
    private boolean roundOver = false;
    private boolean flopDealt = false;
    private boolean turnDealt = false;
    private boolean riverDealt = false;
    private boolean splitPot = false;
    private boolean stopThread = false;

    public GameThread(int screenWidth, int screenHeight, ImageView flop1, ImageView flop2, ImageView flop3, ImageView turn, ImageView river,
                      RelativeLayout board, List<Player> players, TextView potsize, Button btnCheck, Button btnBet, Button btnFold,
                      SeekBar betBar, TextView betValue, Context context, Resources resources, String packageName) {
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
        game = new Game();
        game.setPlayers(players);
        game.setPotChips(new ArrayList<RelativeLayout>());
    }

    public void shuffle() {
        deck.clear();
        for (int i = 1; i <= 13; i++) {
            for (Suit suit : Suit.values()) {
                Card card = new Card();
                card.setSuit(suit);
                card.setValue(i);
                deck.add(card);
            }
        }
        Collections.shuffle(deck, new Random());
        playersInRound = new ArrayList<>();
        playersInRound.addAll(players);
        game.setPotSize(0);
        game.setBoard(new ArrayList<Card>());
        minBet = 30;
    }

    public void startRound() throws InterruptedException {
        roundOver = false;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                deal();
            }
        });
        Thread.sleep(1000);
        makeMoves();
        while (makeMovesAgain()) {
            makeMoves();
        }
        while (!roundOver) {
            Thread.sleep(2000);
            if (!flopDealt) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        dealFlop();
                    }
                });
                flopDealt = true;
                minBet = 30;
            } else if (!turnDealt) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        dealTurn();
                    }
                });
                turnDealt = true;
                minBet = 30;
            } else if (!riverDealt) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        dealRiver();
                    }
                });
                riverDealt = true;
                roundOver = true;
                minBet = 30;
            }
            Thread.sleep(2000);
            makeMoves();
            while (makeMovesAgain()) {
                makeMoves();
            }
        }
        if (roundOver) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    endBettingRound();
                }
            });

        }
    }

    public List<Player> evaluateRound() {

        for (final Player player : playersInRound) {
            final EvaluatedHand evaluatedHand = HandEvaluator.evaluateHand(game.getBoard(), player.getCardOne(), player.getCardTwo());
            player.setEvaluatedHand(evaluatedHand);
        }

        EvaluatedHand bestHand = playersInRound.get(0).getEvaluatedHand();
        Player winner = playersInRound.get(0);
        List<Player> winners = new ArrayList<>();
        for (Player player : playersInRound) {
            if (player.getOrder().equals(winner.getOrder())) {
                continue;
            }
            if (HandEvaluator.isBetterHand(player.getEvaluatedHand(), bestHand)) {
                bestHand = player.getEvaluatedHand();
                winner = player;
                splitPot = false;
            }
        }
        for (Player player : playersInRound) {
            if (player.getOrder().equals(winner.getOrder())) {
                continue;
            }
            if (player.getEvaluatedHand().getHandStrength().getStrength().equals(bestHand.getHandStrength().getStrength())
                    && player.getEvaluatedHand().getValue().equals(bestHand.getValue())) {
                if (null != player.getEvaluatedHand().getHighCards() && null != winner.getEvaluatedHand().getHighCards()) {
                    int equalCardNum = 0;
                    for (int i = 0; i < player.getEvaluatedHand().getHighCards().size(); i++) {
                        Card playerHighCard = player.getEvaluatedHand().getHighCards().get(i);
                        Card winnerHighCard = winner.getEvaluatedHand().getHighCards().get(i);
                        if (playerHighCard.getValue() > winnerHighCard.getValue()
                                || (playerHighCard.getValue() == 1 && winnerHighCard.getValue() != 1)) {
                            bestHand = player.getEvaluatedHand();
                            winner = player;
                            break;
                        }
                        if (playerHighCard.getValue() < winnerHighCard.getValue()) {
                            break;
                        }
                        if (playerHighCard.getValue().equals(winnerHighCard.getValue())) {
                            equalCardNum++;
                        }
                    }
                    if (equalCardNum == player.getEvaluatedHand().getHighCards().size()) {
                        splitPot = true;
                        winners.add(player);
                    }
                } else {
                    splitPot = true;
                    winners.add(player);
                }
            }
        }
        winners.add(winner);
        for (Player w : winners) {
            Log.i(TAG, "Winner : pl.order " + w.getOrder());
            Log.i(TAG, "Winner : card " + w.getEvaluatedHand().getHandStrength());
            Log.i(TAG, w.getEvaluatedHand().getValue().toString());
            if (null != w.getEvaluatedHand().getHighCards()) {
                Log.i(TAG, w.getEvaluatedHand().getHighCards().toString());
            }

        }

        return winners;

    }

    public void showCards() {
        Handler handler = new Handler(Looper.getMainLooper());
        for (final Player player : playersInRound) {
            if (!player.isUser()) {
                final int resId = resources.getIdentifier(player.getCardOne().toString(), "drawable", packageName);
                final int res2Id = resources.getIdentifier(player.getCardTwo().toString(), "drawable", packageName);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        player.getCard1View().setImageResource(resId);
                        player.getCard2View().setImageResource(res2Id);
                    }
                });

            }
        }
        final List<Player> winners = evaluateRound();
        for (Player playerOne : players) {
            int amountToWin = 0;
            for (Player playerTwo : players) {
                amountToWin += playerTwo.getAmountInPot() >= playerOne.getAmountInPot()
                        ? playerOne.getAmountInPot() : playerTwo.getAmountInPot();
            }
            playerOne.setAmountToWin(amountToWin);
            Log.i(TAG, "Player order: " + playerOne.getOrder() + " AmountToWin " + amountToWin);

        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                board.invalidate();
                assignChips(winners);
            }
        });

    }

    public void assignChips(List<Player> winners) {
        if (splitPot) {
            List<List<RelativeLayout>> chipsList = splitChips(game.getPotChips(), winners.size());

            int splitPotAmount = game.getPotSize() / winners.size();

            List<Player> winnerList = new ArrayList<>();
            winnerList.addAll(winners);

            for (Player winner : winnerList) {
                if (winner.getAmountToWin() <= splitPotAmount) {
                    Log.i(TAG, "PotSize " + game.getPotSize());
                    Log.i(TAG, "winner amount to win " + winner.getAmountToWin());
                    game.setPotSize(game.getPotSize() - winner.getAmountToWin());
                    for (RelativeLayout chips : chipsList.get(0)) {
                        chips.animate().setDuration(500).x(winner.getTextView().getLeft()).y(winner.getTextView().getTop());
                    }
                    chipsList.remove(chipsList.get(0));
                    winner.setStackSize(winner.getStackSize() + winner.getAmountToWin());
                    winners.remove(winner);
                }
            }
            for (Player winner : winners) {
                List<List<RelativeLayout>> layoutList = new ArrayList<>();
                layoutList.addAll(chipsList);
                Log.i(TAG, "PotSize " + game.getPotSize());
                Log.i(TAG, "winner amount to win " + winner.getAmountToWin());
                for (List<RelativeLayout> relativeLayouts : layoutList) {
                    for (RelativeLayout chips : relativeLayouts) {
                        chips.animate().setDuration(500).x(winner.getTextView().getLeft()).y(winner.getTextView().getTop());
                    }
                    chipsList.remove(relativeLayouts);
                }
                winner.setStackSize(winner.getStackSize() + game.getPotSize() / winners.size());
            }
        } else {
            Log.i(TAG, "winner amount to win " + winners.get(0).getAmountToWin());
            Log.i(TAG, "PotSize " + game.getPotSize());
            if (winners.get(0).getAmountToWin() >= game.getPotSize()) {
                for (final RelativeLayout chips : game.getPotChips()) {
                    chips.animate().setDuration(500).x(winners.get(0).getTextView().getLeft()).y(winners.get(0).getTextView().getTop());
                }
                winners.get(0).setStackSize(winners.get(0).getStackSize() + game.getPotSize());
            } else {
                winners.get(0).setStackSize(winners.get(0).getStackSize() + winners.get(0).getAmountToWin());
                game.getPotChips().get(0).animate().setDuration(500).x(winners.get(0).getTextView().getLeft()).y(winners.get(0).getTextView().getTop());
                game.getPotChips().remove(0);
                game.setPotSize(game.getPotSize() - winners.get(0).getAmountToWin());
                playersInRound.remove(winners.get(0));
                List<Player> remainingPotWinners = evaluateRound();
                assignChips(remainingPotWinners);
            }

        }
    }


    public void endRound() {

        for (Player player : players) {
            player.getTextView().setText(player.getStackSize().toString());
        }

        List<Player> playerList = new ArrayList<>();
        playerList.addAll(players);
        for (Player player : playerList) {
            Log.i(TAG, player.getOrder() + ": " + player.getAmountInPot());
            if (player.getStackSize() == 0) {
                activity.removeSeat(player.getTextView());
                players.remove(player);
                if (player.isUser()) {
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            context).create();

                    alertDialog.setTitle("Game Over!");
                    alertDialog.setMessage("You have finished " + playerList.size() + ". place!");


                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Back to Menu", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent menuActivity = new Intent(context.getApplicationContext(), MenuActivity.class);
                            menuActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(menuActivity);
//                            ((SinglePlayerActivity) context).finish();
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Start New", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent singlePlayerActivity = new Intent(context.getApplicationContext(), SinglePlayerSettingsActivity.class);
                            singlePlayerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(singlePlayerActivity);
                        }
                    });
                    alertDialog.show();
                    stopThread = true;

                }
            }
            board.removeView(player.getCard1View());
            board.removeView(player.getCard2View());
            player.setCard1View(null);
            player.setCard2View(null);
            player.setAmountInPot(0);
        }
        if (players.size() == 1 && players.get(0).isUser()) {
            AlertDialog alertDialog = new AlertDialog.Builder(
                    context).create();

            alertDialog.setTitle("Congratulations!");
            alertDialog.setMessage("You have finished 1. place!");


            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Back to Menu", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent menuActivity = new Intent(context.getApplicationContext(), MenuActivity.class);
                    menuActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(menuActivity);
//                    ((SinglePlayerActivity) context).finish();
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Start New", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent singlePlayerActivity = new Intent(context.getApplicationContext(), SinglePlayerSettingsActivity.class);
                    singlePlayerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(singlePlayerActivity);
                }
            });
            alertDialog.show();
            stopThread = true;

        }
        for (RelativeLayout chip : game.getPotChips()) {
            board.removeView(chip);
        }
        board.removeView(flop1);
        board.removeView(flop2);
        board.removeView(flop3);
        board.removeView(turn);
        board.removeView(river);


        flopDealt = false;
        turnDealt = false;
        riverDealt = false;

    }

    public void dealFlop() {

        endBettingRound();

        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);
        game.getBoard().add(deck.get(0));
        int res2Id = resources.getIdentifier(deck.get(1).toString(), "drawable", packageName);
        game.getBoard().add(deck.get(1));
        int res3Id = resources.getIdentifier(deck.get(2).toString(), "drawable", packageName);
        game.getBoard().add(deck.get(2));

        Animation flop1Anim = createAnimation(screenWidth / 2, screenWidth / 2 - screenWidth / 6, 0, screenHeight / 6, true);
        flop1 = new ImageView(context);
        board.addView(flop1);
        flop1.setAnimation(flop1Anim);
        flop1.setImageResource(resId);
        flop1.setVisibility(View.VISIBLE);
        flop1.startAnimation(flop1Anim);
        deck.remove(0);

        Animation flop2Anim = createAnimation(screenWidth / 2, screenWidth / 2 - screenWidth / 10, 0, screenHeight / 6, true);
        flop2 = new ImageView(context);
        board.addView(flop2);
        flop2.setAnimation(flop2Anim);
        flop2.setImageResource(res2Id);
        flop2.setVisibility(View.VISIBLE);
        flop2.startAnimation(flop2Anim);
        deck.remove(0);

        Animation flop3Anim = createAnimation(screenWidth / 2, screenWidth / 2 - screenWidth / 32, 0, screenHeight / 6, true);
        flop3 = new ImageView(context);
        board.addView(flop3);
        flop3.setAnimation(flop3Anim);
        flop3.setImageResource(res3Id);
        flop3.setVisibility(View.VISIBLE);
        flop3.startAnimation(flop3Anim);
        deck.remove(0);

    }

    public void dealTurn() {
        endBettingRound();

        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);
        game.getBoard().add(deck.get(0));

        Animation turnAnim = createAnimation(screenWidth / 2, screenWidth / 2 + screenWidth / 26, 0, screenHeight / 6, true);
        turn = new ImageView(context);
        board.addView(turn);
        turn.setAnimation(turnAnim);
        turn.setImageResource(resId);
        turn.setVisibility(View.VISIBLE);
        turn.startAnimation(turnAnim);
        deck.remove(0);

    }

    public void dealRiver() {
        endBettingRound();

        int resId = resources.getIdentifier(deck.get(0).toString(), "drawable", packageName);
        game.getBoard().add(deck.get(0));

        Animation riverAnim = createAnimation(screenWidth / 2, screenWidth / 2 + screenWidth / 9, 0, screenHeight / 6, true);
        river = new ImageView(context);
        board.addView(river);
        river.setAnimation(riverAnim);
        river.setImageResource(resId);
        turn.setVisibility(View.VISIBLE);
        river.startAnimation(riverAnim);
        deck.remove(0);

    }

    public void deal() {

        for (Player player : players) {

            final int resId = player.isUser() ? resources.getIdentifier(deck.get(deck.size() - 1).toString(), "drawable", packageName)
                    : resources.getIdentifier("back", "drawable", packageName);

            player.setCardOne(deck.get(deck.size() - 1));
            TextView textView = player.getTextView();
            Animation card1Anim = createAnimation(screenWidth / 2, textView.getLeft() + screenWidth / 20, 0, textView.getTop() - screenHeight / 20, true);
            ImageView card1 = new ImageView(context);
            board.addView(card1);
            card1.setAnimation(card1Anim);
            card1.setImageResource(resId);
            card1.startAnimation(card1Anim);
            deck.remove(deck.size() - 1);
            player.setCard1View(card1);

            final int res2Id = player.isUser() ? resources.getIdentifier(deck.get(deck.size() - 1).toString(), "drawable", packageName)
                    : resources.getIdentifier("back", "drawable", packageName);

            player.setCardTwo(deck.get(deck.size() - 1));
            Animation card2Anim = createAnimation(screenWidth / 2, textView.getLeft() + screenWidth / 13, 0, textView.getTop() - screenHeight / 20, true);
            ImageView card2 = new ImageView(context);
            board.addView(card2);
            card2.setAnimation(card2Anim);
            card2.setImageResource(res2Id);
            card2.startAnimation(card2Anim);
            deck.remove(deck.size() - 1);
            player.setCard2View(card2);

        }

    }

    public Animation createAnimation(int xFrom, int xTo, int yFrom, int yTo, boolean fillAfter) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, xFrom, Animation.ABSOLUTE, xTo, Animation.ABSOLUTE, yFrom, Animation.ABSOLUTE, yTo);

        translateAnimation.setRepeatMode(0);
        translateAnimation.setDuration(500);
        translateAnimation.setFillAfter(fillAfter);

        return translateAnimation;
    }

    public void makeMoves() {
        List<Player> playersWithStack = new ArrayList<>();
        for(Player player : playersInRound){
            if(player.getStackSize() > 0){
                playersWithStack.add(player);
            }
        }
        if (playersInRound.size() == 1 || playersWithStack.size() == 1) {
            roundOver = true;
            return;
        }
        List<Player> playerList = new ArrayList<>();
        playerList.addAll(playersInRound);
        for (final Player player : playerList) {
            if (playersInRound.size() == 1) {
                roundOver = true;
                return;

            }
            if ((null != player.getBetAmount() && null != highestBetAction && player.getBetAmount().equals(highestBetAction.getBetValue()))
                    || player.getStackSize() == 0) {
                continue;
            }
            final List<ActionType> availableActions = getAvailableActions(player);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    player.getTextView().setBackgroundResource(R.drawable.seatactive);
                }
            });
            if (!player.isUser()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                player.setEvaluatedHand(HandEvaluator.evaluateHand(game.getBoard(), player.getCardOne(), player.getCardTwo()));
                Action nextAction = getNextAction(player, availableActions);
                if (nextAction.getActionType().equals(ActionType.CHECK)) {
                    player.setActionType(ActionType.CHECK);
                }
                if (nextAction.getActionType().equals(ActionType.BET)) {
                    player.setActionType(ActionType.BET);
                    highestBetAction = nextAction;
                    minBet = nextAction.getBetValue() * 2;
                    activity.setHighestBetAction(nextAction);
                    moveBet(nextAction.getBetValue(), player);
                }
                if (nextAction.getActionType().equals(ActionType.FOLD)) {
                    player.setActionType(ActionType.FOLD);
                    moveFold(player);
                }
                if (nextAction.getActionType().equals(ActionType.CALL)) {
                    int amount = highestBetAction.getBetValue() > player.getStackSize()
                            ? player.getStackSize() : highestBetAction.getBetValue();
                    player.setActionType(ActionType.CALL);
                    moveBet(amount, player);
                }
                if (nextAction.getActionType().equals(ActionType.RAISE)) {
                    player.setActionType(ActionType.RAISE);
                    activity.setHighestBetAction(nextAction);
                    minBet = nextAction.getBetValue() * 2;
                    highestBetAction = nextAction;
                    moveBet(nextAction.getBetValue(), player);

                }
                if (nextAction.getActionType().equals(ActionType.ALL_IN)) {
                    player.setActionType(ActionType.ALL_IN);
                    activity.setHighestBetAction(nextAction);
                    highestBetAction = nextAction;
                    moveBet(null == player.getBetAmount() ? nextAction.getBetValue()
                            : nextAction.getBetValue() + player.getBetAmount(), player);

                }
            } else {
                isPlayerTurn = true;
                if (null != game.getBoard() && game.getBoard().size() > 0) {
                    EvaluatedHand handStrength = HandEvaluator.evaluateHand(game.getBoard(), player.getCardOne(), player.getCardTwo());
                    Log.i(TAG, handStrength.getHandStrength().name());
                    if (null != handStrength.getValue()) {
                        Log.i(TAG, handStrength.getValue().toString());
                    }
                    if (null != handStrength.getHighCards()) {
                        Log.i(TAG, handStrength.getHighCards().toString());
                    }
                }
                if(minBet > player.getStackSize()){
                    minBet = null == player.getBetAmount() ? player.getStackSize()
                            : player.getBetAmount() + player.getStackSize();
                }
                activity.getBetBar().setMax(null == player.getBetAmount() ? player.getStackSize() - minBet
                                                    : player.getBetAmount() + player.getStackSize() - minBet );
                activity.getBetBar().setProgress(0);

                showActionButtons(true);
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (availableActions.contains(ActionType.CALL)) {
                            btnCheck.setText("CALL");
                        } else {
                            btnCheck.setText("CHECK");
                        }
                        if (availableActions.contains(ActionType.RAISE)) {
                            btnBet.setText("RAISE");
                        } else {
                            btnBet.setText("BET");
                        }
                        if (availableActions.contains(ActionType.ALL_IN)) {
                            btnCheck.setText("ALL IN");
                            btnBet.setVisibility(View.INVISIBLE);
                            betBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });


                while (isPlayerTurn) {
                    try {
                        if (null != playerAction) {
                            if (playerAction.equals(ActionType.BET)) {
                                moveBet(playerBetAmount, player);
                                highestBetAction = new Action();
                                minBet = playerBetAmount * 2;
                                highestBetAction.setBetValue(playerBetAmount);
                                highestBetAction.setActionType(playerAction);
                            }
                            if (playerAction.equals(ActionType.CALL)) {
                                int amount = highestBetAction.getBetValue();
                                if (amount > player.getStackSize()) {
                                    amount = player.getStackSize();
                                }
                                moveBet(amount, player);
                            }
                            if (playerAction.equals(ActionType.RAISE)) {
                                moveBet(playerBetAmount, player);
                                minBet = playerBetAmount * 2;
                                highestBetAction = new Action();
                                highestBetAction.setBetValue(playerBetAmount);
                                highestBetAction.setActionType(playerAction);
                            }
                            if (playerAction.equals(ActionType.FOLD)) {
                                moveFold(player);
                            }
                            if (playerAction.equals(ActionType.ALL_IN)) {
                                moveBet(null == player.getBetAmount() ? player.getStackSize()
                                        : player.getStackSize() + player.getBetAmount(), player);
                            }
                            isPlayerTurn = false;
                            showActionButtons(false);
                            playerAction = null;
                        } else {
                            Thread.sleep(500);
                        }

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

    public void moveBet(int amount, final Player player) {
        int betAmount = amount;
        if (amount > player.getStackSize()) {
            betAmount = null == player.getBetAmount() ? player.getStackSize()
                    : player.getBetAmount() + player.getStackSize();
            amount = null == player.getBetAmount() ? player.getStackSize()
                    : player.getBetAmount() + player.getStackSize();
        }
        Log.i(TAG, "Player order: " + player.getOrder() + " Bet " + betAmount);

        if (null != player.getBetAmount()) {
            betAmount -= player.getBetAmount();
        }
        game.setPotSize(game.getPotSize() + betAmount);
        if (null == player.getAmountInPot()) {
            player.setAmountInPot(betAmount);
        } else {
            player.setAmountInPot(player.getAmountInPot() + betAmount);
        }
        player.setBetAmount(amount);
        Log.i(TAG, "Player order: " + player.getOrder() + " AmountInPot " + player.getAmountInPot());

        if (player.getStackSize() <= player.getBetAmount()) {
            player.setStackSize(0);
        } else {
            player.setStackSize(player.getStackSize() - betAmount);
        }


        activity.runOnUiThread(new Runnable() {
            public void run() {
                potsize.setText(game.getPotSize().toString());
                if (null != player.getChipLayout()) {
                    TextView existingChipsTextViw = (TextView) player.getChipLayout().getChildAt(0);
                    existingChipsTextViw.setText(player.getBetAmount() + "");
                } else {
                    RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.chips, null);
                    TextView chipsTextView = (TextView) relativeLayout.getChildAt(0);
                    chipsTextView.setText(player.getBetAmount().toString());


                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 12, screenHeight / 8);
                    Position position = getChipsPosition(player);
                    layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);
                    relativeLayout.setLayoutParams(layoutParams);

                    board.addView(relativeLayout);
                    player.setChipLayout(relativeLayout);
                }

                player.getTextView().setText(player.getStackSize().toString());
            }
        });

    }

    public void moveFold(final Player player) {
        Animation card1Anim = createAnimation(player.getTextView().getRight() - 150, screenWidth / 2, player.getTextView().getTop() - 40, 0, false);
        player.getCard1View().setAnimation(card1Anim);

        Animation card2Anim = createAnimation(player.getTextView().getRight() - 110, screenWidth / 2, player.getTextView().getTop() - 40, 0, false);
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

    public void collectChips() {

        for (Player player : players) {
            if (null != player.getChipLayout()) {
                int chipsShiftX = new Random().nextInt(100) - 50;
                int chipsShiftY = new Random().nextInt(20) - 10;
                TextView chipsText = (TextView) player.getChipLayout().getChildAt(0);
                chipsText.setText("");
                player.getChipLayout().animate().x(screenWidth / 2 + chipsShiftX).y(screenHeight / 16 + chipsShiftY);
                game.getPotChips().add(player.getChipLayout());
            }
            player.setChipLayout(null);
            player.setBetAmount(null);
        }
    }

    public void showActionButtons(final boolean show) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (show) {
                    btnCheck.setVisibility(View.VISIBLE);
                    btnBet.setVisibility(View.VISIBLE);
                    btnFold.setVisibility(View.VISIBLE);
                    betBar.setVisibility(View.VISIBLE);
                    betValue.setVisibility(View.VISIBLE);
                } else {
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
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        SystemClock.sleep(2000);
        createGame();

        try {
            while (players.size() > 1 && !stopThread) {
                shuffle();
                startRound();
                showCards();
                Thread.sleep(2000);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endRound();
                    }
                });
                Thread.sleep(2000);

            }


        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public Position getChipsPosition(Player player) {
        switch (player.getOrder()) {
            case 1:
                return new Position(player.getTextView().getLeft() - 70, player.getTextView().getTop() + 80);
            case 2:
                return new Position(player.getTextView().getLeft() - 70, player.getTextView().getTop() + 20);
            case 3:
                return new Position(player.getTextView().getLeft() - 50, player.getTextView().getTop() - 20);
            case 4:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 150);
            case 5:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 150);
            case 6:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 150);
            case 7:
                return new Position(player.getTextView().getLeft() + 220, player.getTextView().getTop() + 20);
            case 8:
                return new Position(player.getTextView().getLeft() + 220, player.getTextView().getTop() + 20);
            case 9:
                return new Position(player.getTextView().getLeft() + 100, player.getTextView().getTop() + 80);
        }
        return null;
    }

    public List<ActionType> getAvailableActions(Player player) {
        List<ActionType> availableActions = new ArrayList<>();
        if (null == highestBetAction || highestBetAction.getActionType().equals(ActionType.CHECK)) {
            availableActions.add(ActionType.CHECK);
            availableActions.add(ActionType.BET);
            availableActions.add(ActionType.FOLD);
        } else if (highestBetAction.getActionType().equals(ActionType.BET) || highestBetAction.getActionType().equals(ActionType.CALL)
                || highestBetAction.getActionType().equals(ActionType.RAISE) || highestBetAction.getActionType().equals(ActionType.ALL_IN)) {

            if (player.getStackSize() > highestBetAction.getBetValue()) {
                availableActions.add(ActionType.CALL);
                availableActions.add(ActionType.RAISE);
                availableActions.add(ActionType.FOLD);
            } else {
                availableActions.add(ActionType.ALL_IN);
                availableActions.add(ActionType.FOLD);
            }

        }


        return availableActions;
    }

    public Action getNextAction(Player player, List<ActionType> availableActions) {
        int handStrength = 0;
        handStrength += player.getCardOne().getValue() == 1 ? 14 : player.getCardOne().getValue();
        handStrength += player.getCardTwo().getValue() == 1 ? 14 : player.getCardTwo().getValue();
        if (player.getCardTwo().getValue().equals(player.getCardOne().getValue())) {
            handStrength += player.getCardOne().getValue() == 1 ? 14 : player.getCardOne().getValue();

        }
        if (player.getCardTwo().getSuit().equals(player.getCardOne().getSuit())) {
            handStrength += 5;
        }
        if (null != player.getEvaluatedHand()) {
            switch (player.getEvaluatedHand().getHandStrength()) {
                case ONE_PAIR:
                    handStrength += 5;
                    break;
                case TWO_PAIR:
                    handStrength += 10;
                    break;
                case THREE_OF_A_KIND:
                    handStrength += 15;
                    break;
                case STRAIGHT:
                    handStrength += 20;
                    break;
                case FLUSH:
                    handStrength += 25;
                    break;
                case FULL_HOUSE:
                    handStrength += 30;
                    break;
                case STRAIGHT_FLUSH:
                    handStrength += 35;
                    break;
                case FOUR_OF_A_KIND:
                    handStrength += 40;
                    break;
                case ROYAL_FLUSH:
                    handStrength += 45;
                    break;
            }
        }
        Action action = null;
        switch (player.getPlayStyle()) {
            case MANIAC:
                if (handStrength > 15) {
                    if (availableActions.contains(ActionType.BET)) {
                        Action maniacAction = new Action();
                        maniacAction.setActionType(ActionType.BET);
                        maniacAction.setBetValue(minBet * 2);
                        action = maniacAction;
                    } else if (availableActions.contains(ActionType.RAISE)) {
                        Action maniacAction = new Action();
                        maniacAction.setActionType(ActionType.RAISE);
                        maniacAction.setBetValue(highestBetAction.getBetValue() * 2);
                        action = maniacAction;
                    } else if (availableActions.contains(ActionType.ALL_IN)) {
                        Action maniacAction = new Action();
                        maniacAction.setActionType(ActionType.ALL_IN);
                        maniacAction.setBetValue(player.getStackSize());
                        action = maniacAction;
                    }

                } else {
                    Action maniacAction = new Action();
                    maniacAction.setActionType(ActionType.FOLD);
                    maniacAction.setBetValue(0);
                    action = maniacAction;
                }
                break;
            case CALLING_STATION:
                if (handStrength > 15) {
                    if (availableActions.contains(ActionType.CALL)) {
                        Action callingAction = new Action();
                        callingAction.setActionType(ActionType.CALL);
                        callingAction.setBetValue(highestBetAction.getBetValue());
                        action = callingAction;
                    }
                    if (availableActions.contains(ActionType.CHECK)) {
                        Action callingAction = new Action();
                        callingAction.setActionType(ActionType.CHECK);
                        callingAction.setBetValue(0);
                        action = callingAction;
                    }
                    if (availableActions.contains(ActionType.ALL_IN)) {
                        Action callingAction = new Action();
                        callingAction.setActionType(ActionType.ALL_IN);
                        callingAction.setBetValue(player.getStackSize());
                        action = callingAction;
                    }

                } else {
                    Action callingAction = new Action();
                    callingAction.setActionType(ActionType.FOLD);
                    callingAction.setBetValue(0);
                    action = callingAction;
                }
                break;

            case ROCK:
                if (handStrength > 35) {
                    if (availableActions.contains(ActionType.BET)) {
                        Action rockAction = new Action();
                        rockAction.setActionType(ActionType.BET);
                        rockAction.setBetValue(minBet * 2);
                        action = rockAction;
                    }
                    if (availableActions.contains(ActionType.RAISE)) {
                        Action rockAction = new Action();
                        rockAction.setActionType(ActionType.RAISE);
                        rockAction.setBetValue(highestBetAction.getBetValue() * 2);
                        action = rockAction;
                    }
                    if (availableActions.contains(ActionType.ALL_IN)) {
                        Action rockAction = new Action();
                        rockAction.setActionType(ActionType.ALL_IN);
                        rockAction.setBetValue(player.getStackSize());
                        action = rockAction;
                    }
                } else {
                    Action rockAction = new Action();
                    rockAction.setActionType(ActionType.FOLD);
                    rockAction.setBetValue(0);
                    action = rockAction;
                }
                break;

            case SHARK:

                if (availableActions.contains(ActionType.BET)) {
                    Action sharkAction = new Action();
                    if (handStrength < 15) {
                        sharkAction.setActionType(ActionType.FOLD);
                        sharkAction.setBetValue(0);
                    } else if (handStrength < 25) {
                        sharkAction.setActionType(ActionType.CHECK);
                        sharkAction.setBetValue(0);
                    } else {
                        sharkAction.setActionType(ActionType.BET);
                        sharkAction.setBetValue(minBet * 3);
                    }
                    action = sharkAction;
                } else if (availableActions.contains(ActionType.RAISE)) {
                    Action sharkAction = new Action();
                    if (handStrength < 25) {
                        sharkAction.setActionType(ActionType.FOLD);
                        sharkAction.setBetValue(0);
                    } else if (handStrength < 40) {
                        sharkAction.setActionType(ActionType.CALL);
                        sharkAction.setBetValue(highestBetAction.getBetValue());
                    } else {
                        sharkAction.setActionType(ActionType.RAISE);
                        sharkAction.setBetValue(highestBetAction.getBetValue() * 3);
                    }
                    action = sharkAction;
                } else if (availableActions.contains(ActionType.ALL_IN)) {
                    Action sharkAction = new Action();
                    if (handStrength < 45) {
                        sharkAction.setActionType(ActionType.FOLD);
                        sharkAction.setBetValue(0);
                    } else {
                        sharkAction.setActionType(ActionType.ALL_IN);
                        sharkAction.setBetValue(player.getStackSize());
                    }
                    action = sharkAction;
                } else {
                    Action sharkAction = new Action();
                    sharkAction.setActionType(ActionType.FOLD);
                    sharkAction.setBetValue(0);
                    action = sharkAction;
                }
                break;

        }
        return action;

    }

    public boolean makeMovesAgain() {
        for (Player player : playersInRound) {
            if ((null != player.getBetAmount() && null != highestBetAction && player.getBetAmount() < highestBetAction.getBetValue()
                    && player.getStackSize() > 0) || (null == player.getBetAmount() && null != highestBetAction && player.getStackSize() > 0)) {
                return true;
            }
        }
        return false;
    }

    public void endBettingRound() {
        collectChips();
        highestBetAction = null;
//        previousAction = null;
        for (Player player : playersInRound) {
            player.setActionType(null);
            player.setBetAmount(null);
        }
    }

    public boolean isUserStillInGame() {
        for (Player player : players) {
            if (player.isUser()) {
                return true;
            }
        }
        return false;
    }

    public static List<List<RelativeLayout>> splitChips(List<RelativeLayout> list, int numberOfLists) {

        List<List<RelativeLayout>> subLists = new ArrayList<>();

        for (int i = 0; i < numberOfLists; i++) {
            subLists.add(new ArrayList<RelativeLayout>());
        }

        int index = 0;

        for (RelativeLayout layout : list) {
            subLists.get(index).add(layout);
            index = (index + 1) % numberOfLists;
        }
        return subLists;
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

    public int getMinBet() {
        return minBet;
    }

    public void setMinBet(int minBet) {
        this.minBet = minBet;
    }
}
