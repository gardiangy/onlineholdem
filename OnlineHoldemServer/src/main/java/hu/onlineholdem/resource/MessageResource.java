package hu.onlineholdem.resource;


import hu.onlineholdem.dao.BaseDAO;
import hu.onlineholdem.dao.MessageDAO;
import hu.onlineholdem.entity.Message;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/message")
public class MessageResource {

    private MessageDAO messageDAO = new MessageDAO();

    @GET
    @Path("messages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Message> getMessages() {
        List<Message> messages = messageDAO.getAll();
        return messages;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void postMessage(Message message) {


        System.out.println(message.getValue());

        messageDAO.save(message);

    }
}
