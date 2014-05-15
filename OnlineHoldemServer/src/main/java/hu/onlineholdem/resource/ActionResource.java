package hu.onlineholdem.resource;

import hu.onlineholdem.bean.BlindCreatorBean;
import hu.onlineholdem.bo.ActionBO;
import hu.onlineholdem.dao.*;
import hu.onlineholdem.entity.*;
import hu.onlineholdem.enums.ActionType;
import hu.onlineholdem.enums.GameState;
import hu.onlineholdem.enums.ResponseType;
import hu.onlineholdem.response.Response;
import hu.onlineholdem.util.EvaluatedHand;
import hu.onlineholdem.util.HandEvaluator;
import hu.onlineholdem.util.PlayerComperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Path("/action")
@Component
public class ActionResource {

    @Autowired
    private GameDAO gameDAO;

    @Autowired
    private ActionDAO actionDAO;

    @Autowired
    private PlayerDAO playerDAO;

    @Autowired
    private CardDAO cardDAO;

    @Autowired
    private BlindCreatorBean blindCreator;

    @Autowired
    private UserDAO userDAO;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAction(ActionBO actionBO) {

        System.out.println(actionBO.toString());

        Player player = playerDAO.findOne(actionBO.getPlayerId());

        ActionType actionType = ActionType.valueOf(actionBO.getActionType());
        Action action = new Action();
        action.setActionType(actionType);
        action.setBetValue(Integer.parseInt(actionBO.getBetValue()));
        action.setPlayer(player);

        Game game = gameDAO.findOne(actionBO.getGameId());

        action.setGame(game);

        List<Player> playersInRound = new ArrayList<>();
        for (Player pl : game.getPlayers()) {
            if (pl.getPlayerInTurn()) {
                playersInRound.add(pl);
            }
        }
        Collections.sort(playersInRound, new PlayerComperator());

        int highestBetAmount = 0;
        Action lastAction = null;
        boolean preFlop = game.getFlop() == null || game.getFlop().size() == 0;

        if (game.getActions().size() == 0) {
            action.setActionRound(1);
        } else {
            lastAction = game.getActions().get(game.getActions().size() - 1);

            if (player.getPlayerOrder() <= lastAction.getPlayer().getPlayerOrder() && !makeMovesAgain(playersInRound,game.getBigBlind(),preFlop)) {
                action.setActionRound(lastAction.getActionRound() + 1);
                setRaiser(player, game);
            } else {
                action.setActionRound(lastAction.getActionRound());
            }
            Action highestBetAction = getHighestBetAction(game.getActions(), action.getActionRound());
            highestBetAmount = highestBetAction == null ? action.getBetValue() : highestBetAction.getBetValue();
        }

        for (Player pl : game.getPlayers()) {
            if (pl.getPlayerInTurn()) {
                if (pl.getPlayerId().equals(player.getPlayerId())) {
                    if (action.getBetValue() > highestBetAmount || (null != lastAction && action.getActionRound() > lastAction.getActionRound())) {
                        setRaiser(pl, game);
                    }
                    if (actionType.equals(ActionType.BET)) {
                        int newStackSize = pl.getStackSize() - action.getBetValue();
                        pl.setStackSize(newStackSize);
                        pl.setPlayerBetAmount(action.getBetValue());
                        pl.setPlayerAmountInPot(null == pl.getPlayerAmountInPot() ? pl.getPlayerBetAmount() : pl.getPlayerAmountInPot() + pl.getPlayerBetAmount());
                    }
                    if (actionType.equals(ActionType.RAISE)) {
                        int amount = action.getBetValue() - pl.getPlayerBetAmount();
                        int newStackSize = pl.getStackSize() - amount;
                        pl.setStackSize(newStackSize);
                        pl.setPlayerBetAmount(action.getBetValue());
                        pl.setPlayerAmountInPot(null == pl.getPlayerAmountInPot() ? amount : pl.getPlayerAmountInPot() + amount);
                    }
                    if (actionType.equals(ActionType.FOLD)) {
                        pl.setPlayerInTurn(false);
                        playersInRound.remove(pl);
                    }
                    if (actionType.equals(ActionType.CALL)) {
                        int amountToCall = highestBetAmount - pl.getPlayerBetAmount();
                        int newStackSize = amountToCall > pl.getStackSize() ? 0 : pl.getStackSize() - amountToCall;
                        pl.setStackSize(newStackSize);
                        pl.setPlayerBetAmount(highestBetAmount > pl.getStackSize() ? pl.getStackSize() : highestBetAmount);
                        action.setBetValue(amountToCall);
                        pl.setPlayerAmountInPot(null == pl.getPlayerAmountInPot() ? amountToCall : pl.getPlayerAmountInPot() + amountToCall);
                    }
                    if (actionType.equals(ActionType.ALL_IN)) {
                        pl.setPlayerBetAmount(pl.getStackSize());
                        pl.setStackSize(0);
                        action.setBetValue(pl.getPlayerBetAmount());
                        pl.setPlayerAmountInPot(null == pl.getPlayerAmountInPot() ? pl.getPlayerBetAmount() : pl.getPlayerAmountInPot() + pl.getPlayerBetAmount());
                    }
                    if (actionType.equals(ActionType.CHECK) || actionType.equals(ActionType.FOLD)) {
                        action.setBetValue(0);
                        pl.setPlayerBetAmount(0);
                    }

                } else {
                    if (isBettingRoundOver(playersInRound, player, game.getBigBlind(),preFlop) && !makeMovesAgain(playersInRound,game.getBigBlind(),preFlop)) {
                        System.out.println("betting round over");
                        for (Player nextPlayer : game.getPlayers()) {
                            if (nextPlayer.equals(playersInRound.get(0))) {
                                setNextPlayer(game.getPlayers(),nextPlayer);
                                System.out.println("nextPlayer: " + nextPlayer.getUser().getUserName());
                            }
                        }
                    } else {
                        System.out.println("betting round not over");
                        if (playersInRound.indexOf(pl) == playersInRound.indexOf(player) + 1) {
                            setNextPlayer(game.getPlayers(),pl);
                            System.out.println("nextPlayer: " + pl.getUser().getUserName());
                        }
                    }

                }
            }


        }

        boolean newRound = false;

        int newPotSize = game.getPotSize();
        if (action.getActionType().equals(ActionType.RAISE)) {
            Integer amount = getPreviousBetAmount(player, action.getActionRound(), game.getActions());
            newPotSize += action.getBetValue() - amount;
        } else {
            newPotSize += action.getBetValue();
        }
        game.setPotSize(newPotSize);

        int playersWithStack = 0;

        if (game.getActions().size() == 0) {
            action.setActionRound(1);
        } else {

            if (playersInRound.size() == 1) {
                endRound(playersInRound, game, action);
                startNewRound(game);
                newRound = true;
            } else if (isBettingRoundOver(playersInRound, player,game.getBigBlind(),preFlop) && !makeMovesAgain(playersInRound,game.getBigBlind(),preFlop)) {
                System.out.println("round over");

                for (Player pl : game.getPlayers()) {
                    if (pl.getStackSize() > 0) {
                        playersWithStack++;
                    }
                }

                if (null == game.getFlop() || game.getFlop().size() == 0) {
                    System.out.println("dealing flop");
                    game.setFlop(new ArrayList<Card>());
                    game.getFlop().add(getNextCard(game));
                    game.getFlop().add(getNextCard(game));
                    game.getFlop().add(getNextCard(game));

                    if(playersWithStack < 2){
                        game.setTurn(getNextCard(game));
                        System.out.println("dealing turn");
                        game.setRiver(getNextCard(game));
                        System.out.println("dealing river");
                        int playerSize = endRound(playersInRound, game, action);
                        if(playerSize > 1){
                            startNewRound(game);
                        }
                        newRound = true;
                    }
                } else if (null == game.getTurn()) {
                    System.out.println("dealing turn");
                    game.setTurn(getNextCard(game));
                    if(playersWithStack < 2){
                        game.setRiver(getNextCard(game));
                        System.out.println("dealing river");
                        int playerSize = endRound(playersInRound, game, action);
                        if(playerSize > 1){
                            startNewRound(game);
                        }
                        newRound = true;
                    }
                } else {
                    if (null == game.getRiver()) {
                        System.out.println("dealing river");
                        game.setRiver(getNextCard(game));
                        if(playersWithStack < 2){
                            int playerSize =  endRound(playersInRound, game, action);
                            if(playerSize > 1){
                                startNewRound(game);
                            }
                            newRound = true;
                        }
                    } else {

                        int playerSize = endRound(playersInRound, game, action);
                        if(playerSize > 1){
                            startNewRound(game);
                        }
                        newRound = true;
                    }
                }
                for (Player pl : game.getPlayers()) {
                    pl.setPlayerBetAmount(0);
                }

            }

        }



        List<Player> playerList = new ArrayList<>();
        List<Player> playersToRemove= new ArrayList<>();
        playerList.addAll(game.getPlayers());
        if(newRound){
            for(Player pl : playerList){
                if(pl.getStackSize() == 0){
                    game.getPlayers().remove(pl);
                    playersToRemove.add(pl);
                }
            }
        }


        if(!playersToRemove.contains(action.getPlayer())){
            game.getActions().add(action);
        }
        if(game.getPlayers().size() == 1){
            game.setGameState(GameState.FINISHED);
        }

        Game persistedGame = gameDAO.save(game);

        if(newRound && playersWithStack > 1){
            blindCreator.setGameId(persistedGame.getGameId());
            blindCreator.setAddBlinds(true);
        }

        Response response = new Response();
        response.setResponseObject(persistedGame);
        response.setResponseType(ResponseType.OK);

        return response;
    }

