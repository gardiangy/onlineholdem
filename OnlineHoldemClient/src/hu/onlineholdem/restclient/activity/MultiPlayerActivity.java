package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import hu.onlineholdem.restclient.util.Position;

public class MultiPlayerActivity extends Activity {

    private boolean init = false;
    private boolean flopDealt = false;
    private boolean handDealt = false;
    private long userId;
    private long playerId;
    private long gameId;
    private long lastActionId = -1;
    private int actionSize = 0;
    private static final String SERVICE_URL = "http://192.168.1.103:8080/rest";
    private GraphicStuff graphics;

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
        RefreshGameTask refreshTask = new RefreshGameTask(this);
        refreshTask.execute(new String[]{getURL});
    }

    public void handleGameResponse(Response gameResponse) {

        Game game = (Game) gameResponse.getResponseObject();

        if (!init) {
            graphics.setGame(game);
            graphics.createPlayers();
            init = true;
        }
        if(null != game.getBoard() && game.getBoard().size() > 2){
            if(!flopDealt){
                graphics.dealFlop();
                flopDealt = true;
            }
        }
        if (!handDealt) {
            graphics.deal();
            handDealt = true;
        }
        graphics.updateGame(game);
        graphics.showCurrentPlayer();
        if(game.getActions().size() > actionSize){
            for(Action action : game.getActions()){
                if(action.getActionId() > lastActionId || lastActionId == -1){
                    if(action.getActionType().equals(ActionType.BET)){
                        graphics.moveBet(action.getBetValue(),action.getPlayer().getPlayerId());
                    }
                    if(action.getActionType().equals(ActionType.FOLD)){
                        graphics.moveFold(action.getPlayer().getPlayerId());
                    }
                }
                if(game.getActions().indexOf(action) == game.getActions().size() - 1){
                    lastActionId = action.getActionId();
                }
                if(action.getPlayer().getOrder() == game.getPlayers().size()){
                    List<Player> playersInRound = new ArrayList<>();
                    for(Player player : graphics.getGame().getPlayers()){
                        if(player.getPlayerInTurn()){
                            playersInRound.add(player);
                        }
                    }
                    graphics.collectChips(playersInRound);
                }
            }
            actionSize = game.getActions().size();

        }


    }



    public void postAction(View vw) {

        String postURL = SERVICE_URL + "/action";

        TextView betValue = (TextView) findViewById(R.id.betValue);

        WebServiceTask wst = new PostGameTask(WebServiceTask.POST_TASK, this,"Posting data...");

        ActionType actionType = null;
        int btnId = vw.getId();
        if (btnId == R.id.btnCheck)
            actionType = ActionType.CHECK;
        if (btnId == R.id.btnBet)
            actionType = ActionType.BET;
        if (btnId == R.id.btnFold)
            actionType = ActionType.FOLD;

        wst.addNameValuePair("actionType", actionType.name());
        wst.addNameValuePair("betValue", betValue.getText().toString().equals("") ? "0" : betValue.getText().toString());
        wst.addNameValuePair("playerId", String.valueOf(playerId));
        wst.addNameValuePair("gameId", String.valueOf(gameId));

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{postURL});


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
                if(!playerItem.isNull("playerBetAmount")){
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
                if(userItem.getLong("userId") == userId){
                    player.setIsUser(true);
                    playerId = player.getPlayerId();
                } else {
                    player.setIsUser(false);
                }
                playerList.add(player);
            }
            game.setPlayers(playerList);


            if(!gamesJSON.isNull("flop")){
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
            if(!gamesJSON.isNull("turn")){
                JSONObject turnJSON = gamesJSON.getJSONObject("turn");
                Card turn = new Card();
                turn.setSuit(Suit.valueOf(turnJSON.getString("suit")));
                turn.setValue(turnJSON.getInt("value"));
                game.getBoard().add(turn);
            }

            if(!gamesJSON.isNull("river")){
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
                for(Player player : game.getPlayers()){
                    if(player.getPlayerId() == playerItem.getLong("playerId")){
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

    private class PostGameTask extends WebServiceTask{

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
