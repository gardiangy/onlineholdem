package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Message;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class MessageDAO implements BaseDAO<Message>{

    private EntityManager em;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

    public MessageDAO() {
        em = emf.createEntityManager();
    }

    public List<Message> getAll() {

        em.getTransaction().begin();

        Query q = em.createQuery("select m from Message m");

        List<Message> messageList = q.getResultList();

        em.close();

        return messageList;
    }

    @Override
    public Message save(Message entity) {
        em.getTransaction().begin();
        if(null == entity.getId()){
            em.persist(entity);
        }else{
            em.merge(entity);
        }

        em.getTransaction().commit();
        em.close();
        return entity;
    }

    @Override
    public void delete(Message entity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Message findOne(Long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
