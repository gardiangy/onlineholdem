package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;


@Repository
public interface GameDAO extends JpaRepository<Game,Long>{

    public List<Game>  findByGameNameContaining(String gameName);

    public Page<Game> findByGameNameContaining(String gameName, Pageable pageable);
}
