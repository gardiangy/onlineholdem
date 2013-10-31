package hu.onlineholdem.restclient.entity;

import java.io.Serializable;
import java.util.List;


public class Game implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long gameId;

	private Integer playerNumber;

	private Integer potSize;

	private List<Player> players;

	public Game() {
	}

	public Long getGameId() {
		return this.gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public Integer getPlayerNumber() {
		return this.playerNumber;
	}

	public void setPlayerNumber(Integer playerNumber) {
		this.playerNumber = playerNumber;
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

}