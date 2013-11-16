package hu.onlineholdem.restclient.thread;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import hu.onlineholdem.restclient.activity.SinglePlayerActivity;
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
    private ActionType previousAction;
    private int playerBetAmount;
    private int previousBetAmount;
    private boolean roundOver = false;
    private boolean flopDealt = false;
    private boolean turnDealt = false;
    private boolean riverDealt = false;
    private Player winner;

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
            } else if (!turnDealt) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        dealTurn();
                    }
                });
                turnDealt = true;
            } else if (!riverDealt) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        dealRiver();
                    }
                });
                riverDealt = true;
                roundOver = true;
            }
            Thread.sleep(2000);
            makeMoves();
            while (makeMovesAgain()) {
                makeMoves();
            }
            if (roundOver) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        endBettingRound();
                    }
                });

            }
        }
    }

    public void evaluateRound() {

        for (final Player player : playersInRound) {
            final EvaluatedHand evaluatedHand = HandEvaluator.evaluateHand(game.getBoard(), player.getCardOne(), player.getCardTwo());
            player.setEvaluatedHand(evaluatedHand);
        }

        EvaluatedHand bestHand = playersInRound.get(0).getEvaluatedHand();
        Player winner = playersInRound.get(0);
        for (Player player : playersInRound) {
            if (player.equals(winner)) {
                continue;
            }
            if (player.getEvaluatedHand().getHandStrength().getStrength() > bestHand.getHandStrength().getStrength()) {
                bestHand = player.getEvaluatedHand();
                winner = player;
            }
            if (player.getEvaluatedHand().getHandStrength().getStrength().equals(bestHand.getHandStrength().getStrength())) {
                if (null != player.getEvaluatedHand().getHighCards() && null != winner.getEvaluatedHand().getHighCards()) {
                    Collections.reverse(player.getEvaluatedHand().getHighCards());
                    Collections.reverse(winner.getEvaluatedHand().getHighCards());
                    for (int i = 0; i < player.getEvaluatedHand().getHighCards().size(); i++) {
                        Card playerHighCard = player.getEvaluatedHand().getHighCards().get(i);
                        Card winnerHighCard = winner.getEvaluatedHand().getHighCards().get(i);
                        if (playerHighCard.getValue() > winnerHighCard.getValue()) {
                            bestHand = player.getEvaluatedHand();
                            winner = player;
                            break;
                        }
                        if (playerHighCard.getValue() < winnerHighCard.getValue()) {
                            break;
                        }
                    }
                }
            }
        }
        this.winner = winner;

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
        handler.post(new Runnable() {
            @Override
            public void run() {
                board.invalidate();
                for (final RelativeLayout chips : game.getPotChips()) {
                    chips.animate().setDuration(500).x(winner.getTextView().getLeft()).y(winner.getTextView().getTop());
                }
            }
        });

    }

    public void endRound() {

        winner.setStackSize(winner.getStackSize() + game.getPotSize());

        winner.getTextView().setText(winner.getStackSize().toString());
        List<Player> playerList = new ArrayList<>();
        playerList.addAll(players);
        for (Player player : playerList) {
            if (player.getStackSize() == 0) {
                activity.removeSeat(player.getTextView());
                players.remove(player);
            }
            board.removeView(player.getCard1View());
            board.removeView(player.getCard2View());
            player.setCard1View(null);
            player.setCard2View(null);
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

        Animation flop1Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 300, 0, screenHeight / 4, true);
        flop1 = new ImageView(context);
        board.addView(flop1);
        flop1.setAnimation(flop1Anim);
        flop1.setImageResource(resId);
        flop1.setVisibility(View.VISIBLE);
        flop1.startAnimation(flop1Anim);
        deck.remove(0);

        Animation flop2Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 200, 0, screenHeight / 4, true);
        flop2 = new ImageView(context);
        board.addView(flop2);
        flop2.setAnimation(flop2Anim);
        flop2.setImageResource(res2Id);
        flop2.setVisibility(View.VISIBLE);
        flop2.startAnimation(flop2Anim);
        deck.remove(0);

        Animation flop3Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 100, 0, screenHeight / 4, true);
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

        Animation turnAnim = createAnimation(screenWidth / 2, screenWidth / 2, 0, screenHeight / 4, true);
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

        Animation riverAnim = createAnimation(screenWidth / 2, screenWidth / 2 + 100, 0, screenHeight / 4, true);
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
            Animation card1Anim = createAnimation(screenWidth / 2, textView.getRight() - 150, 0, textView.getTop() - 40, true);
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
            Animation card2Anim = createAnimation(screenWidth / 2, textView.getRight() - 110, 0, textView.getTop() - 40, true);
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
        if (playersInRound.size() == 1) {
            roundOver = true;
            return;
        }
        List<Player> playerList = new ArrayList<>();
        playerList.addAll(playersInRound);
        for (final Player player : playerList) {
            if ((null != player.getBetAmount() && player.getBetAmount() == previousBetAmount)
                    || player.getStackSize() == 0) {
                continue;
            }
            final List<ActionType> availableActions = getAvailableActions();
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
                Random random = new Random();
                int action = random.nextInt(availableActions.size());
                ActionType randomAction = availableActions.get(action);
                if (randomAction.equals(ActionType.CHECK)) {
                    previousAction = ActionType.CHECK;
                    player.setActionType(ActionType.CHECK);
                }
                if (randomAction.equals(ActionType.BET)) {
                    int amount = 50;
                    player.setActionType(ActionType.BET);
                    previousBetAmount = amount;
                    activity.setPreviousBetAmount(previousBetAmount);
                    previousAction = ActionType.BET;
                    moveBet(amount, player);
                }
                if (randomAction.equals(ActionType.FOLD)) {
                    player.setActionType(ActionType.FOLD);
                    moveFold(player);
                }
                if (randomAction.equals(ActionType.CALL)) {
                    int amount = previousBetAmount;
                    previousAction = ActionType.CALL;
                    player.setActionType(ActionType.CALL);
                    moveBet(amount, player);
                }
                if (randomAction.equals(ActionType.RAISE)) {
                    previousAction = ActionType.RAISE;
                    player.setActionType(ActionType.RAISE);
                    if (previousBetAmount * 2 > 1500) {
                        player.setBetAmount(1500);
                        previousBetAmount = 1500;
                        moveBet(1500, player);
                    } else {
                        player.setBetAmount(previousBetAmount * 2);
                        moveBet(previousBetAmount * 2, player);
                        previousBetAmount = previousBetAmount * 2;
                    }
                    activity.setPreviousBetAmount(previousBetAmount);

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
                activity.getBetBar().setMax(player.getStackSize());
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
                        if (previousBetAmount > player.getStackSize()) {
                            btnCheck.setText("ALL IN");
                            btnBet.setVisibility(View.INVISIBLE);
                        }
                    }
                });


                while (isPlayerTurn) {
                    try {
                        if (null != playerAction) {
                            if (playerAction.equals(ActionType.BET)) {
                                moveBet(playerBetAmount, player);
                                previousAction = ActionType.BET;
                                previousBetAmount = playerBetAmount;
                            }
                            if (playerAction.equals(ActionType.CALL)) {
                                int amount = previousBetAmount;
                                if (amount > player.getStackSize()) {
                                    amount = player.getStackSize();
                                }
                                moveBet(amount, player);
                                previousAction = ActionType.CALL;
                            }
                            if (playerAction.equals(ActionType.RAISE)) {
                                moveBet(playerBetAmount, player);
                                previousAction = ActionType.RAISE;
                                previousBetAmount = playerBetAmount;
                            }
                            if (playerAction.equals(ActionType.FOLD)) {
                                moveFold(player);
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

    public void moveBet(final int amount, final Player player) {
        game.setPotSize(game.getPotSize() + amount);
        int betAmount = amount;
        if (amount > player.getStackSize()) {
            betAmount = player.getStackSize();
            if(null != player.getBetAmount()){
                betAmount = player.getBetAmount() + amount;
            }

        }
        player.setBetAmount(betAmount);
        if (null != player.getChipLayout()) {
            TextView existingChipsTextViw = (TextView) player.getChipLayout().getChildAt(0);
            int prevBet = Integer.parseInt(existingChipsTextViw.getText().toString());
            if(player.getStackSize() <= player.getBetAmount()){
                player.setStackSize(0);
            } else {
                player.setStackSize(player.getStackSize() - (amount - prevBet));
            }
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
                    RelativeLayout relativeLayout = new RelativeLayout(context);
                    TextView chipsTextView = new TextView(context);
                    chipsTextView.setTextSize(15);
                    chipsTextView.setText(player.getBetAmount().toString());
                    relativeLayout.addView(chipsTextView);
                    ImageView chipsImageView = new ImageView(context);
                    chipsImageView.setImageResource(R.drawable.chips);
                    relativeLayout.addView(chipsImageView);


                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(100, 40);
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

        for (Player player : playersInRound) {
            if (null != player.getChipLayout()) {
                int chipsShiftX = new Random().nextInt(100) - 50;
                int chipsShiftY = new Random().nextInt(20) - 10;
                TextView chipsText = (TextView) player.getChipLayout().getChildAt(0);
                chipsText.setText("");
                player.getChipLayout().animate().x(screenWidth / 2 + chipsShiftX).y(screenHeight / 7 + chipsShiftY);
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

        createGame();

        try {
            while (players.size() > 1) {
                shuffle();
                startRound();
                Thread.sleep(1000);
                evaluateRound();
                showCards();
                Thread.sleep(4000);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endRound();
                    }
                });

            }


        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public Position getChipsPosition(Player player) {
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

    public List<ActionType> getAvailableActions() {
        List<ActionType> availableActions = new ArrayList<>();
        if (null == previousAction || previousAction.equals(ActionType.CHECK)) {
//            availableActions.add(ActionType.CHECK);
            availableActions.add(ActionType.BET);
//            availableActions.add(ActionType.FOLD);
        }
        if (null != previousAction) {
            if (previousAction.equals(ActionType.BET) || previousAction.equals(ActionType.CALL)
                    || previousAction.equals(ActionType.RAISE) || previousAction.equals(ActionType.FOLD)) {
                availableActions.add(ActionType.CALL);
//                availableActions.add(ActionType.RAISE);
//                availableActions.add(ActionType.FOLD);
            }
        }

        return availableActions;
    }

    public boolean makeMovesAgain() {
        for (Player player : playersInRound) {
            if ((null != player.getBetAmount() && player.getBetAmount() != previousBetAmount && player.getStackSize() > 0)
                    || (null == player.getBetAmount() && previousBetAmount != 0 && player.getStackSize() > 0)) {
                return true;
            }
        }
        return false;
    }

    public void endBettingRound() {
        collectChips();
        previousBetAmount = 0;
        previousAction = null;
        for (Player player : playersInRound) {
            player.setActionType(null);
            player.setBetAmount(null);
        }
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
