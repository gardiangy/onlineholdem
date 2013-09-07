package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Repository
public interface PlayerDAO extends JpaRepository<Player,Long>{
}
