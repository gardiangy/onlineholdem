package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionDAO extends JpaRepository<Action,Long>{

}
