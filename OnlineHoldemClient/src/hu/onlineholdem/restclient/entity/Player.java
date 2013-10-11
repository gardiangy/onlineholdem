package hu.onlineholdem.restclient.entity;

import java.io.Serializable;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long playerId;
	private Integer stackSize;

	public Player() {
	}

	public Long getPlayerId() {
		return this.playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public Integer getStackSize() {
		return this.stackSize;
	}

	public void setStackSize(Integer stackSize) {
		this.stackSize = stackSize;
	}
}