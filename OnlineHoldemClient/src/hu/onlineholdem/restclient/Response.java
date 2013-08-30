package hu.onlineholdem.restclient;

public class Response {

    private ActionType actionType;
    private Integer betValue;


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
}
