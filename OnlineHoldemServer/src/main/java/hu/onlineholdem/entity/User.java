package hu.onlineholdem.entity;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the user database table.
 * 
 */
@Entity
@Table(name="user")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_id", unique=true, nullable=false)
	private Long userId;

	@Column(name="user_email", length=255)
	private String userEmail;

	@Column(name="user_name", nullable=false, length=255)
	private String userName;

	@Column(name="user_password", nullable=false, length=255)
	private String userPassword;

	//bi-directional many-to-one association to Player
	@OneToMany(mappedBy="user", fetch = FetchType.EAGER)
	private List<Player> players;

    //bi-directional many-to-one association to Rankings
    @OneToOne
    @JoinColumn(name="rank_id")
    private Rankings rankings;

	public User() {
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return this.userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

    @JsonIgnore
	public List<Player> getPlayers() {
		return this.players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

    @JsonIgnore
    public Rankings getRankings() {
        return rankings;
    }

    public void setRankings(Rankings rankings) {
        this.rankings = rankings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (userId != null ? !userId.equals(user.userId) : user.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}