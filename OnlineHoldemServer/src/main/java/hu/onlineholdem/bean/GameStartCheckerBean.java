package hu.onlineholdem.bean;

import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.enums.GameState;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class GameStartCheckerBean implements Runnable{

    @Autowired
    private GameDAO gameDAO;

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(1000);
                for(Game game : gameDAO.findAll()){
                    if(game.getStartTime().before(new Date()) && game.getGameState().equals(GameState.REGISTERING)){
                        game.setGameState(GameState.STARTED);
                        gameDAO.save(game);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