    @GET
    @Path("{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActions(@PathParam("gameId") Long gameId) {

        Game game = gameDAO.findOne(gameId);
        List<Action> actions = actionDAO.findByGameOrderByActionIdAsc(game);

        Response response = new Response();
        response.setResponseObject(actions);
        response.setResponseType(ResponseType.OK);

        return response;
    }

    public Action getHighestBetAction(List<Action> actions, int actionRound) {
        Action higestBetAction = null;
        int higestBetAmount = 0;
        for (Action action : actions) {
            if (action.getActionRound() == actionRound) {
                if (action.getBetValue() >= higestBetAmount) {
                    higestBetAmount = action.getBetValue();
                    higestBetAction = action;
                }
            }
        }
        return higestBetAction;
    }

    public Card getNextCard(Game game) {
        List<Card> deck = cardDAO.findAll();
        Collections.shuffle(deck);
        for (Card card : deck) {
            if (!game.getFlop().contains(card)) {
                if (!card.equals(game.getTurn())) {

                    boolean playerHasThatCard = false;
                    for (Player pl : game.getPlayers()) {
                        if (pl.getCardOne().equals(card) || pl.getCardTwo().equals(card)) {
                            playerHasThatCard = true;
                        }
                    }
                    if (!playerHasThatCard) {
                        return card;
                    }
                }
            }
        }
        return null;
    }


