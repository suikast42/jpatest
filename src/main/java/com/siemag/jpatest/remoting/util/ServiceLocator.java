package com.siemag.jpatest.remoting.util;


import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.jms.*;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

abstract class ServiceLocator {

    public static final String                        PERSISTENCE_UNIT_NAME        = "WMSDS";
    public static final String                        CONNECTION_FACTORY_JNDI_NAME = "java:/ConnectionFactory";

    protected Context                                 context                      = null;

    private BeanManager                               beanManager;
    private final Logger                              log                          = Logger.getLogger( ServiceLocator.class.getCanonicalName());


    private EntityManagerFactory                      entityManagerFactory;

    private JNDIName                                  controllerRepoNaming         = null;

    /**
     * Cache for <Global JNDI Name> ,<Concrete Module>, for doing a lookup without a module name
     */
    protected final ConcurrentHashMap<String, Module> moduleCache                  = new ConcurrentHashMap<String, Module>();

    public ServiceLocator() {
        try {
            init();
        } catch (NamingException e) {
            log.severe (e.getMessage());
        }
    }

    private interface JmsConnectionType {
        public ConnectionFactory getConnectionFactory() throws NamingException;
    }

    private class JmsConnectionTypeXA implements JmsConnectionType {

        @Override
        public ConnectionFactory getConnectionFactory() throws NamingException{
            return (ConnectionFactory)context.lookup(getJNDINameProvider().getLookupStringJMSConnectionFactoryXA());
        }
    }

    private class JmsConnectionTypeNonXA implements JmsConnectionType {

        @Override
        public ConnectionFactory getConnectionFactory() throws NamingException{
            return (ConnectionFactory)context.lookup(getJNDINameProvider().getLookupStringJMSConnectionFactoryNonXA());
        }
    }

    private class UserNamePasswordWrapper {
        private String username;
        private String password;

        public UserNamePasswordWrapper() {
            try {
                Hashtable<?, ?> environment = context.getEnvironment();
                username = (String)environment.get(Context.SECURITY_PRINCIPAL);
                password = (String)environment.get(Context.SECURITY_CREDENTIALS);
            } catch (NamingException e) {
                username = null;
                password = null;
            }
        }

        public String getUsername(){
            return username;
        }

        public String getPassword(){
            return password;
        }

        public boolean hasUsernameAndPassword(){
            return username != null && password != null;
        }
    }

    /**
     * Encapsulate the name of the jar that contains services.
     *
     * @author vuru
     */
    public static abstract class Module {
        /**
         * Name of the Jar, without the suffix .jar, which contains the service implementation
         *
         * @return
         */
        public abstract String getModuleName();

        public Module() {
            Modules.registerAdditionalModule(this);
        }

        @Override
        public boolean equals(Object obj){
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }

            if (Module.class.isInstance(obj)) {
                return ((Module)obj).getModuleName().equals(getModuleName());
            }
            return false;
        }

