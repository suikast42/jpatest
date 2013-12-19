package com.siemag.jpatest.remoting.util;

import com.siemag.jpatest.backend.dao.api.impl.UserDAOHibernate;
import com.siemag.jpatest.backend.dao.api.impl.UserRoleDAOHibernate;
import com.siemag.jpatest.backend.dao.api.remote.UserDAORemote;
import com.siemag.jpatest.backend.dao.api.remote.UserRoleDAORemote;

import javax.naming.InitialContext;

/**
 * @author: vuru
 * Date: 18.12.13
 * Time: 14:26
 */
public class ServiceUtil {

    private ServiceLocator  locator;
    private static Boolean server;
    private static ServiceUtil instance = new ServiceUtil();

    private ServiceUtil(){
        if (isServer() ) {
            locator = new ServiceLocatorServerSide();
        } else {
            locator = new ServiceLocatorClientSide();
        }
    }

    public static UserDAORemote getUserDAORemote() {
        return instance.locator.getService(UserDAORemote.class, UserDAOHibernate.class.getSimpleName());
    }

    public static UserRoleDAORemote getUserRoleDAORemote() {
        return instance.locator.getService(UserRoleDAORemote.class, UserRoleDAOHibernate.class.getSimpleName());
    }


    private  synchronized boolean isServer() {
        if (server == null) {
            Object service = null;
            try {
        /* ****************************************************************************   */
        /* The service java:/ConnectionFactory is standardized in the JEE6 specification  */
        /* and is deployed before any application.                                        */
        /* So we get a reference for this lookup then we are running in server            */
        /* ****************************************************************************   */

                InitialContext ictx = new InitialContext();
                service = ictx.lookup("java:/ConnectionFactory");
            } catch (Exception e) {
                // IGNORE
            }
            server = Boolean.valueOf(service != null);
        }
        return server.booleanValue();
    }


}
