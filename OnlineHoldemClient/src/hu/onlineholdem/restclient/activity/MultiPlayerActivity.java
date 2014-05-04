package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
    private static final String SERVICE_URL = "http://192.168.1.104:8010/rest";
    private static final String TAG = "MultiplayerActivity";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.multi_player_layout);

        context = this;
        betBar = (SeekBar) findViewById(R.id.betBar);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        btnBet = (Button) findViewById(R.id.btnBet);
        btnFold = (Button) findViewById(R.id.btnFold);
        betValue = (TextView) findViewById(R.id.betValue);

        betBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                betValue.setText("" + (i + minBet));
                betAmount = i + minBet;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

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
            this.game = game;
            createPlayers();
            graphics.addDealer(this.game.getDealer());
            init = true;
        }

        if (!handDealt) {
            for(Player player : this.game.getPlayers()){
                graphics.deal(player);
            }

            handDealt = true;
        }
        if (cardsShown) {
            SystemClock.sleep(2000);
            endRound();
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
            updateGame(game);
            List<Player> winners = new ArrayList<>();
            for (Player player : this.game.getPlayers()) {
                if (player.isPlayerWinner()) {
                    winners.add(player);
                }
            }
            graphics.showCards(winners);
            if(winners.size() > 1){
                List<List<RelativeLayout>> chipList = graphics.splitChips(this.game.getPotChips(), winners.size());
                for (List<RelativeLayout> chips : chipList) {
                    for(RelativeLayout chip : chips){
                        graphics.assignChips(chip, winners.get(chipList.indexOf(chips)));
                    }
                }
            } else {
                for(RelativeLayout chip : this.game.getPotChips()){
                    graphics.assignChips(chip, winners.get(0));
                }

            }

            cardsShown = true;
        }
        updateGame(game);
        if (!flopDealt && null != game.getBoard() && game.getBoard().size() >= 3) {
            List<Card> board = this.game.getBoard();
            graphics.dealFlop(board.get(0),board.get(1),board.get(2));
            flopDealt = true;
        }


        if (!turnDealt && null != game.getBoard() && game.getBoard().size() >= 4) {
            graphics.dealTurn(this.game.getBoard().get(3));
            turnDealt = true;
        }
        if (!riverDealt && null != game.getBoard() && game.getBoard().size() >= 5) {
            graphics.dealRiver(this.game.getBoard().get(4));
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
//                        graphics.moveBet(action.getBetValue(), action.getPlayer().getPlayerId());
                        graphics.moveBet(getPlayer(action.getPlayer().getPlayerId()));
                    }
                    if (action.getActionType().equals(ActionType.FOLD)) {
//                        graphics.moveFold(action.getPlayer().getPlayerId());
                        graphics.moveFold(getPlayer(action.getPlayer().getPlayerId()));
                    }
                    Player raiser = getRaiser(playersInRound);
                    if (playersInRound.size() == 1) {
                        collectChips(playersInRound);
                        roundOver = true;
                    } else if (null != raiser) {
                        if (isRoundOver(raiser, action.getPlayer(), playersInRound) && !newBettingRound) {
                            collectChips(playersInRound);
                            graphics.moveDealer(this.game.getDealer());
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
            JSONObject smallBlind = gamesJSON.getJSONObject("smallBlind");
            JSONObject bigBlind = gamesJSON.getJSONObject("bigBlind");
            JSONObject dealer = gamesJSON.getJSONObject("dealer");
            for(Player player : game.getPlayers()){
                if(player.getPlayerId() == smallBlind.getLong("playerId")){
                    game.setSmallBlind(player);
                }
                if(player.getPlayerId() == bigBlind.getLong("playerId")){
                    game.setBigBlind(player);
                }
                if(player.getPlayerId() == dealer.getLong("playerId")){
                    game.setDealer(player);
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

    public void showCurrentPlayer(boolean roundOver) {
        for (Player player : this.game.getPlayers()) {
            if (player.isPlayerTurn()) {
//                if (!player.equals(currentPlayer)) {
                    graphics.showCurrentPlayer(player, this.game.getPlayers());
                    if (player.isUser()) {
                        betBar.setMax(player.getStackSize() - minBet);
                        betBar.setProgress(0);
                    }
                    showAvailableActionButtons(lastAction, highestBetAction, roundOver);
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

    public void updateGame(Game game) {

        this.game.setPotSize(game.getPotSize());
        graphics.updatePotSize(game.getPotSize());
        if(game.getBoard().size() > this.game.getBoard().size()){
            for(Card card : game.getBoard()){
                if(game.getBoard().indexOf(card) > this.game.getBoard().size() - 1){
                    this.game.getBoard().add(card);
                }
            }
        }
        this.game.setDealer(getPlayer(game.getDealer().getPlayerId()));
        this.game.setSmallBlind(getPlayer(game.getSmallBlind().getPlayerId()));
        this.game.setSmallBlindValue(game.getSmallBlindValue());
        this.game.setBigBlind(getPlayer(game.getBigBlind().getPlayerId()));
        this.game.setBigBlindValue(game.getBigBlindValue());
        for (Player player : this.game.getPlayers()) {

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
                    }
                }
            } else {
                graphics.removeSeat(player);
                if(player.isUser()){
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            this).create();

                    alertDialog.setTitle("Game Over!");
                    alertDialog.setMessage("You have finished " + game.getPlayers().size() + 1 + ". place!");

                    final long userId = player.getUserId();
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
                graphics.collectChips(player);
                game.getPotChips().add(player.getChipLayout());
            }

            player.setChipLayout(null);
            player.setBetAmount(0);
        }
    }

    public void endRound() {

        for (Player player : game.getPlayers()) {
            player.getTextView().setText(player.getPlayerName() + "\n" + player.getStackSize().toString());
        }

        List<Player> playerList = new ArrayList<>();
        playerList.addAll(game.getPlayers());
        for (Player player : playerList) {
            if (player.getStackSize() == 0) {
//                seats.removeView(player.getTextView());
                graphics.removeSeat(player);
                game.getPlayers().remove(player);
            }
//            board.removeView(player.getCard1View());
//            board.removeView(player.getCard2View());
            graphics.removePlayerCards(player);
            player.setCard1View(null);
            player.setCard2View(null);
            player.setAmountInPot(0);
        }
        for (RelativeLayout chip : game.getPotChips()) {
//            board.removeView(chip);
            graphics.removeChips(chip);
        }
//        board.removeView(flop1);
//        board.removeView(flop2);
//        board.removeView(flop3);
//        board.removeView(turn);
//        board.removeView(river);
        graphics.removeBoard();
        game.setBoard(new ArrayList<Card>());
    }

    public void createPlayers() {
        for (Player player : game.getPlayers()) {

//            TextView textView = new TextView(context);
//            textView.setBackgroundResource(R.drawable.seatnotactive);
//
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 5, screenHeight / 6);
//            Position position = graphics.getPlayerPosition(player.getPosition());
//            layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);
//
//            textView.setTop(position.getTop());
//            textView.setLeft(position.getLeft());
//            textView.setLayoutParams(layoutParams);
//            textView.setText(player.getPlayerName() + "\n" + player.getStackSize().toString());
//            textView.setGravity(Gravity.CENTER);
//            textView.setTextColor(0xFF000000);
//            textView.setTextSize(15);

            player.setTextView(graphics.createPlayerView(player));

//            seats.addView(textView);
        }
    }
    public void showAvailableActionButtons(Action lastAction, Action highestBetAction, boolean roundOver) {
        List<ActionType> availableActions = new ArrayList<>();
        if (null == lastAction || roundOver) {
            availableActions.add(ActionType.CHECK);
            availableActions.add(ActionType.BET);
            availableActions.add(ActionType.FOLD);
        } else {
            Boolean higherStackThanBetAmount = highestBetAction == null ? null : game.getUser().getStackSize() > highestBetAction.getBetValue();

            availableActions = graphics.getAvailableActions(lastAction.getActionType(), highestBetAction == null ? null : highestBetAction.getActionType(),
                    higherStackThanBetAmount);
        }


        btnCheck.setVisibility(View.VISIBLE);
        if (availableActions.contains(ActionType.CALL)) {
            btnCheck.setText("CALL");
        } else {
            btnCheck.setText("CHECK");
        }
        if (availableActions.contains(ActionType.RAISE)) {
            btnBet.setText("RAISE");
            if (game.getUser().getStackSize() > highestBetAction.getBetValue() * 2) {
                minBet = highestBetAction.getBetValue() * 2;
            } else {
                minBet = game.getUser().getStackSize();
                betBar.setMax(0);
            }

            betValue.setText("" + minBet);
        } else {
            btnBet.setText("BET");
            minBet = game.getBigBlindValue();
            betValue.setText("" + minBet);
        }
        if (availableActions.contains(ActionType.ALL_IN)) {
            btnBet.setText("ALL IN");
            btnCheck.setVisibility(View.GONE);
        }

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