        @Override
        public int hashCode(){
            return getModuleName().hashCode();
        }
    }

    public interface JNDIName {

        /**
         * @param module The correspondent module object which contains the bean implementation.
         * @return Set modulename and return the same instance.
         */
        public JNDIName withModule(Module module);

        /**
         * @param beanName The deployed name of the Service.
         * @return Set JNDIName and return the same instance.
         */
        public JNDIName withBeanName(String beanName);

        /**
         * @param interfaze The remote or Local interface of the Service.
         * @return Set JNDIName and return the same instance.
         */
        public JNDIName withBusinessInterface(Class<?> interfaze);

        /**
         * Compute the serve or client side lookupstring.
         *
         * @return
         */
        public String getLookupString();

        /**
         * Return a new instance of me.
         *
         * @return
         */
        public JNDIName cloneMe();

        /**
         * A lookup String for the Entitymanager
         *
         * @return
         */
        public String getLookupStringEntitymanager();

        /**
         * A lookup String for the TransactionFactory
         *
         * @return
         */
        public String getLookupStringJMSConnectionFactoryNonXA();

        public abstract String getLookupStringJMSConnectionFactoryXA();

        /**
         * If prefix needed for lookup a jms que or topic
         *
         * @return
         */
        public String getLookupStringPrefixJMSDestination();

        /**
         * Lookup String for userTransaction
         *
         * @return
         */
        public String getLookupStringUserTransaction();

        /**
         * Where is the CDI beanmanager located
         *
         * @return
         */
        public String getLookupStringForBeanManager();

    }


    public abstract class JNDINameAbstract implements JNDIName {
        public final static String SEPARATOR = "/";
        public final static String APPNAME   = "";

        private String             beanName  = "";
        private Module             module;
        private Class<?>           businessInterface;

        @Override
        public JNDIName withModule(Module module){
            this.module = module;
            return this;
        }

        @Override
        public JNDIName withBeanName(String beanName){
            this.beanName = beanName;
            return this;
        }

        @Override
        public JNDIName withBusinessInterface(Class<?> interfaze){
            this.businessInterface = interfaze;
            return this;
        }

        @Override
        public String getLookupString(){
            StringBuilder builder = new StringBuilder();
            builder.append(getGlobalPrefix()).append(APPNAME).append(SEPARATOR);
            if (module != null) {
                builder.append(module.getModuleName()).append(SEPARATOR);
            } else {
                builder.append("").append(SEPARATOR);
            }

            builder.append(beanName);
            if (businessInterface != null) {
                builder.append("!").append(businessInterface.getName());
            }
            return builder.toString();
        }

        public String getBeanName(){
            return beanName;
        }

        public Module getModule(){
            return module;
        }

        public Class<?> getInterface(){
            return businessInterface;
        }

        /**
         * The first letter of the lookup String "java:global" for serverside for example.
         *
         * @return
         */
        public abstract String getGlobalPrefix();
    }

    /**
     * Static class with the name of the modules wich contains EJB services
     *
     * @author vuru
     */
    public static class Modules {

        private static Logger       log                    = Logger.getLogger(Modules.class.getCanonicalName());
        private static List<Module> modules;

        /**
         * The Device Controller services
         */
        public static final Module  JPATEST_MODULE    = new Module() {

            @Override
            public String getModuleName(){
                return "jpatest";
            }

        };




        /**
         * List of all registered modules
         */
        public static List<Module> values(){
            return modules;
        }

        public static synchronized void registerAdditionalModule(Module module){
            if (modules == null) {
                modules = new ArrayList<Module>();
            }
            if (!modules.contains(module)) {
                modules.add(module);
            }
        }

        /**
         * The Module object for a given ModuleName
         */
        public static Module getModuleForName(String aModuleName){
            for (Module module : values()) {
                if (module.getModuleName().equals(aModuleName)) {
                    return module;
                }
            }
            return null;
        }


    }



    public UserTransaction getUserTransaction(){
        try {
            return (UserTransaction)context.lookup(getJNDINameProvider().getLookupStringUserTransaction());
        } catch (NamingException e) {
            log.severe("UserTransaction not bound");
            return null;
        }
    }



    public final Destination getDestination(String aName) throws NamingException{
        return (Destination)this.context.lookup(getJNDINameProvider().getLookupStringPrefixJMSDestination() + aName);
    }

    public final Topic getTopic(String aName) throws NamingException{
        return (Topic)getDestination(aName);
    }

    public final Queue getQueue(String aName) throws NamingException{
        return (Queue)getDestination(aName);
    }

    /**
     * Computes the deployed name of a bean.
     *
     * @param beanClass
     * @return
     */
    public final String computeBeanName(Class<?> beanClass){
        Stateless stateless = beanClass.getAnnotation(Stateless.class);
        if (stateless != null && isNotEmpty(stateless.name())) {
            return stateless.name();
        }
        Stateful stateful = beanClass.getAnnotation(Stateful.class);
        if (stateful != null && isNotEmpty(stateful.name())) {
            return stateful.name();
        }
        Singleton singleton = beanClass.getAnnotation(Singleton.class);
        if (singleton != null && isNotEmpty(singleton.name())) {
            return singleton.name();
        }
        return beanClass.getSimpleName();
    }

    private boolean isNotEmpty(String name){
        return (name != null && !name.isEmpty());
    }

    /**
     *
     * @return a TopicConnection which is not bound to XA. You can use this on server and client side for sender and receiver
     * @throws JMSException
     * @throws NamingException
     */
    public final TopicConnection getTopicConnection() throws JMSException, NamingException{
        return (TopicConnection)getJMSConnection();
    }

    /**
     *
     * @return a QueueConnection which is not bound to XA. You can use this on server and client side for sender and receiver
     * @throws JMSException
     * @throws NamingException
     */
    public final QueueConnection getQueueConnection() throws JMSException, NamingException{
        return (QueueConnection)getJMSConnection();
    }

    /**
     *
     * @return a Connection which is not bound to XA. You can use this on server and client side for sender and receiver
     * @throws JMSException
     * @throws NamingException
     */
    public final Connection getJMSConnection() throws JMSException, NamingException{
        return getJMSConnectionInternal(new JmsConnectionTypeNonXA());
    }

    public final Connection getJMSConnectionXA() throws JMSException, NamingException{
        return getJMSConnectionInternal(new JmsConnectionTypeXA());
    }

    private final Connection getJMSConnectionInternal(JmsConnectionType type) throws JMSException, NamingException{
        UserNamePasswordWrapper wrapper = new UserNamePasswordWrapper();
        ConnectionFactory queueConnectionFactory = type.getConnectionFactory();

        Connection connection;
        if (wrapper.hasUsernameAndPassword()) {
            connection = queueConnectionFactory.createConnection(wrapper.getUsername(), wrapper.getPassword());
        } else {
            connection = queueConnectionFactory.createConnection();
        }
        return connection;
    }

    public void init(Hashtable<Object, String> aProps) throws NamingException{
        this.context = new InitialContext(aProps);
    }

    /**
     * Initilaize the Serivcelocator. Especially the Initialcontext. For server or client lookup.
     *
     * @throws NamingException
     */
    public abstract void init() throws NamingException;

    /**
     * @param aBeanInterface Remote Interface
     * @param aModule        Name of the Jar, without the suffix .jar, which contains the service implementation
     * @param aBeanName      Name of the service.
     * @return
     */
    public <T> T getService(Class<? extends T> aBeanInterface, Module aModule, String aBeanName){
        //    String lookupString = APP_NAME + "/" + aBeanName + "/local-" + aBeanInterface.getName();
        JNDIName globalJNDIName = getJNDINameProvider().withBeanName(aBeanName).withModule(aModule)
                .withBusinessInterface(aBeanInterface);
        //    ClassLoader classLoader = MaterialFlowControllerLocal.class.getClassLoader();
        //    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        //    Thread.currentThread().setContextClassLoader(classLoader);
        Object service = null;
        T locatedService = null;
        if (aModule != null) {
            try {
                service = context.lookup(globalJNDIName.getLookupString());
            } catch (NamingException e) {
                log.severe("Lookup for " + globalJNDIName.getLookupString() + " failed");
                return null;
            }
        } else {
            if (moduleCache.containsKey(globalJNDIName.getLookupString())) {
                return getService(aBeanInterface, moduleCache.get(globalJNDIName.getLookupString()), aBeanName);
            }
            HashMap<Module, Throwable> throwAbles = new HashMap<>();
            for (Module module : Modules.values()) {
                globalJNDIName = globalJNDIName.withModule(module);
                try {
                    service = context.lookup(globalJNDIName.getLookupString());
                    moduleCache.put(globalJNDIName.cloneMe().withModule(null).getLookupString(), module);
                    break;
                } catch (NamingException e) {
                    throwAbles.put(module, e);
                    // Try with next module
                }
            }
            if (service == null) {
                log.severe("Lookup for service " + aBeanName + " failed ");

            }
        }
        try {
            //      debugLookupString(globalJNDIName, locatedService, aBeanInterface);
            locatedService = aBeanInterface.cast(service);
        } catch (ClassCastException e) {
            log.severe("ClassCastException for servicename " + aBeanName + " with beaninterface " + aBeanInterface.getName());
        }
        //    finally{
        //      Thread.currentThread().setContextClassLoader(contextClassLoader);
        //    }

        return locatedService;
    }



    /**
     * @param aBeanInterface aBeanInterface Remote Interface
     * @param aModule        Name of the Jar, without the suffix .jar, which contains the service implementation
     * @param aBean          Name of the service.
     * @return
     */
    public <T> T getService(Class<? extends T> aBeanInterface, Module aModule, Class<?> aBean){
        return getService(aBeanInterface, aModule, computeBeanName(aBean));
    }

    /**
     * @param aBeanInterface Remote Interface
     * @param aBeanName      Name of the service.
     * @return
     */
    public <T> T getService(Class<? extends T> aBeanInterface, String aBeanName){
        return getService(aBeanInterface, null, aBeanName);
    }

    /**
     * @param aBeanInterface Remote Interface
     * @param aBean          Class of the service
     * @return
     */
    public <T> T getService(Class<? extends T> aBeanInterface, Class<?> aBean){
        return getService(aBeanInterface, computeBeanName(aBean));
    }



    @SuppressWarnings("unchecked")
    public <T> T getBeanByName(Class<? extends T> clazz, String aBeanName){
        try {
            return getCDIBean(clazz, (Bean<T>)getBeanManager().getBeans(aBeanName).iterator().next());
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T getBeanByQualifier(Class<? extends T> clazz, AnnotationLiteral qualifier){
        try {
            return getCDIBean(clazz, (Bean<T>)getBeanManager().getBeans(clazz, qualifier).iterator().next());
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T getBeanBeanDefaultQualifier(Class<? extends T> clazz){
        try {
            @SuppressWarnings("serial")
            AnnotationLiteral _default = new AnnotationLiteral<Default>() {
            };
            return getCDIBean(clazz, (Bean<T>)getBeanManager().getBeans(clazz, _default).iterator().next());
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @SuppressWarnings({ "unchecked" })
    private <T> T getCDIBean(Class<? extends T> clazz, Bean<T> bean){
        BeanManager _beanManager = getBeanManager();
        CreationalContext<T> ctx = _beanManager.createCreationalContext(bean);
        T reference = (T)_beanManager.getReference(bean, clazz, ctx);
        return reference;
    }



    public BeanManager getBeanManager(){
        if (beanManager == null) {
            try {
                beanManager = (BeanManager)context.lookup(getJNDINameProvider().getLookupStringForBeanManager());
            } catch (NamingException e) {
                throw new RuntimeException("BeanManager not found", e);
            }
        }
        return beanManager;
    }

    /**
     * The Client or server side JNDIName implementation
     *
     * @return
     */
    public abstract JNDIName getJNDINameProvider();

}
