package hu.onlineholdem.dao;

import hu.onlineholdem.bo.Response;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

@Repository
public interface ActionDAO extends JpaRepository<Action,Long>{

}
