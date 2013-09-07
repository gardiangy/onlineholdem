package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;


@Repository
public interface GameDAO extends JpaRepository<Game,Long>{

}
