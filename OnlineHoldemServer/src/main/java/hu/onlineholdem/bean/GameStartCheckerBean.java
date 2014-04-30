package hu.onlineholdem.bean;

import hu.onlineholdem.dao.CardDAO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Card;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.enums.ActionType;
import hu.onlineholdem.enums.GameState;
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

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(1000);
                for(Game game : gameDAO.findAll()){
                    if(game.getStartTime().before(new Date()) && game.getGameState().equals(GameState.REGISTERING)
                            && game.getPlayers().size() > 1){
                        game.setGameState(GameState.STARTED);

                        List<Card> deck = cardDAO.findAll();
                        Collections.shuffle(deck);
                        List<Player> players = game.getPlayers();
                        Player playerWithLowestOrder = players.get(0);
                        for(Player player : players){
                            player.setCardOne(deck.get(0));
                            deck.remove(0);
                            player.setCardTwo(deck.get(0));
                            deck.remove(0);
                            if(player.getPlayerOrder() < playerWithLowestOrder.getPlayerOrder()){
                                playerWithLowestOrder = player;
                            }
                        }
                        playerWithLowestOrder.setPlayerTurn(true);
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

                        Action bigBlindAction = new Action();
                        bigBlindAction.setActionRound(1);
                        bigBlindAction.setBetValue(game.getBigBlindValue());
                        bigBlindAction.setGame(game);
                        bigBlindAction.setPlayer(game.getBigBlind());
                        bigBlindAction.setActionType(ActionType.BET);

                        game.getActions().add(smallBlindAction);
                        game.getActions().add(bigBlindAction);

                        gameDAO.save(game);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
