package com.siemag.jpatest.backend.dao.api.impl;

import com.siemag.jpatest.backend.dao.api.local.EditableDAOLocal;
import com.siemag.jpatest.backend.dao.api.remote.EditableDAORemote;
import com.siemag.jpatest.backend.model.Editable;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;


@Stateless
public class EditableDAOHibernate extends AbstractDAOHibernate implements EditableDAOLocal, EditableDAORemote {

    @Inject
    private Logger log;

    private static final int BULK_FLUSH_SIZE = 50;


    @Asynchronous
    @Override
    public Future<List<? extends Editable>> findAllFromSerivce(String serivceName) {
        throw new UnsupportedOperationException("Not implemented know");
    }


    @Override
    //  @SuppressWarnings("unchecked")
    public Editable getById(Class<? extends Editable> clazz, Long anId, boolean aLock) {
        Editable entity = null;

        try {
            if (aLock) {
                entity = em.find(clazz, anId, LockModeType.PESSIMISTIC_WRITE);
            } else {
                entity = em.find(clazz, anId);
            }
            return entity;
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public <T extends Editable> T getByName(Class<? extends Editable> clazz, String name) {
        return getByName(clazz, name, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Editable> T getByName(Class<? extends Editable> clazz, String name, boolean cacheable) {
        T result = null;
        Criteria crit = getHibernateSession().createCriteria(clazz);
        crit.add(Restrictions.eq("name", name));
        crit.setCacheable(cacheable);
        result = (T) crit.uniqueResult();
        return result;
    }


    @Override
    public Editable getEditableByCriteria(Class<? extends Editable> aClass, Criterion... aCriterias) {

        Criteria criteria = getHibernateSession().createCriteria(aClass);
        for (Criterion next : aCriterias) {
            criteria.add(next);
        }
        return (Editable) criteria.uniqueResult();

    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Editable> findCollectionByHQLCriteriaString(String aParCriteriaString) {
        return getHibernateSession().createQuery(aParCriteriaString).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Editable> findCollectionByHQLCriteriaString(String aParCriteriaString, Date dStart, Date dEnd) {
        return getHibernateSession().createQuery(aParCriteriaString).setDate("startDate", dStart).setDate("endDate", dEnd)
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Editable> Collection<T> findEditablesByCriteria(
            Class<T> aParClass,
            Collection<Criterion> aParCriteriaCollection) {


        if (aParClass != null) {

            Criteria criteria = getHibernateSession().createCriteria(aParClass);
            Iterator<Criterion> iter = aParCriteriaCollection.iterator();
            for (; iter.hasNext(); ) {
                criteria.add(iter.next());
            }
            return criteria.list();
        }
        return null;
    }

    @Override
    public <T extends Editable> Collection<T> findEditablesByCriteria(Class<T> aParClass, Criterion... aParCriteriaArray) {
        return (aParCriteriaArray == null) ? new Vector<T>() : findEditablesByCriteria(aParClass, Arrays.asList(aParCriteriaArray));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Editable> Collection<T> findByCriteria(Class<T> aParClass, Collection<Criterion> aParCriteriaCollection) {

        Criteria criteria = getHibernateSession().createCriteria(aParClass);
        Iterator<Criterion> iter = aParCriteriaCollection.iterator();
        for (; iter.hasNext(); ) {
            criteria.add(iter.next());
        }
        return criteria.list();
    }

    @Override
    public <T extends Editable> T makePersistent(T anEditable) {
        return makePersistent(anEditable, false);
    }

    @Override
    public <T extends Editable> T mergeIfNeeeded(T editable) {
        if (editable.getId() == null) {
            return editable;
        }
        if (!em.contains(editable)) {
            editable = em.merge(editable);
        }
        return editable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Editable> T makePersistent(T editable, boolean partly) {
        // TODO vuru 10.12.13: Check out why we needd a flush here?

        {   // BUILD 31

            if (editable.getId() == null) {
                em.persist(editable);
            } else {
                editable = mergeIfNeeeded(editable);
            }
            em.flush();
            return editable;
        }

    }

    @Override
    public void makeTransient(Editable editable) {
        if (editable.getId() == null) {
            logger.severe(new StringBuilder().append("Try to remove not peristent object. Ignore it ").append(editable.toString()).toString());
            return;
        }
        Editable toRemove = getById(editable.getClass(), editable.getId(), false);
        if (toRemove == null) {
            logger.severe(new StringBuilder().append(editable.toString()).append(" is already deleted").toString());
            return;
        }
        logger.severe(new StringBuilder().append("Deleting ").append(editable).toString());
        em.remove(toRemove);
    }

    @Override
    public <T extends Editable> List<Long> makePersistent(List<T> editableles, Integer bulkFlushSize) {
        int flushSize = BULK_FLUSH_SIZE;
        if (bulkFlushSize != null) {
            flushSize = bulkFlushSize.intValue();
        }

        List<Long> ids = new ArrayList<>();
        if (editableles == null || editableles.size() == 0) {
            log.severe("Nothing to persist");
            return ids;
        }
        // Backup the current flushmode
        FlushMode flushModeBefore = getHibernateSession().getFlushMode();
        getHibernateSession().setFlushMode(FlushMode.MANUAL);
        int i = 0;
        // Save or update all editables and flush every 50 times
        Date lastChanged = new Date();
        for (T eif : editableles) {
            eif.setLastChangeTime(lastChanged);
            getHibernateSession().saveOrUpdate(eif);
            ids.add(eif.getId());
            i++;
            if (i % flushSize == 0) {
                em.flush();
                em.clear();
            }
        }
        em.flush();
        em.clear();
        getHibernateSession().setFlushMode(flushModeBefore);
        return ids;
    }

    @Override
    public <T extends Editable> List<Long> makePersistent(List<T> editableles) {
        throw new UnsupportedOperationException("Not implemented know");
    }

    @Override
    public <T extends Editable> List<T> getByIds(Class<T> clazz, List<Long> ids) {
        throw new UnsupportedOperationException("Not implemented know");
    }

    @Override
    public <T extends Editable> List<Long> makeTransient(List<T> editables, Integer bulkFlushSize) {
        throw new UnsupportedOperationException("Not implemented know");
    }

    @Override
    public <T extends Editable> List<Long> makeTransient(List<T> editables) {
        throw new UnsupportedOperationException("Not implemented know");
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends Editable> T refreshIfNecessary(T editable) {
        if (editable != null && editable.getId() != null) {
            //      editable = (T)getById(editable.getClass(), editable.getId(), true);
            editable = (T) getById(Hibernate.getClass(editable), editable.getId(), true);
        }
        return editable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Editable> List<T> refreshIfNecessary(List<T> editables) {
        List<T> retColl = new ArrayList<>();
        for (T editable : editables) {
//            retColl.add((T) getById(editable.getClass(), editable.getId(), true));
            retColl.add((T) refreshIfNecessary(editable));
        }
        return retColl;
    }

    //TODO VURU 14.02.2012: Hibernate.Session --> javax.persistence.EntityManager
    protected final Session getHibernateSession() {
        return em.unwrap(Session.class);
    }

    @Override
    public <E extends Editable> E initializeAndUnproxy(E editable) {
        throw new UnsupportedOperationException("Not implemented know");
    }


    @Override
    public Class<? extends Editable> getConcreteClassOfEditable(Class<? extends Editable> superClass, Long id) {
        Editable ed = getById(superClass, id, false);
        if (ed == null) {
            return null;
        }
        return ed.getClass();
    }
}
