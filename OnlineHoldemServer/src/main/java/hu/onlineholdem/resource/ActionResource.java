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
        game.getActions().add(action);

        for (Player pl : game.getPlayers()) {
            if (pl.getPlayerId().equals(player.getPlayerId())) {
                if (actionType.equals(ActionType.FOLD)) {
                    pl.setPlayerInTurn(false);
                }
                int newStackSize = pl.getStackSize() - action.getBetValue();
                pl.setStackSize(newStackSize);
                pl.setPlayerTurn(false);
                pl.setPlayerBetAmount(action.getBetValue());
            } else {
                if (pl.getPlayerOrder() == player.getPlayerOrder() + 1) {
                    pl.setPlayerTurn(true);
                }
            }
            if (game.getPlayers().size() == game.getPlayers().indexOf(pl) + 1) {
                game.getPlayers().get(0).setPlayerTurn(true);
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
}
