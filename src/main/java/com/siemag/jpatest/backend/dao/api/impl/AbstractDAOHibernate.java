package com.siemag.jpatest.backend.dao.api.impl;


import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AbstractDAOHibernate {

  @Inject
  protected Logger logger;
  
  @PersistenceContext(unitName ="TESTDB")
  protected EntityManager em;

  /**
   * Excecute a named query for an entity
   * @param nqName
   * @param clazz
   * @return
   */
  public <R> List<R> executeNamedQuery(String nqName, Class<R> clazz){
    return executeNamedQuery(nqName, clazz, null);
  }

  /**
   * excecute a named query for an entity
   * @param nqName
   * @param clazz
   * @return
   */
  public <R> List<R> executeNamedQuery(String nqName, Class<R> clazz, Map<String, Object> props){
    TypedQuery<R> createNamedQuery = createNamedQuery(nqName, clazz, props);
    return createNamedQuery.getResultList();
  }
  
  
  public <R> R executeNamedQuerySingleResult(String nqName, Class<R> clazz){
    return executeNamedQuerySingleResult(nqName, clazz, null);
  }


  public <R> R executeNamedQuerySingleResult(String nqName, Class<R> clazz, Map<String, Object> props){
    TypedQuery<R> createNamedQuery = createNamedQuery(nqName, clazz, props);
    try {
      return createNamedQuery.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
  
  public <R> TypedQuery<R> createNamedQuery (String nqName, Class<R> clazz){
    return createNamedQuery(nqName, clazz,null);
  }
  
  /**
   * create a named query for an entity
   * @param nqName
   * @param clazz
   * @return
   */
  public <R> TypedQuery<R> createNamedQuery (String nqName, Class<R> clazz, Map<String, Object> props){
    TypedQuery<R> createNamedQuery = em.createNamedQuery(nqName, clazz);
    if (props != null && props.size() > 0) {
      for (String key : props.keySet()) {
        createNamedQuery.setParameter(key, props.get(key));
      }
    }
    return createNamedQuery;
  }

}
