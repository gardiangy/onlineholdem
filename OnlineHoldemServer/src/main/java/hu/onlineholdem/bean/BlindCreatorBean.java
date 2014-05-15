package hu.onlineholdem.bean;

import hu.onlineholdem.dao.ActionDAO;
import hu.onlineholdem.dao.CardDAO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Card;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.enums.ActionType;
import hu.onlineholdem.enums.GameState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BlindCreatorBean implements Runnable {

    @Autowired
    private GameDAO gameDAO;

    @Autowired
    private ActionDAO actionDAO;

    private Long gameId;
    private boolean addBlinds;

    public BlindCreatorBean() {
    }

    @Override
    public void run() {
        while (true) {
            try {

                Thread.sleep(500);
                if (addBlinds) {
                    Thread.sleep(3000);

                    Game game = gameDAO.findOne(gameId);
                    Action lastAction = game.getActions().get(game.getActions().size() - 1);

                    Action smallBlindAction = new Action();
                    smallBlindAction.setActionRound(lastAction.getActionRound() + 1);
                    smallBlindAction.setBetValue(game.getSmallBlindValue());
                    smallBlindAction.setGame(game);
                    smallBlindAction.setPlayer(game.getSmallBlind());
                    smallBlindAction.setActionType(ActionType.BET);
                    game.getSmallBlind().setPlayerAmountInPot(game.getSmallBlindValue());
                    game.getSmallBlind().setPlayerBetAmount(game.getSmallBlindValue());

                    Action bigBlindAction = new Action();
                    bigBlindAction.setActionRound(lastAction.getActionRound() + 1);
                    bigBlindAction.setBetValue(game.getBigBlindValue());
                    bigBlindAction.setGame(game);
                    bigBlindAction.setPlayer(game.getBigBlind());
                    bigBlindAction.setActionType(ActionType.BET);
                    game.getBigBlind().setPlayerAmountInPot(game.getBigBlindValue());
                    game.getBigBlind().setPlayerBetAmount(game.getBigBlindValue());
                    game.getBigBlind().setPlayerRaiser(true);
                    for(Player player : game.getPlayers()){
                        if(!player.equals(game.getBigBlind())){
                            player.setPlayerRaiser(false);
                        }
                    }

                    game.getActions().add(smallBlindAction);
                    game.setPotSize(game.getSmallBlindValue() + game.getBigBlindValue());

                    setOrder(game.getPlayers(),game.getBigBlind());

                    Player playerWithLowestOrder = game.getPlayers().get(0);
                    for (Player player : game.getPlayers()) {
                        if (player.getPlayerOrder() < playerWithLowestOrder.getPlayerOrder()) {
                            playerWithLowestOrder = player;
                        }
                        player.setPlayerTurn(false);
                        player.setPlayerInTurn(true);
                    }
                    playerWithLowestOrder.setPlayerTurn(true);
                    playerWithLowestOrder.setPlayerTurnTime(new Date());

                    Game persistedGame = gameDAO.save(game);
                    persistedGame.getActions().add(bigBlindAction);
                    gameDAO.save(persistedGame);
                    addBlinds = false;

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

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public boolean isAddBlinds() {
        return addBlinds;
    }

    public void setAddBlinds(boolean addBlinds) {
        this.addBlinds = addBlinds;
    }
}
