package com.siemag.jpatest.remoting.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author: vuru
 * Date: 18.12.13
 * Time: 15:07
 */
class ServiceLocatorServerSide extends ServiceLocator {

    private class GlobalJNDIName extends JNDINameAbstract {

        @Override
        public JNDIName cloneMe() {
            return new GlobalJNDIName().withBeanName(getBeanName()).withBusinessInterface(getInterface()).withModule(getModule());
        }

        @Override
        public String getGlobalPrefix() {
            return "java:global" + SEPARATOR;
        }

        @Override
        public String getLookupStringEntitymanager() {
            return "java:/EntityManagerFactory";
        }

        @Override
        public String getLookupStringJMSConnectionFactoryNonXA() {
            return "java:/ConnectionFactory";
        }

        @Override
        public String getLookupStringJMSConnectionFactoryXA() {
            return "java:/JmsXA";
        }

        @Override
        public String getLookupStringPrefixJMSDestination() {
            return "";
        }

        @Override
        public String getLookupStringUserTransaction() {
            return "java:jboss/UserTransaction";
        }

        @Override
        public String getLookupStringForBeanManager() {
            return "java:comp/BeanManager";
        }

    }


    public ServiceLocatorServerSide() {
        super();
    }


    @Override
    public void init() throws NamingException {
        this.context = new InitialContext();
    }

    @Override
    public JNDIName getJNDINameProvider() {
        return new GlobalJNDIName();
    }


}
