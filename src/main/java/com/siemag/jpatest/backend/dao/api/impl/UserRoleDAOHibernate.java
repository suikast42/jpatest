package com.siemag.jpatest.backend.dao.api.impl;




import com.siemag.jpatest.backend.dao.api.local.UserRoleDAOLocal;
import com.siemag.jpatest.backend.dao.api.remote.UserRoleDAORemote;
import com.siemag.jpatest.backend.model.UserRole;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@SuppressWarnings("serial")
@Stateless
public class UserRoleDAOHibernate extends GenericHibernateDAO<UserRole, Long> implements UserRoleDAOLocal,UserRoleDAORemote {



    @Override
    public List<UserRole> findAll() {
        return executeNamedQuery(UserRole.NQ_ALL, UserRole.class);
    }

    @Override
    public List<UserRole> findAllWithSecrets() {
        return executeNamedQuery(UserRole.NQ_ALL_WITH_SECRET, UserRole.class);
    }

    @Override
    public UserRole createRole(UserRole aRole) {
        UserRole persistentRole = makePersistent(aRole);
        return persistentRole;
    }

    @Override
    public UserRole makePersistent(UserRole aParEditableIF) {
        UserRole role = super.makePersistent(aParEditableIF);

        return role;
    }

    @Override
    public Class<? extends UserRole> getPersistentClass() {
        return UserRole.class;
    }

}
