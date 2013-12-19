package com.siemag.jpatest.test;

import com.siemag.jpatest.remoting.util.ServiceUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class ArqtTest  {


    @Deployment
    public static WebArchive createArchiveTest() {
        File archive = new File("target/jpatest.war");
        return ShrinkWrap.createFromZipFile(WebArchive.class, archive);
    }

    @Test
    @RunAsClient
    public void test1(){
        ServiceUtil.getUserRoleDAORemote().findAll();
    }

    @Test
    public void test2(){
        ServiceUtil.getUserRoleDAORemote().findAll();
    }

}
