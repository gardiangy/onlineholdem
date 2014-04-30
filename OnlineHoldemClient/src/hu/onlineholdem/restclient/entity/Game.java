package hu.onlineholdem.restclient.entity;

import android.widget.RelativeLayout;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import hu.onlineholdem.restclient.enums.GameState;


public class Game implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long gameId;

    private String gameName;

	private Integer maxPlayerNumber;

    private Integer startingStackSize;

	private Integer potSize;

    private Date startTime;

	private List<Player> players;

    private List<Card> board;

    private List<RelativeLayout> potChips;

    private GameState gameState;

    private List<Action> actions;

    private Player dealer;

    private Player smallBlind;

    private Player bigBlind;

    private Integer smallBlindValue;

    private Integer bigBlindValue;

	public Game() {
	}

    public Player getUser(){
        for(Player player : players){
            if(player.isUser()){
                return player;
            }
        }
        return null;
    }

	public Long getGameId() {
		return this.gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Integer getMaxPlayerNumber() {
        return maxPlayerNumber;
    }

    public Integer getStartingStackSize() {
        return startingStackSize;
    }

    public void setStartingStackSize(Integer startingStackSize) {
        this.startingStackSize = startingStackSize;
    }

    public void setMaxPlayerNumber(Integer maxPlayerNumber) {
        this.maxPlayerNumber = maxPlayerNumber;
    }

    public Integer getPotSize() {
		return this.potSize;
	}

	public void setPotSize(Integer potSize) {
		this.potSize = potSize;
	}

	public List<Player> getPlayers() {
		return this.players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

    public List<Card> getBoard() {
        return board;
    }

    public void setBoard(List<Card> board) {
        this.board = board;
    }

    public List<RelativeLayout> getPotChips() {
        return potChips;
    }

    public void setPotChips(List<RelativeLayout> potChips) {
        this.potChips = potChips;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public Player getDealer() {
        return dealer;
    }

    public void setDealer(Player dealer) {
        this.dealer = dealer;
    }

    public Player getBigBlind() {
        return bigBlind;
    }

    public void setBigBlind(Player bigBlind) {
        this.bigBlind = bigBlind;
    }

    public Player getSmallBlind() {
        return smallBlind;
    }

    public void setSmallBlind(Player smallBlind) {
        this.smallBlind = smallBlind;
    }

    public Integer getSmallBlindValue() {
        return smallBlindValue;
    }

    public void setSmallBlindValue(Integer smallBlindValue) {
        this.smallBlindValue = smallBlindValue;
    }

    public Integer getBigBlindValue() {
        return bigBlindValue;
    }

    public void setBigBlindValue(Integer bigBlindValue) {
        this.bigBlindValue = bigBlindValue;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameId=" + gameId +
                ", gameName='" + gameName + '\'' +
                ", maxPlayerNumber=" + maxPlayerNumber +
                ", startingStackSize=" + startingStackSize +
                ", potSize=" + potSize +
                ", startTime=" + startTime +
                ", players=" + players +
                ", board=" + board +
                ", potChips=" + potChips +
                ", gameState=" + gameState +
                ", actions=" + actions +
                '}';
    }
}