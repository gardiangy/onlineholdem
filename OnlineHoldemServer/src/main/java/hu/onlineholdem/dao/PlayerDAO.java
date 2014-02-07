package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerDAO extends JpaRepository<Player,Long>{

    public List<Player> findByGameOrderByPlayerOrderDesc(Game game);
}
