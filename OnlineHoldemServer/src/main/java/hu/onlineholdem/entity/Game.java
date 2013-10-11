package hu.onlineholdem.entity;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;


/**
 * The persistent class for the game database table.
 * 
 */
@Entity
@Table(name="game")
public class Game implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="game_id", unique=true, nullable=false)
	private Long gameId;

	@Column(name="player_number")
	private Integer playerNumber;

	@Column(name="pot_size")
	private Integer potSize;

	//bi-directional many-to-one association to Action
    @JsonIgnore
	@OneToMany(mappedBy="game", cascade={CascadeType.ALL})
	private List<Action> actions;

	//bi-directional many-to-one association to Player
	@OneToMany(mappedBy="game", cascade={CascadeType.ALL})
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
    @JsonIgnore
	public List<Action> getActions() {
		return this.actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public List<Player> getPlayers() {
		return this.players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

}