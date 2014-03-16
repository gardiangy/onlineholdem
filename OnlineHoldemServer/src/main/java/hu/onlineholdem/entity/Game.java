package hu.onlineholdem.entity;

import hu.onlineholdem.enums.GameState;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the game database table.
 */
@Entity
@Table(name = "game")
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id", unique = true, nullable = false)
    private Long gameId;

    @Column(name = "game_name")
    private String gameName;

    @Column(name = "game_max_player_number")
    private Integer maxPlayerNumber;

    @Column(name = "game_starting_stack_size")
    private Integer startingStackSize;

    @Column(name = "game_pot_size")
    private Integer potSize;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "game_start_time")
    private Date startTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_state")
    private GameState gameState;

    //bi-directional many-to-one association to Action
    @OneToMany(mappedBy = "game", cascade = {CascadeType.ALL})
    private List<Action> actions;

    //bi-directional many-to-one association to Player
    @OneToMany(mappedBy = "game", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Player> players;

    @ManyToMany
    @JoinTable(name = "join_card_to_game",
            joinColumns = {@JoinColumn(name = "game_id") },
            inverseJoinColumns = {@JoinColumn(name = "card_id")})
    private List<Card> flop;

    @ManyToOne
    @JoinColumn(name="game_turn", nullable=false)
    private Card turn;

    @ManyToOne
    @JoinColumn(name="game_river", nullable=false)
    private Card river;

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

    public Integer getPotSize() {
        return this.potSize;
    }

    public void setPotSize(Integer potSize) {
        this.potSize = potSize;
    }

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

    public List<Card> getFlop() {
        return flop;
    }

    public void setFlop(List<Card> flop) {
        this.flop = flop;
    }

    public Card getTurn() {
        return turn;
    }

    public void setTurn(Card turn) {
        this.turn = turn;
    }

    public Card getRiver() {
        return river;
    }

    public void setRiver(Card river) {
        this.river = river;
    }

    @Override
    public String toString() {
        return "Game{" +
                "potSize=" + potSize +
                ", actions=" + actions +
                ", players=" + players +
                ", flop=" + flop +
                ", turn=" + turn +
                ", river=" + river +
                '}';
    }
}