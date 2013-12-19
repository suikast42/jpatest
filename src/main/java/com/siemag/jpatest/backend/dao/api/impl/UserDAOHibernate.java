package com.siemag.jpatest.backend.dao.api.impl;



import com.siemag.jpatest.backend.dao.api.local.UserDAOLocal;
import com.siemag.jpatest.backend.dao.api.local.UserRoleDAOLocal;
import com.siemag.jpatest.backend.dao.api.remote.UserDAORemote;
import com.siemag.jpatest.backend.model.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hibernate-specific implementation of the <tt>ItemDAO</tt> non-CRUD data access object.
 *
 * @author Christian Bauer
 */
@SuppressWarnings("serial")
@Stateless
public class UserDAOHibernate extends GenericHibernateDAO<User, Long> implements UserDAOLocal, UserDAORemote {

    @EJB
   private UserRoleDAOLocal userRoleDAOLocal;

    @Override
    public List<User> findAll() {
        return executeNamedQuery(User.NQ_ALL, User.class);
    }

    @Override
    public List<User> findAllWithSecrets() {
        return executeNamedQuery(User.NQ_ALL_WITH_SECRET, User.class);
    }

    @Override
    public User getUnblockedByName(String parName) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", parName);
        return executeNamedQuerySingleResult(User.NQ_UNBLOCKED_BY_NAME, User.class, props);
    }

    @Override
    public User getUserByNameAndPassword(String aName, String aPassword) {
        Map<String, Object> ops = new HashMap<>();
        ops.put("name", aName);
        ops.put("password", aPassword);
        return executeNamedQuerySingleResult(User.NQ_BYNAME_AND_PASSWD, User.class, ops);
    }

    @Override
    public Class<? extends User> getPersistentClass() {
        return User.class;
    }


}
