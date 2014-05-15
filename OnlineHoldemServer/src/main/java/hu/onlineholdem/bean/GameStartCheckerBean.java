package hu.onlineholdem.bean;

import hu.onlineholdem.bo.ActionBO;
import hu.onlineholdem.dao.CardDAO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Card;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.enums.ActionType;
import hu.onlineholdem.enums.GameState;
import hu.onlineholdem.resource.ActionResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GameStartCheckerBean implements Runnable{

    @Autowired
    private GameDAO gameDAO;

    @Autowired
    private CardDAO cardDAO;

    @Autowired
    private ActionResource actionResource;

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(2000);
                for(Game game : gameDAO.findAll()){
                    if(game.getGameState().equals(GameState.FINISHED)){
                        gameDAO.delete(game);
                    }
                    for(Player pl : game.getPlayers()){
                        if(pl.getPlayerTurn() && null != pl.getPlayerTurnTime()){
                            long currentTime = System.currentTimeMillis();
                            long diff = currentTime - pl.getPlayerTurnTime().getTime();
                            if(diff/1000 > 30){
                                ActionBO actionBO = new ActionBO();
                                boolean fold = false;
                                for(Player player : game.getPlayers()){
                                    if(player.getPlayerBetAmount() > game.getBigBlindValue()){
                                        actionBO.setActionType(ActionType.FOLD.name());
                                        fold = true;
                                    }
                                }
                                if(!fold){
                                    actionBO.setActionType(ActionType.CHECK.name());
                                }
                                actionBO.setBetValue("0");
                                actionBO.setGameId(game.getGameId());
                                actionBO.setPlayerId(pl.getPlayerId());

                                actionResource.postAction(actionBO);
                            }
                        }


                    }
                    if(game.getStartTime().before(new Date()) && game.getGameState().equals(GameState.REGISTERING)
                            && game.getPlayers().size() > 1){
                        game.setGameState(GameState.STARTED);

                        List<Card> deck = cardDAO.findAll();
                        Collections.shuffle(deck);
                        List<Player> players = game.getPlayers();
                        for(Player player : players){
                            player.setCardOne(deck.get(0));
                            deck.remove(0);
                            player.setCardTwo(deck.get(0));
                            deck.remove(0);

                        }

                        game.setDealer(players.get(0));
                        game.setSmallBlind(players.get(1));
                        game.setBigBlind(players.size() > 2 ? players.get(2) : players.get(0));
                        game.setSmallBlindValue(10);
                        game.setBigBlindValue(20);

                        Action smallBlindAction = new Action();
                        smallBlindAction.setActionRound(1);
                        smallBlindAction.setBetValue(game.getSmallBlindValue());
                        smallBlindAction.setGame(game);
                        smallBlindAction.setPlayer(game.getSmallBlind());
                        smallBlindAction.setActionType(ActionType.BET);
                        game.getSmallBlind().setPlayerAmountInPot(game.getSmallBlindValue());
                        game.getSmallBlind().setPlayerBetAmount(game.getSmallBlindValue());
                        game.getSmallBlind().setStackSize(game.getSmallBlind().getStackSize() - game.getSmallBlindValue());

                        Action bigBlindAction = new Action();
                        bigBlindAction.setActionRound(1);
                        bigBlindAction.setBetValue(game.getBigBlindValue());
                        bigBlindAction.setGame(game);
                        bigBlindAction.setPlayer(game.getBigBlind());
                        bigBlindAction.setActionType(ActionType.BET);
                        game.getBigBlind().setPlayerAmountInPot(game.getBigBlindValue());
                        game.getBigBlind().setPlayerBetAmount(game.getBigBlindValue());
                        game.getBigBlind().setStackSize(game.getBigBlind().getStackSize() - game.getBigBlindValue());
                        game.getBigBlind().setPlayerRaiser(true);


                        game.getActions().add(smallBlindAction);
                        game.setPotSize(game.getSmallBlindValue() + game.getBigBlindValue());

                        setOrder(game.getPlayers(),game.getBigBlind());

                        Player playerWithLowestOrder = players.get(0);
                        for(Player player : players){
                            if(player.getPlayerOrder() < playerWithLowestOrder.getPlayerOrder()){
                                playerWithLowestOrder = player;
                            }
                        }
                        playerWithLowestOrder.setPlayerTurn(true);
                        playerWithLowestOrder.setPlayerTurnTime(new Date());

                        Game persistedGame = gameDAO.save(game);
                        persistedGame.getActions().add(bigBlindAction);
                        gameDAO.save(persistedGame);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOrder(List<Player> players, Player bigBlind) {
        int order = 1;

        int bigBlindIndex = players.indexOf(bigBlind);
        int firstPlayerIndex = players.size() - 1 > bigBlindIndex ? bigBlindIndex + 1 : 0;

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
}
