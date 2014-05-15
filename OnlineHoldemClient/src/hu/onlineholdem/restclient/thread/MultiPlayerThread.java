package hu.onlineholdem.restclient.thread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import hu.onlineholdem.restclient.activity.GameBrowserActivity;
import hu.onlineholdem.restclient.activity.MultiPlayerActivity;
import hu.onlineholdem.restclient.entity.Action;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;
import hu.onlineholdem.restclient.enums.GameState;
import hu.onlineholdem.restclient.enums.Suit;
import hu.onlineholdem.restclient.response.Response;
import hu.onlineholdem.restclient.task.RefreshTask;
import hu.onlineholdem.restclient.util.GraphicStuff;
import hu.onlineholdem.restclient.util.PlayerComperator;

public class MultiPlayerThread extends Thread{

    private static final String SERVICE_URL = "http://192.168.1.101:8080/rest";
    private static final String TAG = "MultiplayerThread";

    private boolean init = false;
    private boolean flopDealt = false;
    private boolean turnDealt = false;
    private boolean riverDealt = false;
    private boolean handDealt = false;
    private boolean cardsShown = false;

    private long userId;
    private long playerId;

    private long gameId;
    private int actionSize = 0;

    private GraphicStuff graphics;
    private Game game;
    private Context context;
    private Button btnCheck;
    private Button btnBet;
    private Button btnFold;
    private SeekBar betBar;
    private TextView betValue;
    private int betAmount;
    private int minBet;
    private Player currentPlayer;
    private Action lastAction;
    private Action highestBetAction;
    private RefreshGameTask refreshTask;
    private MultiPlayerActivity activity;
    private boolean endAnimationStarted;
    private boolean endAnimationFinished;
    private boolean roundOver;
    private boolean onePlayerLeft;
    private boolean betBarMaxSet;
    private boolean gameOver;
    private boolean gameOverDialogShown;
    private boolean actionButtonsVisible;
    private List<Player> lostPlayers = new ArrayList<>();

    public MultiPlayerThread(long userId, Context context, Button btnCheck, Button btnBet, Button btnFold, SeekBar betBar, TextView betValue, long gameId,GraphicStuff graphics) {
        this.userId = userId;
        this.context = context;
        this.btnCheck = btnCheck;
        this.btnBet = btnBet;
        this.btnFold = btnFold;
        this.betBar = betBar;
        this.betValue = betValue;
        this.gameId = gameId;
        this.graphics = graphics;
        activity = (MultiPlayerActivity) context;
    }

    @Override
    public void run() {
        String getURL = SERVICE_URL + "/game/" + gameId;
        refreshTask = new RefreshGameTask(context);
        refreshTask.setWait(1000);
        refreshTask.execute(new String[]{getURL});
    }

