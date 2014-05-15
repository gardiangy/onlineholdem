package hu.onlineholdem.restclient.thread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import hu.onlineholdem.restclient.util.GraphicStuff;
import hu.onlineholdem.restclient.util.HandEvaluator;
import hu.onlineholdem.restclient.util.PlayerComperator;

public class GameThread extends Thread {

    private static final String TAG = "GameThread";

    private GraphicStuff graphs;
    private List<Card> deck = new ArrayList<>();
    private Button btnCheck;
    private Button btnBet;
    private Button btnFold;
    private SeekBar betBar;
    private TextView potsize;
    private TextView betValue;
    private TextView blindsText;
    private List<Player> players = new ArrayList<>();
    private List<Player> playersInRound;
    private Context context;
    private SinglePlayerActivity activity;
    private Game game;
    private boolean isPlayerTurn;
    private ActionType playerAction;
    private int playerBetAmount;
    private Action highestBetAction;
    private int minBet;
    private boolean roundOver = false;
    private boolean flopDealt = false;
    private boolean turnDealt = false;
    private boolean riverDealt = false;
    private boolean splitPot = false;
    private boolean stopThread = false;
    private int smallBlind;
    private int bigBlind;
    private long timePassedSinceBlindRaise = System.currentTimeMillis();

    public GameThread(List<Player> players, TextView potsize, Button btnCheck, Button btnBet, Button btnFold,
                      SeekBar betBar, TextView betValue, Context context, GraphicStuff graphs) {
        this.players = players;
        this.context = context;
        this.potsize = potsize;
        this.btnCheck = btnCheck;
        this.btnBet = btnBet;
        this.btnFold = btnFold;
        this.betBar = betBar;
        this.betValue = betValue;
        this.graphs = graphs;
        activity = (SinglePlayerActivity) context;
        blindsText = (TextView) activity.findViewById(R.id.blindsText);
    }

