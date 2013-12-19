jpatest
=======
   Setup the test environment:
        1. Add a db resource which is named TESTDB or change it in the persistence.xml
        2. Adopt your DB provider in persistence.xml
        3. Start your testserver with a protoffset of 10 or change the lookup port
        com.siemag.jpatest.remoting.util.ServiceLocatorClientSide:85
        4. Adopt the propety jboss.test.dir to your testserver
        5. Adopt the property serverProfile for your runconfiguration
