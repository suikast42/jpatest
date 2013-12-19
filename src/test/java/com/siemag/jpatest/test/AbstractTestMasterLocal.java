package com.siemag.jpatest.test;

import com.siemag.jpatest.backend.dao.api.local.UserDAOLocal;
import com.siemag.jpatest.backend.dao.api.local.UserRoleDAOLocal;
import org.junit.After;
import org.junit.Before;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import static org.junit.Assert.fail;

/**
 * For common use for all Integration tests which running in cointainer. Use also only local interfaces here . <br>
 * Inject all common needed DAOS and methods which assert the manipulated {@link com.siemag.jpatest.backend.model.Editable}<br>
 * All inherited tests don't need start a Transaction. This class will start and try commit for every testmethod automatically.
 *
 * @author huso
 */
public class AbstractTestMasterLocal {



    @Resource
    protected UserTransaction utx;


    @EJB
    protected UserRoleDAOLocal userRoleDAO;
    @EJB
    protected UserDAOLocal userDAO;


    @Inject
    private TransactionHandler transactionHandler;

    @PersistenceContext(unitName ="TESTDB")
    protected EntityManager em;
    @Before
    public void init() {
        transactionBegin();
    }

    public void setTransactionTimeout(int seconds) {
        try {
            utx.setTransactionTimeout(seconds);
        } catch (SystemException e) {
            fail(e.getMessage());
        }
    }



    public void transactionBegin() {
        transactionHandler.transactionBegin(getUserTransaction());
    }

    public void transactionCommit() {
        transactionHandler.transactionCommit(getUserTransaction());
    }

    public void transactionRollback() {
        transactionHandler.transactionRollback(getUserTransaction());
    }

    private UserTransaction getUserTransaction() {
        return utx;
    }

    @After
    public void deinit() {
        transactionCommit();
    }

}
