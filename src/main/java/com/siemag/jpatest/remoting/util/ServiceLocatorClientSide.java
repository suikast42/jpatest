package com.siemag.jpatest.remoting.util;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;


/**
 * Client side Service lookups
 *
 * @author vuru
 */
class ServiceLocatorClientSide extends ServiceLocator {

    private class GlobalJNDIName extends JNDINameAbstract {

        @Override
        public JNDIName cloneMe() {
            return new GlobalJNDIName().withBeanName(getBeanName()).withBusinessInterface(getInterface()).withModule(getModule());
        }

        @Override
        public String getGlobalPrefix() {
            return "ejb:";
        }

        @Override
        public String getLookupStringJMSConnectionFactoryXA() {
            throw new UnsupportedOperationException("Only serverside");
        }

        @Override
        public String getLookupStringEntitymanager() {
            throw new UnsupportedOperationException("Only serverside");
        }

        @Override
        public String getLookupStringJMSConnectionFactoryNonXA() {
            return "jms/RemoteConnectionFactory";

        }

        @Override
        public String getLookupStringPrefixJMSDestination() {
            return "jms/";
        }

        @Override
        public String getLookupStringUserTransaction() {
            throw new UnsupportedOperationException("Only serverside");
        }

        @Override
        public String getLookupStringForBeanManager() {
            throw new UnsupportedOperationException("CDI Beanmanager is only for serverside available at moment");
        }

    }

    public ServiceLocatorClientSide() {
        super();
    }

    /**
     * Create a JNDI API InitialContext object if none exists yet.
     */
    @Override
    public void init() throws NamingException {
        //TODO vuru 21.08.2012: --> Property File
        final String portsPropName = "remote.connection.default.port";
        final String serverName = "wmsserver";
        //    final String serverName = "127.0.0.1";

        final String user = "wmsuser";
        final String passwd = "wmsuser123";
        String remotePort = "4457";

        Hashtable<String, String> jndiProperties = new Hashtable<>();
        Properties pr = new Properties();

        pr.put("endpoint.name", "client-endpoint");
        pr.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
        pr.put("remote.connections", "default");
        pr.put(portsPropName, remotePort);
        pr.put("remote.connection.default.host", serverName);
        //    pr.put("remote.connection.default.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS", "JBOSS-LOCAL-USER");
        pr.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
        pr.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
        pr.put("remote.connection.default.username", user);
        pr.put("remote.connection.default.password", passwd);

        EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(pr);
        ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
        EJBClientContext.setSelector(selector);

        // for Remote EJB
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

        // For remote JMS
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        jndiProperties.put(Context.SECURITY_PRINCIPAL, user);
        jndiProperties.put(Context.SECURITY_CREDENTIALS, passwd);
        jndiProperties.put(Context.PROVIDER_URL, "remote://" + serverName + ":" + remotePort);
        this.context = new InitialContext(jndiProperties);
    }

    @Override
    public JNDIName getJNDINameProvider() {
        return new GlobalJNDIName();
    }

    @Override
    public <T> T getService(Class<? extends T> aBeanInterface, Class<?> aBean) {
        return getService(aBeanInterface, computeBeanName(aBean));
    }

    @Override
    public <T> T getService(Class<? extends T> aBeanInterface, String aBeanName) {
        return super.getService(aBeanInterface, getDefaultModule(), aBeanName);
    }

    /**
     * In Jboss remoting you'll get always a Proxy. So if a Module is not specified use always the defalz module
     *
     * @return
     */
    private Module getDefaultModule() {
        return Modules.JPATEST_MODULE;
    }

}
