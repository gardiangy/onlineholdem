package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Game;
import hu.onlineholdem.entity.Player;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PlayerDAO implements BaseDAO<Player>{

    private EntityManager em;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

    public PlayerDAO() {
        em = emf.createEntityManager();
    }

    @Override
    public Player save(Player entity) {

        em.getTransaction().begin();
        if(null == entity.getPlayerId()){
            em.persist(entity);
        }else{
            em.merge(entity);
        }
        em.getTransaction().commit();
        em.close();
        return entity;
    }

    @Override
    public void delete(Player entity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Player findOne(Long id) {

        em.getTransaction().begin();
        Player player = em.find(Player.class,id);
        em.close();
        return player;
    }
}
