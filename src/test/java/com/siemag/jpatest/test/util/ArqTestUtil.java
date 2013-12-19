package com.siemag.jpatest.test.util;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;

/**
 * @author: vuru
 * Date: 18.12.13
 * Time: 14:14
 */
public class ArqTestUtil {

    public static JavaArchive getJarArchive( Class<?>... additionalClasses) {
        String jarName ="additionalClasses";
        JavaArchive jarArch = null;
        try {
            if (jarName != null && !jarName.trim().isEmpty()) {
                jarArch = ShrinkWrap.create(JavaArchive.class).addClasses(additionalClasses);
            } else {
                jarArch = ShrinkWrap.create(JavaArchive.class, jarName).addClasses(additionalClasses);
            }
                String path = new File("").getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator
                        + "resources" + File.separator + "beans.xml";
                File beansxml = new File(path);
                //        jarArch = jarArch.addAsResource(EmptyAsset.INSTANCE, "beans.xml");
                jarArch = jarArch.addAsResource(beansxml, "META-INF/beans.xml");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return jarArch;
    }
}
