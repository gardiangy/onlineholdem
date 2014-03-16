package hu.onlineholdem.entity;

import hu.onlineholdem.enums.ActionType;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * The persistent class for the action database table.
 * 
 */
@Entity
@Table(name="action")
public class Action implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="action_id", unique=true, nullable=false)
	private Long actionId;

	@Column(name="action_type", nullable=false, length=255)
    @Enumerated(EnumType.STRING)
	private ActionType actionType;

	@Column(name="bet_value")
	private Integer betValue;


    @Column(name="action_round", nullable=false)
    private Integer actionRound;

	//bi-directional many-to-one association to Game
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="game_id", nullable=false)
	private Game game;

    //bi-directional many-to-one association to Player
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="player_id", nullable=false)
    private Player player;

	public Action() {
	}

	public Long getActionId() {
		return this.actionId;
	}

	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}

	public ActionType getActionType() {
		return this.actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public Integer getBetValue() {
		return this.betValue;
	}

	public void setBetValue(Integer betValue) {
		this.betValue = betValue;
	}

    @JsonIgnore
	public Game getGame() {
		return this.game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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
                ", player=" + player.getPlayerId() +
                '}';
    }
}