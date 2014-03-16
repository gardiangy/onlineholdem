package hu.onlineholdem.restclient.entity;

import java.io.Serializable;

import hu.onlineholdem.restclient.enums.ActionType;


public class Action implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long actionId;
	private ActionType actionType;
	private Integer betValue;
	private Integer actionRound;
    private Player player;

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

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Action() {
	}


    public Integer getActionRound() {
        return actionRound;
    }

    public void setActionRound(Integer actionRound) {
        this.actionRound = actionRound;
    }

    @Override
    public String toString() {
        return "Action{" +
                "actionId=" + actionId +
                ", actionType=" + actionType +
                ", betValue=" + betValue +
                ", actionRound=" + actionRound +
                ", player=" + player +
                '}';
    }
}