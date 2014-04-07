package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Action;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;
import hu.onlineholdem.restclient.enums.GameState;
import hu.onlineholdem.restclient.enums.Suit;
import hu.onlineholdem.restclient.response.Response;
import hu.onlineholdem.restclient.task.RefreshTask;
import hu.onlineholdem.restclient.task.WebServiceTask;
import hu.onlineholdem.restclient.util.GraphicStuff;

public class MultiPlayerActivity extends Activity {

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
    private static final String SERVICE_URL = "http://192.168.1.103:8010/rest";
    private static final String TAG = "MultiplayerActivity";
    private GraphicStuff graphics;
    private Player currentPlayer;
    private Action lastAction;
    private Action highestBetAction;
    private RefreshGameTask refreshTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.multi_player_layout);

        Bundle bundle = getIntent().getExtras();
        userId = bundle.getLong("userId");
        gameId = bundle.getLong("gameId");

        graphics = new GraphicStuff(this);


        String getURL = SERVICE_URL + "/game/" + gameId;
        refreshTask = new RefreshGameTask(this);
        refreshTask.execute(new String[]{getURL});
    }

    public void handleGameResponse(Response gameResponse) {

        Game game = (Game) gameResponse.getResponseObject();

        if (!init) {
            graphics.setGame(game);
            graphics.createPlayers();
            init = true;
        }

        if (!handDealt) {
            graphics.deal();
            handDealt = true;
        }
        if (cardsShown) {
            SystemClock.sleep(2000);
            graphics.endRound();
            cardsShown = false;
            flopDealt = false;
            turnDealt = false;
            riverDealt = false;
            handDealt = false;


        }
        List<Player> playersInRound = new ArrayList<>();
        for(Player pl : game.getPlayers()){
            if(pl.getPlayerInTurn()){
                playersInRound.add(pl);
            }
        }

        if (flopDealt && game.getBoard().size() == 0 || playersInRound.size() == 1) {
            graphics.updateGame(game);
            List<Player> winners = new ArrayList<>();
            for (Player player : graphics.getGame().getPlayers()) {
                if (player.isPlayerWinner()) {
                    winners.add(player);
                }
            }
            graphics.showCards();
            graphics.assignChips(winners);
            cardsShown = true;
        }
        graphics.updateGame(game);
        if (!flopDealt && null != game.getBoard() && game.getBoard().size() >= 3) {
            graphics.dealFlop();
            flopDealt = true;
        }


        if (!turnDealt && null != game.getBoard() && game.getBoard().size() >= 4) {
            graphics.dealTurn();
            turnDealt = true;
        }
        if (!riverDealt && null != game.getBoard() && game.getBoard().size() >= 5) {
            graphics.dealRiver();
            riverDealt = true;
        }

        if (game.getActions().size() > actionSize) {
            boolean newBettingRound = false;
            boolean roundOver = false;

            List<Action> newActions = game.getActions().subList(actionSize, game.getActions().size());

            for (Action action : newActions) {

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
                        graphics.moveBet(action.getBetValue(), action.getPlayer().getPlayerId());
                    }
                    if (action.getActionType().equals(ActionType.FOLD)) {
                        graphics.moveFold(action.getPlayer().getPlayerId());
                    }
                    Player raiser = getRaiser(playersInRound);
                    if (playersInRound.size() == 1) {
                        graphics.collectChips(playersInRound);
                        roundOver = true;
                    } else if (null != raiser) {
                        if (isRoundOver(raiser, action.getPlayer(), playersInRound) && !newBettingRound) {
                            graphics.collectChips(playersInRound);
                            roundOver = true;
                        }
                    }
                }

                if (null == highestBetAction || action.getBetValue() > highestBetAction.getBetValue()) {
                    highestBetAction = action;
                }
                lastAction = action;
                newBettingRound = false;

            }

            showCurrentPlayer(roundOver);
            actionSize = game.getActions().size();


        }
        if (game.getActions().size() == 0) {
            showCurrentPlayer(false);
        }

    }


    public void postAction(View vw) {

        String postURL = SERVICE_URL + "/action";

        TextView betValue = (TextView) findViewById(R.id.betValue);

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this, "Posting data...");

        Button button = (Button) vw;

        ActionType actionType = null;
        int btnId = button.getId();
        if (btnId == R.id.btnCheck)
            actionType = button.getText().toString().equals("CHECK") ? ActionType.CHECK : ActionType.CALL;
        if (btnId == R.id.btnBet) {
            if (button.getText().toString().equals("BET")) {
                actionType = ActionType.BET;
            } else if (button.getText().toString().equals("RAISE")) {
                actionType = ActionType.RAISE;
            } else {
                actionType = ActionType.ALL_IN;
            }
        }
        if (btnId == R.id.btnFold)
            actionType = ActionType.FOLD;

        wst.addNameValuePair("actionType", actionType.name());

        if (actionType.equals(ActionType.BET) || actionType.equals(ActionType.RAISE)) {
            wst.addNameValuePair("betValue", betValue.getText().toString().equals("") ? "0" : betValue.getText().toString());
        } else {
            wst.addNameValuePair("betValue", "0");
        }

        wst.addNameValuePair("playerId", String.valueOf(playerId));
        wst.addNameValuePair("gameId", String.valueOf(gameId));

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{postURL});
        graphics.showActionButtons(false);


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
                JSONObject userItem = playerItem.getJSONObject("user");
                if (userItem.getLong("userId") == userId) {
                    player.setIsUser(true);
                    playerId = player.getPlayerId();
                } else {
                    player.setIsUser(false);
                }
                playerList.add(player);
            }
            game.setPlayers(playerList);


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

    public void showCurrentPlayer(boolean roundOver) {
        for (Player player : graphics.getGame().getPlayers()) {
            if (player.isPlayerTurn()) {
//                if (!player.equals(currentPlayer)) {
                    graphics.showCurrentPlayer(player);
                    graphics.showAvailableActionButtons(lastAction, highestBetAction, roundOver);
                    currentPlayer = player;
//                }

            }
        }
        if (null != currentPlayer && currentPlayer.isUser()) {
            graphics.showActionButtons(true);
        } else {
            graphics.showActionButtons(false);
        }
    }

    public Player getRaiser(List<Player> playersInRound) {
        for (Player raiser : playersInRound) {
            if (raiser.isPlayerRaiser()) {
                return raiser;
            }
        }
        return null;
    }

    public boolean isRoundOver(Player raiser, Player currentPlayer, List<Player> playersInRound) {
        if ((playersInRound.indexOf(raiser) == playersInRound.indexOf(currentPlayer) + 1)
                || (playersInRound.indexOf(raiser) == 0 && playersInRound.indexOf(currentPlayer) == playersInRound.size() - 1)) {
            return true;
        }
        return false;
    }


    private class RefreshGameTask extends RefreshTask {

        private RefreshGameTask(Context context) {
            super(context);
        }

        @Override
        public void handleResponse(Response response) {
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

    private class PostGameTask extends WebServiceTask {

        public PostGameTask(int taskType, Context mContext, String processMessage) {
            super(taskType, mContext, processMessage);
        }

        @Override
        public void handleResponse(Response response) {
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
