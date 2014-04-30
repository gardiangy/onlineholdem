package hu.onlineholdem.entity;

import hu.onlineholdem.util.EvaluatedHand;
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

    @Column(name="player_order", nullable=false)
    private Integer playerOrder;

    @Column(name="player_position", nullable=false)
    private Integer playerPosition;

    @Column(name="player_turn", nullable=false)
    private Boolean playerTurn;

    @Column(name="player_raiser", nullable=false)
    private Boolean playerRaiser;

    @Column(name="player_in_turn", nullable=false)
    private Boolean playerInTurn;

    @Column(name="player_amount_in_pot")
    private Integer playerAmountInPot;

    @Column(name="player_bet_amount")
    private Integer playerBetAmount;

    @Column(name="player_winner", nullable=false)
    private Boolean playerWinner;

    @Column(name="player_amount_to_win")
    private Integer playerAmountToWin;

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

    @ManyToOne
    @JoinColumn(name="player_card_one", nullable=false)
    private Card cardOne;

    @ManyToOne
    @JoinColumn(name="player_card_two", nullable=false)
    private Card cardTwo;

    @Transient
    private EvaluatedHand evaluatedHand;

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

    public Integer getPlayerOrder() {
        return playerOrder;
    }

    public void setPlayerOrder(Integer playerOrder) {
        this.playerOrder = playerOrder;
    }

    public Boolean getPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(Boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

    @JsonIgnore
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

    public Boolean getPlayerInTurn() {
        return playerInTurn;
    }

    public void setPlayerInTurn(Boolean playerInTurn) {
        this.playerInTurn = playerInTurn;
    }

    public Boolean getPlayerRaiser() {
        return playerRaiser;
    }

    public void setPlayerRaiser(Boolean playerRaiser) {
        this.playerRaiser = playerRaiser;
    }

    public Card getCardOne() {
        return cardOne;
    }

    public void setCardOne(Card cardOne) {
        this.cardOne = cardOne;
    }

    public Card getCardTwo() {
        return cardTwo;
    }

    public void setCardTwo(Card cardTwo) {
        this.cardTwo = cardTwo;
    }

    public Integer getPlayerAmountInPot() {
        return playerAmountInPot;
    }

    public void setPlayerAmountInPot(Integer playerAmountInPot) {
        this.playerAmountInPot = playerAmountInPot;
    }

    public Integer getPlayerBetAmount() {
        return playerBetAmount;
    }

    public void setPlayerBetAmount(Integer playerBetAmount) {
        this.playerBetAmount = playerBetAmount;
    }

    public EvaluatedHand getEvaluatedHand() {
        return evaluatedHand;
    }

    public void setEvaluatedHand(EvaluatedHand evaluatedHand) {
        this.evaluatedHand = evaluatedHand;
    }

    public Boolean getPlayerWinner() {
        return playerWinner;
    }

    public void setPlayerWinner(Boolean playerWinner) {
        this.playerWinner = playerWinner;
    }

    public Integer getPlayerAmountToWin() {
        return playerAmountToWin;
    }

    public void setPlayerAmountToWin(Integer playerAmountToWin) {
        this.playerAmountToWin = playerAmountToWin;
    }

    public Integer getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(Integer playerPosition) {
        this.playerPosition = playerPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (playerId != null ? !playerId.equals(player.playerId) : player.playerId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return playerId != null ? playerId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Player{" +
                "playerId=" + playerId +
                ", stackSize=" + stackSize +
                ", playerOrder=" + playerOrder +
                ", playerTurn=" + playerTurn +
                ", playerInTurn=" + playerInTurn +
                ", playerAmountInPot=" + playerAmountInPot +
                ", playerBetAmount=" + playerBetAmount +
                '}';
    }
}