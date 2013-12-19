package com.siemag.jpatest.backend.dao.api.remote;

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

import com.siemag.jpatest.backend.model.UserRole;

import javax.ejb.Remote;

@Remote
public interface UserRoleDAORemote extends GenericDAO<UserRole, Long> {

}
