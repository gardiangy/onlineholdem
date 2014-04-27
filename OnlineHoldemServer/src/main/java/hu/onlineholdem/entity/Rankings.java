package hu.onlineholdem.entity;

import hu.onlineholdem.enums.GameState;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the rankings database table.
 */
@Entity
@Table(name = "rankings")
public class Rankings implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id", unique = true, nullable = false)
    private Long rankId;

    @Column(name = "rank_point")
    private Integer rankPoint;

    @Column(name = "rank_played_games")
    private Integer rankPlayedGames;

    //bi-directional many-to-one association to Action
    @OneToOne(mappedBy = "rankings", cascade = {CascadeType.ALL})
    private User user;


    public Rankings() {
    }

    public Long getRankId() {
        return rankId;
    }

    public void setRankId(Long rankId) {
        this.rankId = rankId;
    }

    public Integer getRankPoint() {
        return rankPoint;
    }

    public void setRankPoint(Integer rankPoint) {
        this.rankPoint = rankPoint;
    }

    public Integer getRankPlayedGames() {
        return rankPlayedGames;
    }

    public void setRankPlayedGames(Integer rankPlayedGames) {
        this.rankPlayedGames = rankPlayedGames;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}