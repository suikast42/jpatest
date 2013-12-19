package com.siemag.jpatest.backend.dao.api.local;

/**
 * <pre>
 * ----------------------------------------------------------------------------------------------------
 * ----------------------------------------------------------------------------------------------------
 * History:
 * ----------------------------------------------------------------------------------------------------
 * Key: (yyyymmdd) | Description
 * ----------------------------------------------------------------------------------------------------
 * dit20070521     | RELEASE: Refactored to new DAO concept.
 * ----------------------------------------------------------------------------------------------------
 *
 * </pre>
 */


import com.siemag.jpatest.backend.dao.api.remote.UserRoleDAORemote;
import com.siemag.jpatest.backend.model.UserRole;

import javax.ejb.Local;
import java.util.List;

@Local
public interface UserRoleDAOLocal extends UserRoleDAORemote {

    List<UserRole> findAllWithSecrets();

    public UserRole createRole(UserRole aRole);

}
