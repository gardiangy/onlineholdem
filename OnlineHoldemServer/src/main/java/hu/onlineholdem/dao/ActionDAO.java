package hu.onlineholdem.dao;

import hu.onlineholdem.bo.Response;
import hu.onlineholdem.entity.Action;
import hu.onlineholdem.entity.Message;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class ActionDAO implements BaseDAO<Action>{

    private EntityManager em;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

    public ActionDAO() {
        em = emf.createEntityManager();
    }

    @Override
    public Action save(Action entity) {
        em.getTransaction().begin();
        if(null == entity.getActionId()){
            em.persist(entity);
        }else{
            em.merge(entity);
        }

        em.getTransaction().commit();
        em.close();
        return entity;
    }

    @Override
    public void delete(Action entity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Action findOne(Long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
