package com.siemag.jpatest.backend.dao.api.remote;

import com.siemag.jpatest.backend.model.Editable;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;


public interface GenericDAO<T extends Editable, ID extends Serializable> {


    public List<T> findAll();

    public Future<List<T>> findAllAsync();


    public T getById(ID id, boolean lock);



    public T makePersistent(T aParEditableIF);


    public void makeTransient(T aParEditableIF);

    @Deprecated
    public T getByCriteria(Criterion... criterion);


    @Deprecated
    public List<T> findByCriteria(Criterion criterion, Order... orderby);


    @Deprecated
    public List<T> findByCriteriaArray(Criterion... criterion);

    public boolean lock(T aParEditableIF);


    public List<T> findByName(String searchPattern);


    public T getByName(String name);

    public T getByName(String name, boolean cacheable);


    boolean isNameExistis(String name);

    List<Long> makePersistent(List<T> editableles);

    List<Long> makeTransient(List<T> editables);

    public List<Long> makePersistent(List<T> editableles, Integer bulkFlushSize);

    List<Long> makeTransient(List<T> editables, Integer bulkFlushSize);


    Long getCount();

    Class<? extends T> getPersistentClass();


}
