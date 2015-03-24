package it.unina.is2project.sensorgames.database.dao;

import java.util.List;

public interface IDAO<T> {

    public long insert(T entity);

    public int update(T entity);

    public void delete(int id);

    public T findById(int id);

    public List<T> findAll();

    public int count();
}
