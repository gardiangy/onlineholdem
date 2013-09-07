package hu.onlineholdem.bo;

import com.sun.jersey.core.impl.provider.entity.XMLRootElementProvider;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Response {

    private String actionType;
    private String betValue;
    private String playerId;
    private String gameId;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getBetValue() {
        return betValue;
    }

    public void setBetValue(String betValue) {
        this.betValue = betValue;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
