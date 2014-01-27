package hu.onlineholdem.resource;


import hu.onlineholdem.bo.ActionBO;
import hu.onlineholdem.bo.CreateGameBO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.dao.UserDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.entity.User;
import hu.onlineholdem.enums.ActionType;
import hu.onlineholdem.enums.ResponseType;
import hu.onlineholdem.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/game")
@Component
public class GameResource {

    @Autowired
    private GameDAO gameDAO;
    @Autowired
    private UserDAO userDAO;
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

    @POST
    @Path("connect/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response connectToGame(@PathParam("userId") Long userId) {

        Game game = gameDAO.findOne(4l);
        User user = userDAO.findOne(userId);

        Player player = new Player();
        player.setUser(user);
        player.setPlayerOrder(0);
        player.setPlayerTurn(false);
        player.setStackSize(0);
        player.setGame(game);

        if(null == game.getPlayers()){
            game.setPlayers(new ArrayList<Player>());
        }
        game.getPlayers().add(player);
        Game updatedGame = gameDAO.save(game);

        Response response = new Response();
        response.setResponseObject(updatedGame);
        response.setResponseType(ResponseType.OK);

        return response;
    }

    @POST
    @Path("disconnect/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disconnectFromGame(@PathParam("userId") Long userId) {

        Game game = gameDAO.findOne(4l);
        User user = userDAO.findOne(userId);

        List<Player> players = new ArrayList<>();
        players.addAll(game.getPlayers());

        for(Player player : players){
            if(player.getUser().equals(user)){
                game.getPlayers().remove(player);
            }
        }
        Game updatedGame = gameDAO.save(game);

        Response response = new Response();
        response.setResponseObject(updatedGame);
        response.setResponseType(ResponseType.OK);

        return response;
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGame(CreateGameBO createGameBO) {

        Game game = new Game();
        game.setGameName(createGameBO.getGameName());
        game.setMaxPlayerNumber(createGameBO.getMaxPlayerNumber());
        game.setStartingStackSize(createGameBO.getStartingStackSize());
        game.setPotSize(0);

        Game persistedGame = gameDAO.save(game);

        Response response = new Response();
        response.setResponseObject(persistedGame);
        response.setResponseType(ResponseType.OK);


        return response;
    }

    @GET
    @Path("{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGame(@PathParam("gameId") Long gameId) {

        Game game = gameDAO.findOne(gameId);

        Response response = new Response();
        response.setResponseObject(game);
        response.setResponseType(ResponseType.OK);

        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGames() {

        List<Game> gameList = gameDAO.findAll();

        Response response = new Response();
        response.setResponseObject(gameList);
        response.setResponseType(ResponseType.OK);

        return response;
    }
}
