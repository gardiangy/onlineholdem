package hu.onlineholdem.resource;

import hu.onlineholdem.bo.ActionBO;
import hu.onlineholdem.dao.ActionDAO;
import hu.onlineholdem.dao.CardDAO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Card;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.enums.ActionType;
import hu.onlineholdem.enums.ResponseType;
import hu.onlineholdem.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
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


        int highestBetAmount = 0;
        Action lastAction = null;

        if (game.getActions().size() > 0) {
            lastAction = game.getActions().get(game.getActions().size() - 1);
            highestBetAmount = getHighestBetAction(game.getActions(), lastAction.getActionRound()).getBetValue();
            if (action.getBetValue() > highestBetAmount) {
                highestBetAmount = action.getBetValue();
            }
        }

        List<Player> playersInRound = new ArrayList<>();
        for (Player pl : game.getPlayers()) {
            if (pl.getPlayerInTurn()) {
                playersInRound.add(pl);
            }
        }

        for (Player pl : game.getPlayers()) {
            if (pl.getPlayerInTurn()) {
                if (pl.getPlayerId().equals(player.getPlayerId())) {
                    if (action.getBetValue() > highestBetAmount) {
                        pl.setPlayerRaiser(true);
                        for (Player notRaiser : game.getPlayers()) {
                            if (!notRaiser.equals(pl)) {
                                notRaiser.setPlayerRaiser(false);
                            }
                        }
                    }
                    if (actionType.equals(ActionType.BET) || actionType.equals(ActionType.RAISE)) {
                        int newStackSize = pl.getStackSize() - action.getBetValue();
                        pl.setStackSize(newStackSize);
                        pl.setPlayerBetAmount(action.getBetValue());
                        pl.setPlayerAmountInPot(null == pl.getPlayerAmountInPot() ? pl.getPlayerBetAmount() : pl.getPlayerAmountInPot() + pl.getPlayerBetAmount());
                    }
                    if (actionType.equals(ActionType.FOLD)) {
                        pl.setPlayerInTurn(false);
                    }
                    if (actionType.equals(ActionType.CALL)) {
                        int amountToCall = highestBetAmount - pl.getPlayerBetAmount();
                        int newStackSize = amountToCall > pl.getStackSize() ? 0 : pl.getStackSize() - amountToCall;
                        pl.setStackSize(newStackSize);
                        pl.setPlayerBetAmount(highestBetAmount > pl.getStackSize() ? pl.getStackSize() : highestBetAmount);
                        action.setBetValue(highestBetAmount);
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
                    pl.setPlayerTurn(false);

                } else {
                    if (isBettingRoundOver(playersInRound, player)) {
                        for (Player nextPlayer : game.getPlayers()) {
                            if (nextPlayer.equals(playersInRound.get(0))) {
                                nextPlayer.setPlayerTurn(true);
                            }
                        }
                    } else {
                        if (playersInRound.indexOf(pl) == playersInRound.indexOf(player) + 1) {
                            pl.setPlayerTurn(true);
                        }
                    }

                }
            }


        }

        if (game.getActions().size() == 0) {
            action.setActionRound(1);
        } else {

            if(!makeMovesAgain(playersInRound, getLastRoundActions(game.getActions(), lastAction.getActionRound()), highestBetAmount)){
                if (player.equals(playersInRound.get(0))){
                    action.setActionRound(lastAction.getActionRound() + 1);
                } else {
                    action.setActionRound(lastAction.getActionRound());
                }

                if (isBettingRoundOver(playersInRound, player)) {
                    endBettingRound(game);
                }
            } else {
                action.setActionRound(lastAction.getActionRound());
            }

        }

        game.getActions().add(action);

        int newPotSize = game.getPotSize() + action.getBetValue();
        game.setPotSize(newPotSize);


        Game persistedGame = gameDAO.save(game);

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


    public boolean makeMovesAgain(List<Player> players, List<Action> lastRoundActions, int highestBetAmount) {
        for (Player player : players) {
            for (Action action : lastRoundActions) {
                if (action.getPlayer().getPlayerId().equals(player.getPlayerId())) {
                    if ((null != player.getPlayerBetAmount() && action.getBetValue() != highestBetAmount && player.getStackSize() > 0)) {
                        return true;
                    }
                }
            }

        }
        return false;
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


    public void endBettingRound(Game game) {
        if (null == game.getTurn()) {
            game.setTurn(getNextCard(game));
        } else {
            if (null == game.getRiver()) {
                game.setRiver(getNextCard(game));
            } else {
                startNewRound(game);
            }
        }
        for (Player pl : game.getPlayers()) {
            pl.setPlayerBetAmount(0);
        }
    }

    public boolean isBettingRoundOver(List<Player> playersInRound, Player actualPlayer) {
        for (Player player : playersInRound) {
            if (player.equals(actualPlayer)) {
                if (playersInRound.size() > playersInRound.indexOf(player) + 1) {
                    if (playersInRound.get(playersInRound.indexOf(player) + 1).getPlayerRaiser()) {
                        return true;
                    }
                } else {
                    if (!actualPlayer.equals(playersInRound.get(0)) && playersInRound.get(0).getPlayerRaiser()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void startNewRound(Game game) {
        game.setTurn(null);
        game.setRiver(null);

        List<Card> deck = cardDAO.findAll();
        Collections.shuffle(deck);

        game.setFlop(new ArrayList<Card>());
        game.getFlop().add(deck.get(0));
        game.getFlop().add(deck.get(1));
        game.getFlop().add(deck.get(2));

        List<Player> players = game.getPlayers();
        Player playerWithLowestOrder = players.get(0);
        for (Player player : players) {
            player.setCardOne(deck.get(3));
            deck.remove(3);
            player.setCardTwo(deck.get(3));
            deck.remove(3);
            if (player.getPlayerOrder() < playerWithLowestOrder.getPlayerOrder()) {
                playerWithLowestOrder = player;
            }
            player.setPlayerAmountInPot(0);
        }
        playerWithLowestOrder.setPlayerTurn(true);

        game.setPotSize(0);
    }
}
