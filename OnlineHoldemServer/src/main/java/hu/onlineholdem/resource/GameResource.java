package hu.onlineholdem.resource;


import hu.onlineholdem.bo.Response;
import hu.onlineholdem.dao.ActionDAO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.MessageDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Message;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.enums.ActionType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/game")
public class GameResource {

    private GameDAO gameDAO = new GameDAO();
    private PlayerDAO playerDAO = new PlayerDAO();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void postAction(Response response) {

        Player player = playerDAO.findOne(response.getPlayerId());

        ActionType actionType = ActionType.valueOf(response.getActionType());

        Action action = new Action();
        action.setActionType(actionType);
        action.setBetValue(response.getBetValue());
        action.setPlayer(player);

        Game game = gameDAO.findOne(response.getGameId());
        game.getActions().add(action);


        gameDAO.save(game);

    }
}
