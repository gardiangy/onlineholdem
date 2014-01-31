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

	public Game() {
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
}