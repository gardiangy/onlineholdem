package hu.onlineholdem.bo;

public class Response {

    private String actionType;
    private Integer betValue;
    private Long playerId;
    private Long gameId;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
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

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
