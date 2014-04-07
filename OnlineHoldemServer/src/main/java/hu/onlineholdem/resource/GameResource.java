package hu.onlineholdem.resource;


import hu.onlineholdem.bo.ActionBO;
import hu.onlineholdem.bo.CreateGameBO;
import hu.onlineholdem.bo.JoinLeaveBO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.dao.UserDAO;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.entity.User;
import hu.onlineholdem.enums.ActionType;
import hu.onlineholdem.enums.GameState;
import hu.onlineholdem.enums.ResponseType;
import hu.onlineholdem.response.DatatableAjaxResponse;
import hu.onlineholdem.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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


    @GET
    @Path("table")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getTableData(@QueryParam("sEcho") String sEcho, @QueryParam("sSearch")  String sSearch,
                               @QueryParam("iDisplayLength")  Integer iDisplayLength, @QueryParam("iDisplayStart") Integer iDisplayStart) {
        if (sSearch.contains(",") && sSearch.length() > 1)
            sSearch = sSearch.split(",")[0];
        if (sSearch.equals(","))
            sSearch = "";


        PageRequest pageRequest = new PageRequest(iDisplayStart / iDisplayLength, iDisplayLength);
        Page<Game> gamesPage = gameDAO.findByGameNameContaining(sSearch,pageRequest);

        DatatableAjaxResponse datatableAjaxResponse = new DatatableAjaxResponse();
        datatableAjaxResponse.setiTotalRecords(gameDAO.count());
        datatableAjaxResponse.setiTotalDisplayRecords(gamesPage.getTotalElements());
        datatableAjaxResponse.setsEcho(sEcho);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd   HH:mm");

        for (Game game : gamesPage) {
            List<String> row = new ArrayList<>(5);
            datatableAjaxResponse.getAaData().add(row);
            row.add(game.getGameId().toString());
            row.add(game.getGameName());
            row.add(game.getMaxPlayerNumber().toString());
            row.add(sdf.format(game.getStartTime()));
            row.add(game.getGameState().toString());
        }

        return datatableAjaxResponse;
    }



    @POST
    @Path("join")
    @Produces(MediaType.APPLICATION_JSON)
    public Response joinToGame(JoinLeaveBO joinLeaveBO) {

        Game game = gameDAO.findOne(joinLeaveBO.getGameId());
        User user = userDAO.findOne(joinLeaveBO.getUserId());

        Player player = new Player();
        player.setUser(user);
        List<Player> players = game.getPlayers();
        player.setPlayerTurn(false);
        player.setStackSize(game.getStartingStackSize());
        player.setPlayerInTurn(true);
        player.setPlayerRaiser(false);
        player.setPlayerWinner(false);
        player.setPlayerAmountInPot(0);
        player.setPlayerBetAmount(0);
        player.setPlayerAmountToWin(0);
        player.setGame(game);

        if(null == game.getPlayers()){
            game.setPlayers(new ArrayList<Player>());
        }
        if(game.getPlayers().size() == 0){
            player.setPlayerOrder(1);
        } else {
            List<Player> playersHighestOrder = playerDAO.findByGameOrderByPlayerOrderDesc(game);
            player.setPlayerOrder(playersHighestOrder.get(0).getPlayerOrder() + 1);
        }

        game.getPlayers().add(player);
        Game updatedGame = gameDAO.save(game);

        Response response = new Response();
        response.setResponseObject(updatedGame);
        response.setResponseType(ResponseType.OK);

        return response;
    }

    @POST
    @Path("leave")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disconnectFromGame(JoinLeaveBO joinLeaveBO) {

        Game game = gameDAO.findOne(joinLeaveBO.getGameId());
        User user = userDAO.findOne(joinLeaveBO.getUserId());

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

        Game existingGame = gameDAO.findByGameName(createGameBO.getGameName());

        if(null != existingGame){
            Response response = new Response();
            response.setResponseObject("game exists with this name");
            response.setResponseType(ResponseType.ERROR);
            return response;
        }

        Game game = new Game();
        game.setGameName(createGameBO.getGameName());
        game.setMaxPlayerNumber(createGameBO.getMaxPlayerNumber());
        game.setStartingStackSize(createGameBO.getStartingStackSize());
        game.setPotSize(0);
        game.setGameState(GameState.REGISTERING);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            game.setStartTime(sdf.parse(createGameBO.getStartTime()));
        } catch (ParseException e) {
            game.setStartTime(new Date());
        }

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

//        if (null != game.getActions() && game.getActions().size() > 0){
//            int lastActionRound = game.getActions().get(game.getActions().size() - 1).getActionRound();
//            List<Action> actions = new ArrayList<>();
//            actions.addAll(game.getActions());
//            for(Action action : actions){
//                if(action.getActionRound() != lastActionRound){
//                    game.getActions().remove(action);
//                }
//            }
//        }

        Response response = new Response();
        response.setResponseObject(game);
        response.setResponseType(ResponseType.OK);

        return response;
    }

    @GET
    @Path("contains/{gameName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGamesContainingName(@PathParam("gameName") String gameName) {

        List<Game> gameList = gameDAO.findByGameNameContaining(gameName);

        Response response = new Response();
        response.setResponseObject(gameList);
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
