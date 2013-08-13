package hu.onlineholdem.dao;

import hu.onlineholdem.entity.Message;

import javax.persistence.*;
import java.util.List;

public interface BaseDAO<T> {


    T save(T entity);

    void delete(T entity);

    T findOne(Long id);

}
