package hu.onlineholdem.resource;


import hu.onlineholdem.bo.Response;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.enums.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/game")
@Component
public class GameResource {

    @Autowired
    private GameDAO gameDAO;
    @Autowired
    private PlayerDAO playerDAO;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void postAction(Response response) {

        Player player = playerDAO.findOne(Long.parseLong(response.getPlayerId()));

        ActionType actionType = ActionType.valueOf(response.getActionType());

        Action action = new Action();
        action.setActionType(actionType);
        action.setBetValue(Integer.parseInt(response.getBetValue()));
        action.setPlayer(player);

        Game game = gameDAO.findOne(Long.parseLong(response.getGameId()));
        action.setGame(game);
        game.getActions().add(action);


        gameDAO.save(game);

    }
}
