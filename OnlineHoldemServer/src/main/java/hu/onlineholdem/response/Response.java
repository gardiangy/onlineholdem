package hu.onlineholdem.response;

import hu.onlineholdem.entity.Player;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

public class Response {

    private Integer potSize;
    private List<Player> players = new ArrayList<>();

    public Integer getPotSize() {
        return potSize;
    }

    public void setPotSize(Integer potSize) {
        this.potSize = potSize;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}
