package it.unina.is2project.sensorgames.stats.database.dao;

import java.util.List;

public interface IDAO<T> {

    long insert(T entity);

    int update(T entity);

    void delete(int id);

    T findById(int id);

    List<T> findAll(boolean ordered);

    int count();
}