    public void handleGameResponse(Response gameResponse) {
        Log.i(TAG,"handleGameResponse");
        if(null == gameResponse || null == gameResponse.getResponseObject()){
            return;
        }

        Game game = (Game) gameResponse.getResponseObject();

        if (!init) {
            Log.i(TAG,"init");
            this.game = game;
            createPlayers();
            final Player dealer = this.game.getDealer();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graphics.addDealer(dealer);
                }
            });

            init = true;
        }
        updateGame(game);
        if (!handDealt) {
            Log.i(TAG,"dealHand");
            for(final Player player : this.game.getPlayers()){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        graphics.deal(player);
                    }
                });
            }

            handDealt = true;
        }

        final List<Player> players = this.game.getPlayers();
        final List<Player> playersInRound = new ArrayList<>();
        for(Player pl : this.game.getPlayers()){
            if(pl.getPlayerInTurn()){
                playersInRound.add(pl);
            }
        }

        if (flopDealt && game.getBoard().size() == 0 || onePlayerLeft || gameOver) {
            Log.i(TAG,"end");
            if(endAnimationStarted){
                return;
            }
            endAnimationStarted = true;
            endAnimationFinished = false;

            final List<Player> winners = new ArrayList<>();
            for (Player player : this.game.getPlayers()) {
                if (player.isPlayerWinner()) {
                    winners.add(player);
                }
            }
            final List<RelativeLayout> potChips = this.game.getPotChips();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graphics.showCards(players);
                }
            });
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(winners.size() > 1){
                        List<List<RelativeLayout>> chipList = graphics.splitChips(potChips, winners.size());
                        for (List<RelativeLayout> chips : chipList) {
                            for(RelativeLayout chip : chips){
                                graphics.assignChips(chip, winners.get(chipList.indexOf(chips)));
                            }
                        }
                    } else {
                        for(RelativeLayout chip : potChips){
                            graphics.assignChips(chip, winners.get(0));
                        }

                    }
                }
            });

            new Handler().postDelayed(new Runnable(){
                public void run() {
                    endRound();
                }
            }, 2000);

        }
        if (!flopDealt && null != game.getBoard() && game.getBoard().size() >= 3) {
            Log.i(TAG,"dealFlop");
            final List<Card> board = this.game.getBoard();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graphics.dealFlop(board.get(0),board.get(1),board.get(2));
                }
            });

            flopDealt = true;
        }


        if (!turnDealt && null != game.getBoard() && game.getBoard().size() >= 4) {
            Log.i(TAG,"dealTurn");
            final List<Card> board = this.game.getBoard();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graphics.dealTurn(board.get(3));
                }
            });

            turnDealt = true;
        }
        if (!riverDealt && null != game.getBoard() && game.getBoard().size() >= 5) {
            Log.i(TAG,"dealRiver");
            final List<Card> board = this.game.getBoard();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graphics.dealRiver(board.get(4));
                }
            });

            riverDealt = true;
        }

        if (game.getActions().size() > actionSize) {

            if(endAnimationStarted && !endAnimationFinished){
                return;
            }

            boolean newBettingRound = false;
            roundOver = false;

            List<Action> newActions = game.getActions().subList(actionSize, game.getActions().size());

            for (final Action action : newActions) {
                Log.i(TAG,"action");

                if (null != lastAction && action.getActionRound() > lastAction.getActionRound()) {
                    highestBetAction = null;
                    roundOver = false;
                    newBettingRound = false;
                }

                if (null != highestBetAction && action.getBetValue() > highestBetAction.getBetValue()
                        && action.getPlayer().getStackSize() > 0) {
                    newBettingRound = true;
                }

                if (null == lastAction || action.getActionId() > lastAction.getActionId()) {
                    if (action.getActionType().equals(ActionType.BET) || action.getActionType().equals(ActionType.CALL)
                            || action.getActionType().equals(ActionType.RAISE) || action.getActionType().equals(ActionType.ALL_IN)) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                graphics.moveBet(getPlayer(action.getPlayer().getPlayerId()));
                            }
                        });

                    }
                    if (action.getActionType().equals(ActionType.FOLD)) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                graphics.moveFold(getPlayer(action.getPlayer().getPlayerId()));
                            }
                        });
                        onePlayerLeft = playersInRound.size() == 1;
                    }
                    if(newActions.indexOf(action) == newActions.size() - 1){
                        Player raiser = getRaiser(players);
                        if (playersInRound.size() == 1) {
                            collectChips(this.game.getPlayers());
                            final Player dealer = this.game.getDealer();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    graphics.moveDealer(dealer);
                                }
                            });
                            roundOver = true;
                        } else if (null != raiser) {
                            boolean preFlop = null == game.getBoard() || game.getBoard().size() == 0;
                            if (isRoundOver(game.getBigBlind(),action.getPlayer(), playersInRound,preFlop) && !newBettingRound) {
                                collectChips(this.game.getPlayers());
                                final Player dealer = this.game.getDealer();
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        graphics.moveDealer(dealer);
                                    }
                                });
                                roundOver = true;
                            }
                        }
                    }

                }

                if (null == highestBetAction || action.getBetValue() > highestBetAction.getBetValue()) {
                    highestBetAction = action;
                }
                lastAction = action;
                newBettingRound = false;
                betBarMaxSet = false;


            }

            actionSize = game.getActions().size();


        }
        if(!activity.isSendingAction()){
            if(!endAnimationStarted || endAnimationFinished){
                showCurrentPlayer(roundOver);
            }
        }

        if (!betBarMaxSet) {
            for(Player pl : players){
                if(pl.isUser()){

                    betBar.setMax(null == pl.getBetAmount() ? pl.getStackSize() - minBet
                            : pl.getBetAmount() + pl.getStackSize() - minBet);
                    betBar.setProgress(0);
                    betBarMaxSet = true;
                }
            }
            betValue.setText("" + minBet);
        }


    }


    public Response parseGameJson(JSONObject item) throws JSONException {
        Response gameResponse = new Response();

        if (item != null) {
            JSONObject gamesJSON = item.getJSONObject("responseObject");

            Game game = new Game();
            game.setPotSize(gamesJSON.getInt("potSize"));
            game.setGameName(gamesJSON.getString("gameName"));
            game.setGameId(gamesJSON.getLong("gameId"));
            game.setMaxPlayerNumber(gamesJSON.getInt("maxPlayerNumber"));
            game.setStartingStackSize(gamesJSON.getInt("startingStackSize"));
            game.setStartTime(new Date(gamesJSON.getLong("startTime")));
            GameState gameState = GameState.valueOf(gamesJSON.getString("gameState"));
            game.setGameState(gameState);
            game.setPotChips(new ArrayList<RelativeLayout>());
            game.setSmallBlindValue(gamesJSON.getInt("smallBlindValue"));
            game.setBigBlindValue(gamesJSON.getInt("bigBlindValue"));

            JSONArray playerArray = gamesJSON.getJSONArray("players");
            List<Player> playerList = new ArrayList<>();
            for (int counter = 0; counter < playerArray.length(); counter++) {
                Player player = new Player();

                JSONObject playerItem = playerArray.getJSONObject(counter);
                player.setPlayerId(playerItem.getLong("playerId"));
                player.setStackSize(playerItem.getInt("stackSize"));
                player.setPlayerName(playerItem.getJSONObject("user").getString("userName"));
                player.setUserId(playerItem.getJSONObject("user").getLong("userId"));
                player.setOrder(playerItem.getInt("playerOrder"));
                player.setPosition(playerItem.getInt("playerPosition"));
                player.setPlayerTurn(playerItem.getBoolean("playerTurn"));
                player.setPlayerInTurn(playerItem.getBoolean("playerInTurn"));
                player.setPlayerRaiser(playerItem.getBoolean("playerRaiser"));
                player.setPlayerWinner(playerItem.getBoolean("playerWinner"));
                player.setAmountToWin(playerItem.getInt("playerAmountToWin"));
                if (!playerItem.isNull("playerBetAmount")) {
                    player.setBetAmount(playerItem.getInt("playerBetAmount"));
                }
                Card cardOne = new Card();
                JSONObject cardOneItem = playerItem.getJSONObject("cardOne");
                cardOne.setSuit(Suit.valueOf(cardOneItem.getString("suit")));
                cardOne.setValue(cardOneItem.getInt("value"));
                player.setCardOne(cardOne);
                Card cardTwo = new Card();
                JSONObject cardTwoItem = playerItem.getJSONObject("cardTwo");
                cardTwo.setSuit(Suit.valueOf(cardTwoItem.getString("suit")));
                cardTwo.setValue(cardTwoItem.getInt("value"));
                player.setCardTwo(cardTwo);
                if (!playerItem.isNull("cardOneLastRound")) {
                    Card cardOneLastRound = new Card();
                    JSONObject cardOneLastRoundItem = playerItem.getJSONObject("cardOneLastRound");
                    cardOneLastRound.setSuit(Suit.valueOf(cardOneLastRoundItem.getString("suit")));
                    cardOneLastRound.setValue(cardOneLastRoundItem.getInt("value"));
                    player.setCardOneLastRound(cardOneLastRound);

                }
                if (!playerItem.isNull("cardTwoLastRound")) {
                    Card cardTwoLastRound = new Card();
                    JSONObject cardTwoLastRoundItem = playerItem.getJSONObject("cardTwoLastRound");
                    cardTwoLastRound.setSuit(Suit.valueOf(cardTwoLastRoundItem.getString("suit")));
                    cardTwoLastRound.setValue(cardTwoLastRoundItem.getInt("value"));
                    player.setCardTwoLastRound(cardTwoLastRound);
                }
                JSONObject userItem = playerItem.getJSONObject("user");
                if (userItem.getLong("userId") == userId) {
                    player.setIsUser(true);
                    playerId = player.getPlayerId();
                } else {
                    player.setIsUser(false);
                }
                playerList.add(player);
            }
            Collections.sort(playerList, new PlayerComperator());
            game.setPlayers(playerList);
            if (!gamesJSON.isNull("smallBlind")) {
                JSONObject smallBlind = gamesJSON.getJSONObject("smallBlind");
                for(Player player : game.getPlayers()){
                    if(player.getPlayerId() == smallBlind.getLong("playerId")){
                        game.setSmallBlind(player);
                    }
                }
            }
            if (!gamesJSON.isNull("bigBlind")) {
                JSONObject bigBlind = gamesJSON.getJSONObject("bigBlind");
                for(Player player : game.getPlayers()){
                    if(player.getPlayerId() == bigBlind.getLong("playerId")){
                        game.setBigBlind(player);
                    }
                }
            }
            if (!gamesJSON.isNull("dealer")) {
                JSONObject dealer = gamesJSON.getJSONObject("dealer");
                for(Player player : game.getPlayers()){
                    if(player.getPlayerId() == dealer.getLong("playerId")){
                        game.setDealer(player);
                    }
                }
            }


            if (!gamesJSON.isNull("flop")) {
                JSONArray flopArray = gamesJSON.getJSONArray("flop");
                List<Card> flop = new ArrayList<>();
                for (int counter = 0; counter < flopArray.length(); counter++) {
                    Card card = new Card();

                    JSONObject cardItem = flopArray.getJSONObject(counter);
                    card.setSuit(Suit.valueOf(cardItem.getString("suit")));
                    card.setValue(cardItem.getInt("value"));
                    flop.add(card);
                }
                game.setBoard(flop);
            }
            if (!gamesJSON.isNull("turn")) {
                JSONObject turnJSON = gamesJSON.getJSONObject("turn");
                Card turn = new Card();
                turn.setSuit(Suit.valueOf(turnJSON.getString("suit")));
                turn.setValue(turnJSON.getInt("value"));
                game.getBoard().add(turn);
            }

            if (!gamesJSON.isNull("river")) {
                JSONObject riverJSON = gamesJSON.getJSONObject("river");
                Card river = new Card();
                river.setSuit(Suit.valueOf(riverJSON.getString("suit")));
                river.setValue(riverJSON.getInt("value"));
                game.getBoard().add(river);
            }

            JSONArray actionArray = gamesJSON.getJSONArray("actions");
            List<Action> actionList = new ArrayList<>();
            for (int counter = 0; counter < actionArray.length(); counter++) {
                JSONObject actionItem = actionArray.getJSONObject(counter);

                Action action = new Action();
                action.setActionId(actionItem.getLong("actionId"));
                action.setActionType(ActionType.valueOf(actionItem.getString("actionType")));
                action.setBetValue(actionItem.getInt("betValue"));
                action.setActionRound(actionItem.getInt("actionRound"));

                JSONObject playerItem = actionItem.getJSONObject("player");
                for (Player player : game.getPlayers()) {
                    if (player.getPlayerId() == playerItem.getLong("playerId")) {
                        action.setPlayer(player);
                    }
                }


                actionList.add(action);
            }

            game.setActions(actionList);

            gameResponse.setResponseObject(game);
        }
        return gameResponse;
    }

    public void showCurrentPlayer(final boolean roundOver) {
        for (final Player player : this.game.getPlayers()) {
            if (player.isPlayerTurn()) {
                final List<Player> players = this.game.getPlayers();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        graphics.showCurrentPlayer(player,players);

                        currentPlayer = player;
                        showAvailableActionButtons(lastAction, highestBetAction, roundOver, currentPlayer);
                    }
                });


            }
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != currentPlayer && currentPlayer.isUser()) {
                    if(!actionButtonsVisible){
                        graphics.showActionButtons(true);
                        actionButtonsVisible = true;
                    }

                } else {
                    graphics.showActionButtons(false);
                    actionButtonsVisible = false;
                }
            }
        });

    }

    public Player getRaiser(List<Player> playersInRound) {
        for (Player raiser : playersInRound) {
            if (raiser.isPlayerRaiser()) {
                return raiser;
            }
        }
        return null;
    }


    public boolean isRoundOver(Player bigBlind, Player currentPlayer, List<Player> playersInRound, boolean preFlop) {

        for (Player player : playersInRound) {
            if (player.equals(currentPlayer)) {
                int nextPlayerIndex = playersInRound.indexOf(player) + 1;
                if (playersInRound.size() > nextPlayerIndex) {
                    Player nextPlayer = playersInRound.get(nextPlayerIndex);
                    if (nextPlayer.isPlayerRaiser()) {
                        if(nextPlayer.equals(bigBlind) && preFlop){
                            return false;
                        } else {
                            return true;
                        }

                    }
                } else {
                    if (!currentPlayer.equals(playersInRound.get(0)) &&
                            (playersInRound.get(0).isPlayerRaiser() || currentPlayer.equals(bigBlind))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void updateGame(final Game game) {

        Log.i(TAG,"updateGame");
        this.game.setPotSize(game.getPotSize());
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphics.updatePotSize(game.getPotSize());
            }
        });

        if(game.getBoard().size() > this.game.getBoard().size()){
            for(Card card : game.getBoard()){
                if(game.getBoard().indexOf(card) > this.game.getBoard().size() - 1){
                    this.game.getBoard().add(card);
                }
            }
        }
        if(null != game.getSmallBlind()){
            this.game.setSmallBlind(getPlayer(game.getSmallBlind().getPlayerId()));
        }
        if(null != game.getBigBlind()){
            this.game.setBigBlind(getPlayer(game.getBigBlind().getPlayerId()));
        }
        if(null != game.getDealer()){
            this.game.setDealer(getPlayer(game.getDealer().getPlayerId()));
        }
        this.game.setSmallBlindValue(game.getSmallBlindValue());
        this.game.setBigBlindValue(game.getBigBlindValue());

        for (final Player player : this.game.getPlayers()) {

            if(game.getPlayers().contains(player)){
                for (Player newPlayer : game.getPlayers()) {
                    if (player.getPlayerId().equals(newPlayer.getPlayerId())) {
                        player.setPlayerTurn(newPlayer.isPlayerTurn());
                        player.setPlayerInTurn(newPlayer.getPlayerInTurn());
                        player.setPlayerWinner(newPlayer.isPlayerWinner());
                        player.setBetAmount(newPlayer.getBetAmount());
                        player.setPlayerRaiser(newPlayer.isPlayerRaiser());
                        player.setStackSize(newPlayer.getStackSize());
                        player.setStackSize(newPlayer.getStackSize());
                        player.setCardOne(newPlayer.getCardOne());
                        player.setCardTwo(newPlayer.getCardTwo());
                        player.setCardOneLastRound(newPlayer.getCardOneLastRound());
                        player.setCardTwoLastRound(newPlayer.getCardTwoLastRound());
                    }
                }
            } else {
                lostPlayers.add(player);
                this.game.getPlayers().remove(player);

            }

        }
        if(this.game.getPlayers().size() == 1){
            gameOver = true;
            refreshTask.stopTask();
        }

    }

    public Player getPlayer(Long playerId){
        for(Player player : this.game.getPlayers()){
            if(player.getPlayerId().equals(playerId)){
                return player;
            }
        }
        return null;
    }

    public void collectChips(List<Player> players) {

        for (final Player player : players) {
            if(null != player.getChipLayout()){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        graphics.collectChips(player);
                    }
                });
                game.getPotChips().add(player.getChipLayout());
            }
            player.setChipLayout(null);
            player.setBetAmount(0);
        }
    }

    public void endRound() {
        Log.i(TAG,"endRound");

        for (Player player : game.getPlayers()) {
            player.getTextView().setText(player.getPlayerName() + "\n" + player.getStackSize().toString());
        }

        List<Player> playerList = new ArrayList<>();
        playerList.addAll(game.getPlayers());
        for (final Player player : playerList) {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graphics.removePlayerCards(player);
                }
            });
            player.setCard1View(null);
            player.setCard2View(null);
            player.setAmountInPot(0);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (RelativeLayout chip : game.getPotChips()) {
                    graphics.removeChips(chip);
                }
                graphics.removeBoard();
            }
        });
        game.setBoard(new ArrayList<Card>());
        endAnimationFinished = true;
        endAnimationStarted = false;
        onePlayerLeft = false;
        flopDealt = false;
        turnDealt = false;
        riverDealt = false;
        handDealt = false;

        for(final Player lostPlayer : lostPlayers){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graphics.removeSeat(lostPlayer);
                    if(lostPlayer.isUser() && !gameOverDialogShown){
                        AlertDialog alertDialog = new AlertDialog.Builder(
                                context).create();

                        alertDialog.setTitle("Game Over!");
                        alertDialog.setMessage("You have finished " + (game.getPlayers().size() + 1) + ". place!");

                        final long userId = lostPlayer.getUserId();
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Back to Game Browser", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent gameBrowserActivity = new Intent(context.getApplicationContext(), GameBrowserActivity.class);
                                gameBrowserActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Bundle bundle = new Bundle();
                                bundle.putLong("userId", userId);
                                gameBrowserActivity.putExtras(bundle);
                                context.startActivity(gameBrowserActivity);
                            }
                        });

                        alertDialog.show();
                        gameOverDialogShown = true;
                    }
                }
            });
        }

        if(this.game.getPlayers().size() == 1 && !gameOverDialogShown){
            AlertDialog alertDialog = new AlertDialog.Builder(
                    context).create();

            alertDialog.setTitle("Congratulation!");
            alertDialog.setMessage("You have won the game!");

            final long userId = game.getPlayers().get(0).getUserId();
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Back to Game Browser", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent gameBrowserActivity = new Intent(context.getApplicationContext(), GameBrowserActivity.class);
                    gameBrowserActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Bundle bundle = new Bundle();
                    bundle.putLong("userId", userId);
                    gameBrowserActivity.putExtras(bundle);
                    context.startActivity(gameBrowserActivity);
                }
            });

            alertDialog.show();
        }

    }

    public void createPlayers() {
        for (final Player player : game.getPlayers()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    player.setTextView(graphics.createPlayerView(player));
                }
            });
        }
    }

    public void showAvailableActionButtons(Action lastAction, Action highestBetAction, boolean roundOver, Player currentPlayer) {
        List<ActionType> availableActions = new ArrayList<>();
        if (null == lastAction || roundOver) {
            availableActions.add(ActionType.CHECK);
            availableActions.add(ActionType.BET);
            availableActions.add(ActionType.FOLD);
        } else if (currentPlayer.equals(this.game.getBigBlind()) && currentPlayer.getBetAmount().equals(highestBetAction.getBetValue())){
            availableActions.add(ActionType.CHECK);
            availableActions.add(ActionType.RAISE);
            availableActions.add(ActionType.FOLD);

        } else {
            Boolean higherStackThanBetAmount = highestBetAction == null ? null : game.getUser().getStackSize() > highestBetAction.getBetValue();

            availableActions = graphics.getAvailableActions(lastAction.getActionType(), highestBetAction == null ? null : highestBetAction.getActionType(),
                    higherStackThanBetAmount);
        }


        if (availableActions.contains(ActionType.CHECK)) {
            btnCheck.setVisibility(View.VISIBLE);
            btnCheck.setText("CHECK");
        }

        if (availableActions.contains(ActionType.CALL)) {
            btnCheck.setText("CALL");
        }
        if (availableActions.contains(ActionType.RAISE)) {
            btnBet.setText("RAISE");
            if (game.getUser().getStackSize() > highestBetAction.getBetValue() * 2) {
                minBet = highestBetAction.getBetValue() * 2;
            } else {
                minBet = game.getUser().getStackSize();
                betBar.setMax(0);
            }


        } else {
            btnBet.setText("BET");
            minBet = game.getBigBlindValue();
//            betValue.setText("" + minBet);
        }
        if (availableActions.contains(ActionType.ALL_IN)) {
            btnBet.setText("ALL IN");
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    btnCheck.setVisibility(View.INVISIBLE);
                }
            }, 200);

        }

    }


    public int getMinBet() {
        return minBet;
    }

    public void setMinBet(int minBet) {
        this.minBet = minBet;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public boolean isActionButtonsVisible() {
        return actionButtonsVisible;
    }

    public void setActionButtonsVisible(boolean actionButtonsVisible) {
        this.actionButtonsVisible = actionButtonsVisible;
    }

    public Player getUser(){
        for(Player pl : game.getPlayers()){
            if(pl.isUser()){
                return pl;
            }
        }
        return null;
    }

    private class RefreshGameTask extends RefreshTask {

        private RefreshGameTask(Context context) {
            super(context);
        }

        @Override
        public void handleResponse(Response response) {
            Log.i(TAG,"handleResp animStart: " + endAnimationFinished);
            Log.i(TAG,"handleResp animEnd: " + endAnimationFinished);
            if(endAnimationStarted && !endAnimationFinished){
                return;
            }
            handleGameResponse(response);

        }

        @Override
        public Response parseJson(JSONObject jsonObject) {
            try {
                return parseGameJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