    public boolean makeMovesAgain(List<Player> players, Player bigBlind, boolean preFlop) {
        int highestBetAmount = players.get(0).getPlayerBetAmount();
        for(Player player : players){
            if(player.getPlayerBetAmount() > highestBetAmount){
                highestBetAmount = player.getPlayerBetAmount();
            }
        }
        for (Player player : players) {
            if(preFlop && player.equals(bigBlind)){
                if(player.getPlayerBetAmount() + player.getPlayerAmountInPot() != highestBetAmount && player.getStackSize() > 0
                        && player.getActions().get(0).getActionType().equals(ActionType.CHECK)){
                    return true;
                }
            }
            else if ((null != player.getPlayerBetAmount() && player.getPlayerBetAmount() != highestBetAmount && player.getStackSize() > 0)) {
                return true;
            }
        }
        return false;
    }

    public void setNextPlayer(List<Player> players, Player nextPlayer){
        for(Player player : players){
            if(player.equals(nextPlayer)){
                player.setPlayerTurn(true);
                player.setPlayerTurnTime(new Date());
            } else {
                player.setPlayerTurn(false);
            }
        }
    }

    public int endRound(List<Player> playersInRound, Game game, Action action) {
        System.out.println("ending round");
        for (Player pl : game.getPlayers()) {
            pl.setPlayerWinner(false);
        }
        if (playersInRound.size() == 1) {
            playersInRound.get(0).setStackSize(playersInRound.get(0).getStackSize() + game.getPotSize());
            playersInRound.get(0).setPlayerWinner(true);
            game.setPotSize(0);
        } else {

            List<Player> winners = evaluateRound(playersInRound, game);
            for (Player pl : playersInRound) {
                pl.setPlayerWinner(winners.contains(pl));
            }
            for (Player winner : winners) {
                int amountToWin = 0;
                for (Player player : game.getPlayers()) {
                    amountToWin += player.getPlayerAmountInPot() > winner.getPlayerAmountInPot() ? winner.getPlayerBetAmount() : player.getPlayerAmountInPot();
                }
                winner.setStackSize(winner.getStackSize() + amountToWin);
                game.setPotSize(game.getPotSize() - amountToWin);
            }
            if (game.getPotSize() > 0) {
                List<Player> notWinners = new ArrayList<>();
                for (Player player : playersInRound) {
                    if (!winners.contains(player)) {
                        notWinners.add(player);
                    }
                }
                int amountToWin = game.getPotSize() / notWinners.size();
                for (Player notWinner : notWinners) {
                    notWinner.setStackSize(notWinner.getStackSize() + amountToWin);
                }
            }

        }
        int playersWithStack = 0;

        List<Player> players = new ArrayList<>();
        players.addAll(game.getPlayers());
        for (Player player : players) {
            player.setPlayerAmountInPot(0);
            if (player.getStackSize() == 0) {
                List<Action> actions = new ArrayList<>();
                actions.addAll(game.getActions());
                for(Action ac : actions){
                    if(ac.getPlayer().equals(player)){
                        game.getActions().remove(ac);
                    }
                }
                if(game.getBigBlind().equals(player)){
                    game.setBigBlind(null);
                }
                if(game.getSmallBlind().equals(player)){
                    game.setSmallBlind(null);
                }
                if(game.getDealer().equals(player)){
                    game.setDealer(null);
                }
                game = gameDAO.save(game);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                game.getPlayers().remove(player);
                int finishingPos = game.getPlayers().size() + 1;
                User user = userDAO.findByUserName(player.getUser().getUserName());
                if(null == user.getRankings()){
                    Rankings rank = new Rankings();
                    rank.setRankPlayedGames(1);
                    rank.setRankPoint(100 - (10 * finishingPos));
                    user.setRankings(rank);
                    userDAO.save(user);
                } else {
                    Rankings existingRank = user.getRankings();
                    existingRank.setRankPlayedGames(existingRank.getRankPlayedGames() + 1);
                    existingRank.setRankPoint(existingRank.getRankPoint() + (100 - (10 * finishingPos)));
                    userDAO.save(user);
                }
            } else {
                playersWithStack++;
            }

        }
        if(playersWithStack > 1){
            int dealerIndex = players.indexOf(game.getDealer());
            game.setDealer(dealerIndex == players.size() - 1 ? players.get(0) : players.get(dealerIndex + 1));
            setBlindPlayers(game);
        }

        return game.getPlayers().size();

    }