    public void createGame() {
        game = new Game();
        game.setPlayers(players);
        game.setPotChips(new ArrayList<RelativeLayout>());
        game.setDealer(players.get(0));
        setBlindPlayers(players, game.getDealer());
        timePassedSinceBlindRaise = System.currentTimeMillis();
        smallBlind = 10;
        bigBlind = 20;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphs.addDealer(game.getDealer());
            }
        });

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
        minBet = bigBlind;
    }

    public void startRound() throws InterruptedException {
        roundOver = false;
        deal();
        moveBet(smallBlind, game.getSmallBlind());
        moveBet(bigBlind, game.getBigBlind());
        Action blindAction = new Action();
        blindAction.setActionType(ActionType.BET);
        blindAction.setBetValue(bigBlind);
        blindAction.setPlayer(game.getBigBlind());
        activity.setHighestBetAction(blindAction);
        highestBetAction = blindAction;
        setOrder(playersInRound, game.getSmallBlind(), game.getBigBlind(), true);
        Collections.sort(playersInRound, new PlayerComperator());
        for (Player pl : playersInRound) {
            Log.i(TAG, "Player id: " + pl.getPlayerId() + " order: " + pl.getOrder());
        }
        Thread.sleep(1000);
        makeMoves();
        while (makeMovesAgain() && !stopThread) {
            makeMoves();
        }
        while (!roundOver && playersInRound.size() > 1) {
            Thread.sleep(2000);
            if (!flopDealt) {
                dealFlop();
                flopDealt = true;
                minBet = bigBlind;
            } else if (!turnDealt) {
                dealTurn();
                turnDealt = true;
                minBet = bigBlind;
            } else if (!riverDealt) {
                dealRiver();
                riverDealt = true;
                roundOver = true;
                minBet = bigBlind;
            }
            setOrder(playersInRound, game.getSmallBlind(), game.getBigBlind(), false);
            Collections.sort(playersInRound, new PlayerComperator());
            Thread.sleep(2000);
            makeMoves();
            while (makeMovesAgain() && !stopThread) {
                makeMoves();
            }
        }
        if (roundOver || playersInRound.size() == 1) {
            endBettingRound();
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
            if (player.equals(winner)) {
                continue;
            }
            if (HandEvaluator.isBetterHand(player.getEvaluatedHand(), bestHand)) {
                bestHand = player.getEvaluatedHand();
                winner = player;
                splitPot = false;
            }
        }
        for (Player player : playersInRound) {
            if (player.equals(winner)) {
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
            Log.i(TAG, "Winner : pl.id " + w.getPlayerId());
            Log.i(TAG, "Winner : card " + w.getEvaluatedHand().getHandStrength());
            Log.i(TAG, w.getEvaluatedHand().getValue().toString());
            if (null != w.getEvaluatedHand().getHighCards()) {
                Log.i(TAG, w.getEvaluatedHand().getHighCards().toString());
            }

        }

        return winners;

    }

    public void showCards() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphs.showCards(playersInRound);
            }
        });
        final List<Player> winners = evaluateRound();
        for (Player playerOne : players) {
            int amountToWin = 0;
            for (Player playerTwo : players) {
                amountToWin += playerTwo.getAmountInPot() >= playerOne.getAmountInPot()
                        ? playerOne.getAmountInPot() : playerTwo.getAmountInPot();
            }
            playerOne.setAmountToWin(amountToWin);
            Log.i(TAG, "Player id: " + playerOne.getPlayerId() + " AmountToWin " + amountToWin);

        }
        assignChips(winners);

    }

    public void assignChips(final List<Player> winners) {
        if (splitPot) {
            List<List<RelativeLayout>> chipsList = graphs.splitChips(game.getPotChips(), winners.size());

            int splitPotAmount = game.getPotSize() / winners.size();

            List<Player> winnerList = new ArrayList<>();
            winnerList.addAll(winners);

            for (final Player winner : winnerList) {
                if (winner.getAmountToWin() <= splitPotAmount) {
                    Log.i(TAG, "PotSize " + game.getPotSize());
                    Log.i(TAG, "winner amount to win " + winner.getAmountToWin());
                    game.setPotSize(game.getPotSize() - winner.getAmountToWin());
                    for (final RelativeLayout chips : chipsList.get(0)) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                graphs.assignChips(chips,winner);
                            }
                        });
                    }
                    chipsList.remove(chipsList.get(0));
                    winner.setStackSize(winner.getStackSize() + winner.getAmountToWin());
                    winners.remove(winner);
                }
            }
            for (final Player winner : winners) {
                List<List<RelativeLayout>> layoutList = new ArrayList<>();
                layoutList.addAll(chipsList);
                Log.i(TAG, "PotSize " + game.getPotSize());
                Log.i(TAG, "winner amount to win " + winner.getAmountToWin());
                for (List<RelativeLayout> relativeLayouts : layoutList) {
                    for (final RelativeLayout chips : relativeLayouts) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                graphs.assignChips(chips,winner);
                            }
                        });
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
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            graphs.assignChips(chips,winners.get(0));
                        }
                    });
                }
                winners.get(0).setStackSize(winners.get(0).getStackSize() + game.getPotSize());
            } else {
                winners.get(0).setStackSize(winners.get(0).getStackSize() + winners.get(0).getAmountToWin());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        graphs.assignChips(game.getPotChips().get(0),winners.get(0));
                    }
                });
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
            graphs.removePlayerCards(player);
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
            graphs.removeChips(chip);
        }
        graphs.removeBoard();

        flopDealt = false;
        turnDealt = false;
        riverDealt = false;

        int dealerIndex = players.indexOf(game.getDealer());
        game.setDealer(dealerIndex == players.size() - 1 ? players.get(0) : players.get(dealerIndex + 1));
        setBlindPlayers(players, game.getDealer());
        graphs.moveDealer(game.getDealer());
    }

    public void dealFlop() {

        endBettingRound();

        game.getBoard().add(deck.get(0));
        deck.remove(0);
        game.getBoard().add(deck.get(0));
        deck.remove(0);
        game.getBoard().add(deck.get(0));
        deck.remove(0);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphs.dealFlop(game.getBoard().get(0),game.getBoard().get(1),game.getBoard().get(2));
            }
        });

    }

    public void dealTurn() {
        endBettingRound();

        game.getBoard().add(deck.get(0));
        deck.remove(0);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphs.dealTurn(game.getBoard().get(3));
            }
        });
    }

    public void dealRiver() {
        endBettingRound();

        game.getBoard().add(deck.get(0));
        deck.remove(0);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphs.dealRiver(game.getBoard().get(4));
            }
        });
    }

    public void deal() {

        for (final Player player : players) {

            player.setCardOne(deck.get(deck.size() - 1));
            deck.remove(deck.size() - 1);
            player.setCardTwo(deck.get(deck.size() - 1));
            deck.remove(deck.size() - 1);

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    graphs.deal(player);
                }
            });
        }
    }

    public void makeMoves() {
        List<Player> playerList = new ArrayList<>();
        playerList.addAll(playersInRound);
        for (final Player player : playerList) {
            if (playersInRound.size() == 1) {
                roundOver = true;
                return;

            }
            List<Player> playersWithNoStack = new ArrayList<>();
            for (Player pl : playersInRound) {
                if (pl.getStackSize() == 0 && null == pl.getBetAmount()) {
                    playersWithNoStack.add(pl);
                }
            }
            if (playersWithNoStack.size() == playersInRound.size() - 1) {
                return;

            }

            if ((null != player.getBetAmount() && null != highestBetAction && player.getBetAmount().equals(highestBetAction.getBetValue())
                    && !game.getBigBlind().equals(player)) || player.getStackSize() == 0) {
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
                    Log.i(TAG, "Player id: " + player.getPlayerId() + " CHECK");
                }
                if (nextAction.getActionType().equals(ActionType.BET)) {
                    player.setActionType(ActionType.BET);
                    highestBetAction = nextAction;
                    minBet = nextAction.getBetValue() * 2;
                    activity.setHighestBetAction(nextAction);
                    Log.i(TAG, "Player id: " + player.getPlayerId() + " BET amount: " + nextAction.getBetValue());
                    moveBet(nextAction.getBetValue(), player);
                }
                if (nextAction.getActionType().equals(ActionType.FOLD)) {
                    player.setActionType(ActionType.FOLD);
                    Log.i(TAG, "Player id: " + player.getPlayerId() + " FOLD");
                    moveFold(player);
                }
                if (nextAction.getActionType().equals(ActionType.CALL)) {
                    int amount = highestBetAction.getBetValue() > player.getStackSize()
                            ? player.getStackSize() : highestBetAction.getBetValue();
                    player.setActionType(ActionType.CALL);
                    Log.i(TAG, "Player id: " + player.getPlayerId() + " CALL amount: " + amount);
                    moveBet(amount, player);
                }
                if (nextAction.getActionType().equals(ActionType.RAISE)) {
                    player.setActionType(ActionType.RAISE);
                    activity.setHighestBetAction(nextAction);
                    minBet = nextAction.getBetValue() * 2;
                    highestBetAction = nextAction;
                    Log.i(TAG, "Player id: " + player.getPlayerId() + " RAISE amount: " + nextAction.getBetValue());
                    moveBet(nextAction.getBetValue(), player);

                }
                if (nextAction.getActionType().equals(ActionType.ALL_IN)) {
                    player.setActionType(ActionType.ALL_IN);
                    activity.setHighestBetAction(nextAction);
                    highestBetAction = nextAction;
                    int amount = null == player.getBetAmount() ? nextAction.getBetValue()
                            : nextAction.getBetValue() + player.getBetAmount();
                    Log.i(TAG, "Player id: " + player.getPlayerId() + " ALL IN amount: " + amount);
                    moveBet(amount, player);

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
                if (minBet > player.getStackSize()) {
                    minBet = null == player.getBetAmount() ? player.getStackSize()
                            : player.getBetAmount() + player.getStackSize();
                }
                activity.getBetBar().setMax(null == player.getBetAmount() ? player.getStackSize() - minBet
                        : player.getBetAmount() + player.getStackSize() - minBet);
                activity.getBetBar().setProgress(0);

                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        graphs.showActionButtons(true);
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


                while (isPlayerTurn && !stopThread) {
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
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    graphs.showActionButtons(false);
                                }
                            });
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
        Log.i(TAG, "Player id: " + player.getPlayerId() + " Bet " + betAmount);

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
        Log.i(TAG, "Player id: " + player.getPlayerId() + " AmountInPot " + player.getAmountInPot());

        if (player.getStackSize() <= player.getBetAmount()) {
            player.setStackSize(0);
        } else {
            player.setStackSize(player.getStackSize() - betAmount);
        }


        activity.runOnUiThread(new Runnable() {
            public void run() {
                potsize.setText(game.getPotSize().toString());
                graphs.moveBet(player);
            }
        });

    }

    public void moveFold(final Player player) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                graphs.moveFold(player);
            }
        });
        playersInRound.remove(player);
    }

    public void collectChips() {

        for (final Player player : players) {
            if(null != player.getChipLayout()){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        graphs.collectChips(player);
                    }
                });
                game.getPotChips().add(player.getChipLayout());
            }
            SystemClock.sleep(100);
            player.setChipLayout(null);
            player.setBetAmount(null);
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        SystemClock.sleep(2000);
        Thread blindRaiser = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopThread) {
                    SystemClock.sleep(1000);
                    long actualTime = System.currentTimeMillis();
                    if (actualTime - timePassedSinceBlindRaise >= 300000) {
                        timePassedSinceBlindRaise = actualTime;
                        bigBlind *= 2;
                        smallBlind *= 2;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                blindsText.setText("Blinds: " + smallBlind + " / " + bigBlind);
                            }
                        });
                        Log.i(TAG, "Raise blind small: " + smallBlind + " big: " + bigBlind);
                    }
                }
            }
        });
        blindRaiser.start();
        createGame();
        try {
            while (players.size() > 1 && !stopThread) {
                shuffle();
                startRound();
                if (!stopThread) {
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
            }
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
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
                List<Player> playersWithStack = new ArrayList<>();
                for (Player pl : playersInRound) {
                    if (!pl.equals(player) && pl.getStackSize() > 0) {
                        playersWithStack.add(pl);
                    }
                }
                if (playersWithStack.size() > 0) {
                    availableActions.add(ActionType.RAISE);
                }
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
        int amountCanBeRaisedTo = null == player.getBetAmount() ? player.getStackSize() :
                player.getBetAmount() + player.getStackSize();
        switch (player.getPlayStyle()) {
            case MANIAC:
                if (handStrength > 15) {
                    if (availableActions.contains(ActionType.BET)) {
                        Action maniacAction = new Action();
                        maniacAction.setActionType(ActionType.BET);
                        maniacAction.setBetValue(minBet * 2 > amountCanBeRaisedTo
                                ? amountCanBeRaisedTo : minBet * 2);
                        action = maniacAction;
                    } else if (availableActions.contains(ActionType.RAISE)) {
                        Action maniacAction = new Action();
                        maniacAction.setActionType(ActionType.RAISE);
                        maniacAction.setBetValue(highestBetAction.getBetValue() * 2 > amountCanBeRaisedTo
                                ? amountCanBeRaisedTo : highestBetAction.getBetValue() * 2);
                        action = maniacAction;
                    } else if (availableActions.contains(ActionType.ALL_IN)) {
                        Action maniacAction = new Action();
                        maniacAction.setActionType(ActionType.ALL_IN);
                        maniacAction.setBetValue(player.getStackSize());
                        action = maniacAction;
                    } else if (availableActions.contains(ActionType.CALL)) {
                        Action maniacAction = new Action();
                        maniacAction.setActionType(ActionType.CALL);
                        maniacAction.setBetValue(highestBetAction.getBetValue());
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
                    } else if (availableActions.contains(ActionType.CHECK)) {
                        Action callingAction = new Action();
                        callingAction.setActionType(ActionType.CHECK);
                        callingAction.setBetValue(0);
                        action = callingAction;
                    } else if (availableActions.contains(ActionType.ALL_IN)) {
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
                        rockAction.setBetValue(minBet * 2 > amountCanBeRaisedTo
                                ? amountCanBeRaisedTo : minBet * 2);
                        action = rockAction;
                    } else if (availableActions.contains(ActionType.RAISE)) {
                        Action rockAction = new Action();
                        rockAction.setActionType(ActionType.RAISE);
                        rockAction.setBetValue(highestBetAction.getBetValue() * 2 > amountCanBeRaisedTo
                                ? amountCanBeRaisedTo : highestBetAction.getBetValue() * 2);
                        action = rockAction;
                    } else if (availableActions.contains(ActionType.ALL_IN)) {
                        Action rockAction = new Action();
                        rockAction.setActionType(ActionType.ALL_IN);
                        rockAction.setBetValue(player.getStackSize());
                        action = rockAction;
                    } else if (availableActions.contains(ActionType.CALL)) {
                        Action rockAction = new Action();
                        rockAction.setActionType(ActionType.CALL);
                        rockAction.setBetValue(highestBetAction.getBetValue());
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
                        sharkAction.setBetValue(minBet * 3 > amountCanBeRaisedTo
                                ? amountCanBeRaisedTo : minBet * 3);
                    }
                    action = sharkAction;
                } else if (availableActions.contains(ActionType.RAISE)
                        || availableActions.contains(ActionType.CALL)) {
                    Action sharkAction = new Action();
                    if (handStrength < 25) {
                        sharkAction.setActionType(ActionType.FOLD);
                        sharkAction.setBetValue(0);
                    } else if (handStrength < 40) {
                        sharkAction.setActionType(ActionType.CALL);
                        sharkAction.setBetValue(highestBetAction.getBetValue());
                    } else {
                        sharkAction.setActionType(ActionType.RAISE);
                        sharkAction.setBetValue(highestBetAction.getBetValue() * 3 > amountCanBeRaisedTo
                                ? amountCanBeRaisedTo : highestBetAction.getBetValue() * 3);
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
        for (Player player : playersInRound) {
            player.setActionType(null);
            player.setBetAmount(null);
        }
    }

    public void setBlindPlayers(List<Player> players, Player dealer) {
        int dealerIndex = players.indexOf(dealer);
        Player smallBlind = dealerIndex == players.size() - 1 ? players.get(0) : players.get(dealerIndex + 1);
        int smallBlindIndex = players.indexOf(smallBlind);
        Player bigBlind = smallBlindIndex == players.size() - 1 ? players.get(0) : players.get(smallBlindIndex + 1);
        game.setBigBlind(bigBlind);
        game.setSmallBlind(smallBlind);
        Log.i(TAG, "Dealer: " + dealer.getPlayerId());
        Log.i(TAG, "SmallBlind: " + smallBlind.getPlayerId());
        Log.i(TAG, "BigBlind: " + bigBlind.getPlayerId());
    }

    public void setOrder(List<Player> players, Player smallBlind, Player bigBlind, boolean preFlop) {
        int order = 1;

        int bigBlindIndex = players.indexOf(bigBlind);
        int firstPlayerIndex;
        if (preFlop) {
            firstPlayerIndex = players.size() - 1 > bigBlindIndex ? bigBlindIndex + 1 : 0;
        } else {
            firstPlayerIndex = players.indexOf(smallBlind);
            if (firstPlayerIndex == -1) {
                firstPlayerIndex = players.indexOf(bigBlind) == -1 ? 0 : players.indexOf(bigBlind);
            }
        }
        for (Player player : players) {

            if (players.indexOf(player) == firstPlayerIndex) {
                order = 1;
                player.setOrder(order);
                order++;
            }

            if (players.indexOf(player) > firstPlayerIndex) {
                player.setOrder(order);
                order++;
            }
        }
        for (Player player : players) {
            if (players.indexOf(player) < firstPlayerIndex) {
                player.setOrder(order);
                order++;
            }

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

    public int getMinBet() {
        return minBet;
    }

    public void setMinBet(int minBet) {
        this.minBet = minBet;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public boolean isStopThread() {
        return stopThread;
    }

    public void setStopThread(boolean stopThread) {
        this.stopThread = stopThread;
    }

    public boolean isRoundOver() {
        return roundOver;
    }

    public void setRoundOver(boolean roundOver) {
        this.roundOver = roundOver;
    }
}
