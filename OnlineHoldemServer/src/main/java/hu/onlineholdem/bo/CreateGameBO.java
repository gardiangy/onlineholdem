package hu.onlineholdem.bo;

public class CreateGameBO {

    private String gameName;
    private Integer maxPlayerNumber;
    private Integer startingStackSize;
    private String startTime;

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Integer getMaxPlayerNumber() {
        return maxPlayerNumber;
    }

    public void setMaxPlayerNumber(Integer maxPlayerNumber) {
        this.maxPlayerNumber = maxPlayerNumber;
    }

    public Integer getStartingStackSize() {
        return startingStackSize;
    }

    public void setStartingStackSize(Integer startingStackSize) {
        this.startingStackSize = startingStackSize;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
