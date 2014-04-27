package hu.onlineholdem.resource;


import hu.onlineholdem.bo.CreateGameBO;
import hu.onlineholdem.bo.JoinLeaveBO;
import hu.onlineholdem.dao.GameDAO;
import hu.onlineholdem.dao.PlayerDAO;
import hu.onlineholdem.dao.RankingsDAO;
import hu.onlineholdem.dao.UserDAO;
import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import hu.onlineholdem.entity.Rankings;
import hu.onlineholdem.entity.User;
import hu.onlineholdem.enums.GameState;
import hu.onlineholdem.enums.ResponseType;
import hu.onlineholdem.response.DatatableAjaxResponse;
import hu.onlineholdem.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Path("/rankings")
@Component
public class RankingsResource {

    @Autowired
    private RankingsDAO rankingsDAO;


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
        Page<Rankings> rankingsPage = rankingsDAO.findByUserUserNameContainingOrderByRankPointDesc(sSearch,pageRequest);

        DatatableAjaxResponse datatableAjaxResponse = new DatatableAjaxResponse();
        datatableAjaxResponse.setiTotalRecords(rankingsDAO.count());
        datatableAjaxResponse.setiTotalDisplayRecords(rankingsPage.getTotalElements());
        datatableAjaxResponse.setsEcho(sEcho);

        int rowCount = 1;
        for (Rankings rankings : rankingsPage) {
            List<String> row = new ArrayList<>(5);
            datatableAjaxResponse.getAaData().add(row);
            row.add(rankings.getRankId().toString());
            row.add("#" + (rowCount + iDisplayStart) + "");
            row.add(rankings.getRankPoint().toString());
            row.add(rankings.getUser().getUserName());
            row.add(rankings.getRankPlayedGames().toString());
            rowCount++;
        }

        return datatableAjaxResponse;
    }

}