    public List<Action> getLastRoundActions(List<Action> actions, int actionRound) {
        List<Action> lastRoundActions = new ArrayList<>();
        for (Action action : actions) {
            if (action.getActionRound() == actionRound) {
                lastRoundActions.add(action);
            }
        }
        return lastRoundActions;
    }


    public boolean isBettingRoundOver(List<Player> playersInRound, Player actualPlayer, Player bigBlind, boolean preFlop) {
        for (Player player : playersInRound) {
            if (player.equals(actualPlayer)) {
                int nextPlayerIndex = playersInRound.indexOf(player) + 1;
                if (playersInRound.size() > nextPlayerIndex) {
                    Player nextPlayer = playersInRound.get(nextPlayerIndex);
                    if (nextPlayer.getPlayerRaiser()) {
                        if(nextPlayer.equals(bigBlind) && preFlop){
                            return false;
                        } else {
                            return true;
                        }

                    }
                } else {
                    if (!actualPlayer.equals(playersInRound.get(0)) &&
                            (playersInRound.get(0).getPlayerRaiser() || actualPlayer.equals(bigBlind))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void startNewRound(Game game) {

        int playersWithStack = 0;
        for(Player pl : game.getPlayers()){
            if(pl.getStackSize() > 0){
                playersWithStack++;
            }
        }

        if(playersWithStack < 2){
            return;
        }

        game.setFlop(null);
        game.setTurn(null);
        game.setRiver(null);

//        setOrder(game.getPlayers(),game.getSmallBlind(),game.getBigBlind(),true);

        List<Card> deck = cardDAO.findAll();
        Collections.shuffle(deck);

        List<Player> players = game.getPlayers();
        Player playerWithLowestOrder = players.get(0);
        for (Player player : players) {
            player.setCardOneLastRound(player.getCardOne());
            player.setCardOne(deck.get(0));
            deck.remove(0);
            player.setCardTwoLastRound(player.getCardTwo());
            player.setCardTwo(deck.get(0));
            deck.remove(0);
            if (player.getPlayerOrder() < playerWithLowestOrder.getPlayerOrder()) {
                playerWithLowestOrder = player;
            }
            player.setPlayerAmountInPot(0);
            player.setPlayerBetAmount(0);
            player.setPlayerTurn(false);
//            player.setPlayerInTurn(true);
        }
        playerWithLowestOrder.setPlayerTurn(true);
        if(null != game.getBigBlind()){
            game.getBigBlind().setStackSize(game.getBigBlind().getStackSize() - game.getBigBlindValue());
        }
        if(null != game.getSmallBlind()){
            game.getSmallBlind().setStackSize(game.getSmallBlind().getStackSize() - game.getSmallBlindValue());
        }



    }

    public void setRaiser(Player raiser, Game game) {
        raiser.setPlayerRaiser(true);
        for (Player notRaiser : game.getPlayers()) {
            if (!notRaiser.equals(raiser)) {
                notRaiser.setPlayerRaiser(false);
            }
        }
    }

    public Integer getPreviousBetAmount(Player player, Integer actionRound, List<Action> actions) {
        for (Action action : actions) {
            if (action.getActionRound() == actionRound && action.getPlayer().equals(player)) {
                if (action.getActionType().equals(ActionType.BET) || action.getActionType().equals(ActionType.RAISE)) {
                    return action.getBetValue();
                }
            }
        }
        return 0;
    }

    public void setBlindPlayers(Game game) {
        List<Player> players = game.getPlayers();
        int dealerIndex = players.indexOf(game.getDealer());
        Player smallBlind = dealerIndex == players.size() - 1 ? players.get(0) : players.get(dealerIndex + 1);
        int smallBlindIndex = players.indexOf(smallBlind);
        Player bigBlind = smallBlindIndex == players.size() - 1 ? players.get(0) : players.get(smallBlindIndex + 1);
        game.setBigBlind(bigBlind);
        game.setSmallBlind(smallBlind);
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
                player.setPlayerOrder(order);
                order++;
            }

            if (players.indexOf(player) > firstPlayerIndex) {
                player.setPlayerOrder(order);
                order++;
            }
        }
        for (Player player : players) {
            if (players.indexOf(player) < firstPlayerIndex) {
                player.setPlayerOrder(order);
                order++;
            }

        }

    }


    public List<Player> evaluateRound(List<Player> playersInRound, Game game) {
        List<Card> board = new ArrayList<>();
        board.addAll(game.getFlop());

        if (null != game.getTurn()) {
            board.add(game.getTurn());
        }
        if (null != game.getRiver()) {
            board.add(game.getRiver());
        }

        for (final Player player : playersInRound) {
            final EvaluatedHand evaluatedHand = HandEvaluator.evaluateHand(board, player.getCardOne(), player.getCardTwo());
            player.setEvaluatedHand(evaluatedHand);
        }

        EvaluatedHand bestHand = playersInRound.get(0).getEvaluatedHand();
        Player winner = playersInRound.get(0);
        List<Player> winners = new ArrayList<>();
        for (Player player : playersInRound) {
            if (player.equals(winner)) {
                continue;
            }
            if (player.getEvaluatedHand().getHandStrength().getStrength() > bestHand.getHandStrength().getStrength()
                    || (player.getEvaluatedHand().getHandStrength().getStrength().equals(bestHand.getHandStrength().getStrength()))
                    && player.getEvaluatedHand().getValue() > bestHand.getValue()) {
                bestHand = player.getEvaluatedHand();
                winner = player;
            }
        }
        for (Player player : playersInRound) {
            if (player.equals(winner)) {
                continue;
            }
            if (player.getEvaluatedHand().getHandStrength().getStrength().equals(bestHand.getHandStrength().getStrength())
                    && player.getEvaluatedHand().getValue().equals(player.getEvaluatedHand().getValue())) {
                if (null != player.getEvaluatedHand().getHighCards() && null != winner.getEvaluatedHand().getHighCards()) {
                    int equalCardNum = 0;
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
                        if (playerHighCard.getValue().equals(winnerHighCard.getValue())) {
                            equalCardNum++;
                        }
                    }
                    if (equalCardNum == player.getEvaluatedHand().getHighCards().size()) {
                        winners.add(player);
                    }
                }
            }
        }
        winners.add(winner);
        return winners;

    }
}
