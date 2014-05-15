package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Rankings;
import hu.onlineholdem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankingsDAO extends JpaRepository<Rankings,Long>{

    public Page<Rankings> findByUserUserNameContainingOrderByRankPointDesc(String userName, Pageable pageable);

    public Rankings findByUserUserName(String userName);

}
