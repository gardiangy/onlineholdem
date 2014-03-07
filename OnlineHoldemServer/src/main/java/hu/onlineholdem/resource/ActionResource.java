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

        Player player = playerDAO.findOne(actionBO.getPlayerId());

        ActionType actionType = ActionType.valueOf(actionBO.getActionType());
        Action action = new Action();
        action.setActionType(actionType);
        action.setBetValue(Integer.parseInt(actionBO.getBetValue()));
        action.setPlayer(player);

        Game game = gameDAO.findOne(actionBO.getGameId());
        action.setGame(game);

        int highestBetAmount = 0;

        if(game.getActions().size() == 0){
            action.setActionRound(1);
        } else {
            Action lastAction = game.getActions().get(game.getActions().size() - 1);
            if(lastAction.getPlayer().getPlayerOrder() == game.getPlayers().size()){
                List<Player> playersInRound = new ArrayList<>();
                for(Player pl : game.getPlayers()){
                    if(pl.getPlayerInTurn()){
                        playersInRound.add(pl);
                    }
                }
                highestBetAmount = getHighestBetAction(game.getActions(),lastAction.getActionRound()).getBetValue();
                if(makeMovesAgain(playersInRound,highestBetAmount)){
                    action.setActionRound(lastAction.getActionRound());
                } else {
                    if(null == game.getTurn()){
                        game.setTurn(getNextCard(game));
                    } else {
                        if(null == game.getRiver()){
                            game.setRiver(getNextCard(game));
                        }
                    }
                    action.setActionRound(lastAction.getActionRound() + 1);
                }

            } else {
                action.setActionRound(lastAction.getActionRound());
            }

        }

        game.getActions().add(action);

        for (Player pl : game.getPlayers()) {
            if (pl.getPlayerId().equals(player.getPlayerId())) {
                if (actionType.equals(ActionType.BET) || actionType.equals(ActionType.RAISE)) {
                    int newStackSize = pl.getStackSize() - action.getBetValue();
                    pl.setStackSize(newStackSize);
                    pl.setPlayerBetAmount(action.getBetValue());
                    pl.setPlayerAmountInPot(pl.getPlayerAmountInPot() + pl.getPlayerBetAmount());
                }
                if (actionType.equals(ActionType.CALL)) {
                    int newStackSize = highestBetAmount > pl.getStackSize() ? 0 : pl.getStackSize() - highestBetAmount;
                    pl.setStackSize(newStackSize);
                    pl.setPlayerBetAmount(highestBetAmount > pl.getStackSize() ? pl.getStackSize() : highestBetAmount);
                    pl.setPlayerAmountInPot(pl.getPlayerAmountInPot() + pl.getPlayerBetAmount());
                }
                if (actionType.equals(ActionType.ALL_IN)) {
                    pl.setPlayerBetAmount(pl.getStackSize());
                    pl.setStackSize(0);
                    pl.setPlayerAmountInPot(pl.getPlayerAmountInPot() + pl.getPlayerBetAmount());
                }
                pl.setPlayerTurn(false);

            } else {
                if (pl.getPlayerOrder() == player.getPlayerOrder() + 1 ||
                        (pl.getPlayerOrder() < player.getPlayerOrder() && pl.getPlayerInTurn() && player.getPlayerOrder() == game.getPlayers().size())) {
                    pl.setPlayerTurn(true);
                } else {
                    pl.setPlayerTurn(false);
                }
            }


        }

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

    public Action getHighestBetAction(List<Action> actions, int actionRound){
        Action higestBetAction = null;
        int higestBetAmount = 0;
        for(Action action : actions){
            if (action.getActionRound() == actionRound){
                    if(action.getBetValue() > higestBetAmount){
                        higestBetAmount = action.getBetValue();
                        higestBetAction = action;
                    }
            }
        }
        return higestBetAction;
    }

    public Card getNextCard(Game game){
        List<Card> deck = cardDAO.findAll();
        Collections.shuffle(deck);
        for(Card card : deck){
            if(!game.getFlop().contains(card)){
                if(null != game.getTurn() && !game.getTurn().equals(card)){

                    boolean playerHasThatCard = false;
                    for(Player pl : game.getPlayers()){
                        if(pl.getCardOne().equals(card) || pl.getCardTwo().equals(card)){
                            playerHasThatCard = true;
                        }
                    }
                    if(!playerHasThatCard){
                        return card;
                    }
                }
            }
        }
        return null;
    }


    public boolean makeMovesAgain(List<Player> players, Integer highestBetAmount) {
        for (Player player : players) {
            if ((null != player.getPlayerBetAmount() && player.getPlayerBetAmount() != highestBetAmount && player.getStackSize() > 0)) {
                return true;
            }
        }
        return false;
    }
}
