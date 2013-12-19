package com.siemag.jpatest.backend.dao.api.impl;


import com.siemag.jpatest.backend.dao.api.local.EditableDAOLocal;
import com.siemag.jpatest.backend.dao.api.remote.GenericDAO;
import com.siemag.jpatest.backend.model.Editable;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


@SuppressWarnings("serial")
public abstract class GenericHibernateDAO<T extends Editable, ID extends Serializable> extends AbstractDAOHibernate implements GenericDAO<T, ID>, Serializable {

    @EJB
    private EditableDAOLocal eDAO;

    @Override
    public abstract Class<? extends T> getPersistentClass();


    protected EditableDAOLocal getEditableDAOLocal() {
        return this.eDAO;
    }

    @Override
    public T getById(ID id, boolean lock) {
        return (T) eDAO.getById(getPersistentClass(), (Long) id, lock);
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByName(String searchPattern) {
        Criteria crit = getHibernateSession().createCriteria(getPersistentClass());
        crit.add(Restrictions.like("name", searchPattern));
        return crit.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getByName(String name) {
        return (T) eDAO.getByName(getPersistentClass(), name);
    }

    @Override
    public T getByName(String name, boolean cacheable) {
        return (T) eDAO.getByName(getPersistentClass(), name, cacheable);
    }

    @Override
    public Long getCount() {
        TypedQuery<Long> query = em.createQuery("select count(o.name) from " + getPersistentClass().getSimpleName() + " o", Long.class);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return new Long(0L);
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        Query query = em.createQuery("SELECT o FROM " + getPersistentClass().getSimpleName() + " o");
        return query.getResultList();
    }

    @Override
    @Asynchronous
    public Future<List<T>> findAllAsync() {
        return new AsyncResult<>(findAll());
    }

    @Override
    public T makePersistent(T aParEditableIF) {
        return (T) eDAO.makePersistent(aParEditableIF);
    }

    @Override
    public boolean isNameExistis(String name) {
        TypedQuery<Integer> query = em.createQuery("SELECT 1 FROM " + getPersistentClass().getSimpleName() + " o where o.name=:name", Integer.class);
        query.setParameter("name", name);
        try {
            query.getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }catch(NonUniqueResultException e){
           logger.severe("Name should be unique");
            throw e;
        }
    }

    @Override
    public List<Long> makePersistent(List<T> editableles) {
        return eDAO.makePersistent(editableles);
    }

    @Override
    public List<Long> makePersistent(List<T> editableles, Integer bulkFlushSize) {
        return eDAO.makePersistent(editableles, bulkFlushSize);
    }

    @Override
    public void makeTransient(T aParEditableIF) {
        eDAO.makeTransient(aParEditableIF);
    }

    @Override
    public List<Long> makeTransient(List<T> editables, Integer bulkFlushSize) {
        return eDAO.makeTransient(editables, bulkFlushSize);
    }

    @Override
    public List<Long> makeTransient(List<T> editables) {
        return eDAO.makeTransient(editables);
    }


    @Override
    @SuppressWarnings("unchecked")
    public T getByCriteria(Criterion... criterion) {
        Criteria crit = getHibernateSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }

        return (T) crit.uniqueResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findByCriteria(Criterion aCriterion, org.hibernate.criterion.Order... aOrderby) {
        Criteria crit = getHibernateSession().createCriteria(getPersistentClass());
        if (aCriterion != null) {
            crit.add(aCriterion);
        }

        if (aOrderby != null) {
            for (org.hibernate.criterion.Order next : aOrderby) {
                crit.addOrder(next);
            }
        }

        return crit.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findByCriteriaArray(Criterion... aParCriterionArray) {
        if (aParCriterionArray != null) {
            Criteria crit = getHibernateSession().createCriteria(getPersistentClass());
            for (Criterion aCriterion : aParCriterionArray) {
                if (aCriterion != null) {
                    crit.add(aCriterion);
                }
            }
            return crit.list();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean lock(T aParEditableIF) {
        boolean aReturnValue = false;
        if (aParEditableIF != null) {
            em.lock(aParEditableIF, LockModeType.PESSIMISTIC_WRITE);
            aReturnValue = true;
        }
        return aReturnValue;
    }


    //TODO VURU 14.02.2012: Hibernate.Session --> javax.persistence.EntityManager
    protected final Session getHibernateSession() {
        return em.unwrap(Session.class);
    }
}
