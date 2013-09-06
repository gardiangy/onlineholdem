package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Game;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class GameDAO implements BaseDAO<Game>{

    private EntityManager em;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

    public GameDAO() {
        em = emf.createEntityManager();
    }

    @Override
    public Game save(Game entity) {

        em.getTransaction().begin();
        if(null == entity.getGameId()){
            em.persist(entity);
        }else{
            em.merge(entity);
        }
        em.getTransaction().commit();
        em.close();
        return entity;
    }

    @Override
    public void delete(Game entity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Game findOne(Long id) {

        em.getTransaction().begin();
        Game game = em.find(Game.class,id);
        em.close();
        return game;
    }
}
