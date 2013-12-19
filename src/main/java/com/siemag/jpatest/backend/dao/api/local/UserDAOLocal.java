package com.siemag.jpatest.backend.dao.api.local;


import com.siemag.jpatest.backend.dao.api.remote.UserDAORemote;
import com.siemag.jpatest.backend.model.User;
import javax.ejb.Local;
import java.util.List;


@Local
public interface UserDAOLocal extends UserDAORemote {

    /**
     * Retrieves persistent entity from database by name and password.
     *
     * @param parName - <tt>String</tt> identifier
     * @return <tt>User<Users></tt> entity or <tt>null</tt>
     */
    public User getUnblockedByName(String parName);

    List<User> findAllWithSecrets();


}
