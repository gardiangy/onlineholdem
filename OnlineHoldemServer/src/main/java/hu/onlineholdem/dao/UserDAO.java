package hu.onlineholdem.dao;

import hu.onlineholdem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDAO extends JpaRepository<User,Long>{

    public User findByUserName(String userName);
}
