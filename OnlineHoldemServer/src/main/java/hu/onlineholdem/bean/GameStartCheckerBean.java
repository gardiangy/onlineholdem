package hu.onlineholdem.bean;

import hu.onlineholdem.dao.CardDAO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.entity.Card;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
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


                        gameDAO.save(game);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
