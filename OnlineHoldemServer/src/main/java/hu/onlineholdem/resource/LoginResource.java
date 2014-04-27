package hu.onlineholdem.resource;


import hu.onlineholdem.bo.LoginBO;
import hu.onlineholdem.bo.RegisterBO;
import hu.onlineholdem.dao.UserDAO;
import hu.onlineholdem.entity.User;
import hu.onlineholdem.enums.ResponseType;
import hu.onlineholdem.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/login")
@Component
public class LoginResource {

    @Autowired
    private UserDAO userDAO;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAction(LoginBO loginBO) {

        User user = userDAO.findByUserName(loginBO.getUserName());
        if (null == user) {
            Response response = new Response();
            response.setResponseType(ResponseType.ERROR);
            response.setErrorMessage("No user found with this username");
            return response;

        }
        if (!user.getUserPassword().equals(loginBO.getUserPassword())) {
            Response response = new Response();
            response.setResponseType(ResponseType.ERROR);
            response.setErrorMessage("Incorrect password");
            return response;
        }
        Response response = new Response();
        response.setResponseObject(user);
        response.setResponseType(ResponseType.OK);

        return response;
    }


    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(RegisterBO registerBO) {

        User existingUser = userDAO.findByUserName(registerBO.getUserName());

        if(null != existingUser){
            Response response = new Response();
            response.setResponseObject("Another user exists with this username");
            response.setResponseType(ResponseType.ERROR);
            return response;
        }

        User user = new User();
        user.setUserName(registerBO.getUserName());
        user.setUserEmail(registerBO.getUserEmail());
        user.setUserPassword(registerBO.getUserPassword());

        User persistedUser = userDAO.save(user);

        Response response = new Response();
        response.setResponseObject(persistedUser);
        response.setResponseType(ResponseType.OK);

        return response;
    }

}
