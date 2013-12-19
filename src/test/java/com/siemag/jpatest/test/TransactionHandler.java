package com.siemag.jpatest.test;


import javax.inject.Inject;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.logging.Logger;

public class TransactionHandler {

  @Inject
  private transient Logger logger;

  private boolean          verbose = true;

  /**
   * 
   * @param utx
   */
  public void transactionBegin(UserTransaction utx){
    try {
      if (isVerbose()) {
        logger.severe("#### Transaction beginning " + utxStatusToString(utx.getStatus()) + " #####");
      }
      utx.begin();
      if (isVerbose()) {
        logger.severe("#### Transaction began " + utxStatusToString(utx.getStatus()) + " #####");
      }
    } catch (NotSupportedException | SystemException e) {
      logger.severe( e.getMessage());
        e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void transactionCommit(UserTransaction utx){
    try {
      if (isVerbose()) {
        logger.severe("#### Transaction commititting " + utxStatusToString(utx.getStatus()) + " #####");
      }
      utx.commit();
      if (isVerbose()) {
        logger.severe("#### Transaction committed " + utxStatusToString(utx.getStatus()) + " #####");
      
      }
    } catch (Exception e) {
      logger.severe("Transaction commit failed");
      throw new RuntimeException(e);
    }
  }

  public void transactionRollback(UserTransaction utx){
    try {
      if (isVerbose()) {
        logger.severe("#### Transaction rolling back " + utxStatusToString(utx.getStatus()) + " #####");
      }
      utx.rollback();
      if (isVerbose()) {
        logger.severe("#### Transaction rolled back " + utxStatusToString(utx.getStatus()) + " #####");
      }
    } catch (IllegalStateException | SecurityException | SystemException e1) {
      logger.severe("Rollback failed");

      throw new RuntimeException(e1);
    }
  }

  private String utxStatusToString(int utxStatus){

    switch (utxStatus) {
      case Status.STATUS_ACTIVE:
        return "STATUS_ACTIVE";
      case Status.STATUS_COMMITTED:
        return "STATUS_COMMITTED";
      case Status.STATUS_COMMITTING:
        return "STATUS_COMMITTING";
      case Status.STATUS_MARKED_ROLLBACK:
        return "STATUS_MARKED_ROLLBACK";
      case Status.STATUS_NO_TRANSACTION:
        return "STATUS_NO_TRANSACTION";
      case Status.STATUS_PREPARED:
        return "STATUS_PREPARED";
      case Status.STATUS_PREPARING:
        return "STATUS_PREPARING";
      case Status.STATUS_ROLLEDBACK:
        return "STATUS_ROLLEDBACK";
      case Status.STATUS_ROLLING_BACK:
        return "STATUS_ROLLING_BACK";
      case Status.STATUS_UNKNOWN:
        return "STATUS_UNKNOWN";

      default:
        return "UNKOWN";
    }
  }

  public void setVerbose(boolean verbose){
    this.verbose = verbose;
  }

  public boolean isVerbose(){
    return this.verbose;
  }

  public String getTransactionStatus(UserTransaction utx){
    try {
      return utxStatusToString(utx.getStatus());
    } catch (SystemException e) {
      return e.getMessage();
    }
  }
}
