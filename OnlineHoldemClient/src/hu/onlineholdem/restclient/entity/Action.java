package hu.onlineholdem.restclient.entity;

import java.io.Serializable;

import hu.onlineholdem.restclient.enums.ActionType;


public class Action implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long actionId;
	private ActionType actionType;
	private Integer betValue;
    private Long playerId;

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getBetValue() {
        return betValue;
    }

    public void setBetValue(Integer betValue) {
        this.betValue = betValue;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Action() {
	}

}