package hu.onlineholdem.entity;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * The persistent class for the player database table.
 * 
 */
@Entity
@Table(name="player")
public class Player implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="player_id", unique=true, nullable=false)
	private Long playerId;

	@Column(name="stack_size", nullable=false)
	private Integer stackSize;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id", nullable=false)
	private User user;

	//bi-directional many-to-one association to Game
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="game_id", nullable=false)
	private Game game;

    //bi-directional many-to-one association to Action

    @OneToMany(mappedBy="player", cascade={CascadeType.ALL})
    private List<Action> actions;

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

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Game getGame() {
		return this.game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

    @JsonIgnore
    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}