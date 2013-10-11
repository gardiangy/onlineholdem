package hu.onlineholdem.resource;


import hu.onlineholdem.bo.ActionBO;
import hu.onlineholdem.dao.ActionDAO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.enums.ActionType;
import hu.onlineholdem.enums.ResponseType;
import hu.onlineholdem.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/game")
@Component
public class GameResource {

    @Autowired
    private GameDAO gameDAO;
    @Autowired
    private ActionDAO actionDAO;
    @Autowired
    private PlayerDAO playerDAO;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAction(ActionBO actionBO) {

        Player player = playerDAO.findOne(actionBO.getPlayerId());

        ActionType actionType = ActionType.valueOf(actionBO.getActionType());

        Action action = new Action();
        action.setActionType(actionType);
        action.setBetValue(Integer.parseInt(actionBO.getBetValue()));

        int newStackSize = player.getStackSize() - action.getBetValue();
        player.setStackSize(newStackSize);

        action.setPlayer(player);

        Game game = gameDAO.findOne(actionBO.getGameId());
        action.setGame(game);
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGameData() {

        Game game = gameDAO.findOne(1l);

        Response response = new Response();
        response.setResponseObject(game);
        response.setResponseType(ResponseType.OK);

        return response;
    }
}
