package com.siemag.jpatest.backend.dao.api.remote;

import com.siemag.jpatest.backend.model.User;
import javax.ejb.Remote;

@Remote
public interface UserDAORemote extends GenericDAO<User, Long> {

    public User getUserByNameAndPassword(String aName, String aPassword) ;

}
