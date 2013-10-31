package hu.onlineholdem.bo;

import com.sun.jersey.core.impl.provider.entity.XMLRootElementProvider;

import javax.xml.bind.annotation.XmlRootElement;

public class ActionBO {

    private String actionType;
    private String betValue;
    private Long playerId;
    private Long gameId;

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

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}