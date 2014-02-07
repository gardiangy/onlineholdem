package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionDAO extends JpaRepository<Action,Long>{

    public List<Action> findByGameOrderByActionIdAsc(Game game);
}
