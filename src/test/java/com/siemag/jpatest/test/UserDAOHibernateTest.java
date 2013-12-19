package com.siemag.jpatest.test;

import com.siemag.jpatest.backend.dao.api.local.UserDAOLocal;
import com.siemag.jpatest.backend.dao.api.remote.UserDAORemote;
import com.siemag.jpatest.backend.dao.api.remote.UserRoleDAORemote;
import com.siemag.jpatest.backend.model.User;
import com.siemag.jpatest.backend.model.UserRole;
import com.siemag.jpatest.remoting.util.ServiceUtil;
import com.siemag.jpatest.test.util.ArqTestUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
/**
 * Test the insertion and deletion of the systemproperties.
 * All System properties are cached. So they need a special test for midfications.
 *
 * @author vuru
 *
 */
public class UserDAOHibernateTest extends AbstractTestMasterLocal {

    @Inject
    private Logger logger;

    @EJB
    private UserDAOLocal userDAO;


    private final int countOfUsers = 100;
    private final String userNameSuffix = "UserDAOHibernateTest";
    private final List<String> tmpUserNames = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;

        {
            for (int i = 0; i < countOfUsers; i++) {

                add(userNameSuffix + i);
            }
        }
    };

    @Deployment
    public static WebArchive createArchiveTest() {
        File archive = new File("target/jpatest.war");

        WebArchive war =  ShrinkWrap.createFromZipFile(WebArchive.class, archive);
        JavaArchive jarArchive = ArqTestUtil.getJarArchive(UserDAOHibernateTest.class, AbstractTestMasterLocal.class, TransactionHandler.class);
        war.addAsLibrary(jarArchive);
        return war;
    }

    // Disable Begintransaction
    @Override
    @Before
    public void init() {
    }

    //Disable Commit transaction
    @Override
    @After
    public void deinit() {
    }


    //################################   BEGIN DAO TEST METHODS ############################# //


    @Test
    public void testFindAll() throws Exception {
        transactionBegin();
        deleteUsers();
        transactionCommit();

        int countBeforeInsert = userDAO.findAll().size();

        transactionBegin();
        createUsers();
        transactionCommit();

        List<User> all = userDAO.findAll();

        assertThat(all.size(), equalTo(countBeforeInsert + countOfUsers));
        for (User user : findAllTestObjects()) {
            assertThat(user.getName(), startsWith(userNameSuffix));
        }

        transactionBegin();
        deleteUsers();
        transactionCommit();
    }

    @Test
    public void testFindAllWithSecrets() throws Exception {
        transactionBegin();
        deleteUsers();
        transactionCommit();

        User user1 = createUser(userNameSuffix + "1");
        User user2 = createUser(userNameSuffix + "2");
        User user3 = createUser(userNameSuffix + "3");
        User user4 = createUser(userNameSuffix + "4");

        user1.setSecret(true);
        user4.setSecret(true);

        int sizeBeforeInsertWithoutSecrects = userDAO.findAll().size();
        int sizeBeforeWithSecrets = userDAO.findAllWithSecrets().size();

        transactionBegin();
        userDAO.makePersistent(user1);
        userDAO.makePersistent(user2);
        userDAO.makePersistent(user3);
        userDAO.makePersistent(user4);
        transactionCommit();
        int sizeAfterInsertWithoutSecrects = userDAO.findAll().size();
        int sizeAfterWithSecrets = userDAO.findAllWithSecrets().size();

        assertThat(sizeAfterInsertWithoutSecrects - sizeBeforeInsertWithoutSecrects, is(2));
        assertThat(sizeAfterWithSecrets - sizeBeforeWithSecrets, is(4));

        transactionBegin();
        deleteUsers();
        transactionCommit();
    }

    @Test
    public void testGetUnblockedByName() throws Exception {
        transactionBegin();
        deleteUsers();
        transactionCommit();

        User user1 = createUser(userNameSuffix + "1");
        User user2 = createUser(userNameSuffix + "2");
        User user3 = createUser(userNameSuffix + "3");
        User user4 = createUser(userNameSuffix + "4");

        user1.setBlocked(true);
        user2.setBlocked(true);

        transactionBegin();
        user1 = userDAO.makePersistent(user1);
        user2 = userDAO.makePersistent(user2);
        user3 = userDAO.makePersistent(user3);
        user4 = userDAO.makePersistent(user4);
        transactionCommit();

        // Blocked Users
        assertThat(userDAO.getUnblockedByName(user1.getName()), nullValue());
        assertThat(userDAO.getUnblockedByName(user2.getName()), nullValue());

        //Unblocked users
        assertThat(userDAO.getUnblockedByName(user3.getName()), equalTo(user3));
        assertThat(userDAO.getUnblockedByName(user4.getName()), equalTo(user4));

        transactionBegin();
        deleteUsers();
        transactionCommit();
    }

    @Test
    public void testGetUserByNameAndPassword() throws Exception {
        transactionBegin();
        deleteUsers();
        transactionCommit();

        User user1 = createUser(userNameSuffix + "1");
        User user2 = createUser(userNameSuffix + "2");
        User user3 = createUser(userNameSuffix + "3");
        User user4 = createUser(userNameSuffix + "4");


        transactionBegin();
        user1 = userDAO.makePersistent(user1);
        user2 = userDAO.makePersistent(user2);
        user3 = userDAO.makePersistent(user3);
        user4 = userDAO.makePersistent(user4);
        transactionCommit();

        assertThat(userDAO.getUserByNameAndPassword(user1.getName(), user1.getPassword()), equalTo(user1));
        assertThat(userDAO.getUserByNameAndPassword(user2.getName(), user2.getPassword()), equalTo(user2));
        assertThat(userDAO.getUserByNameAndPassword(user3.getName(), user3.getPassword()), equalTo(user3));
        assertThat(userDAO.getUserByNameAndPassword(user4.getName(), user4.getPassword()), equalTo(user4));

        assertThat(userDAO.getUserByNameAndPassword(user1.getName(), "Foo"), nullValue());
        assertThat(userDAO.getUserByNameAndPassword(user2.getName(), "Foo"), nullValue());
        assertThat(userDAO.getUserByNameAndPassword(user3.getName(), "Foo"), nullValue());
        assertThat(userDAO.getUserByNameAndPassword(user4.getName(), "Foo"), nullValue());

        assertThat(userDAO.getUserByNameAndPassword("Foo", user1.getPassword()), nullValue());
        assertThat(userDAO.getUserByNameAndPassword("Foo", user2.getPassword()), nullValue());
        assertThat(userDAO.getUserByNameAndPassword("Foo", user3.getPassword()), nullValue());
        assertThat(userDAO.getUserByNameAndPassword("Foo", user4.getPassword()), nullValue());

        transactionBegin();
        deleteUsers();
        transactionCommit();
    }

    @Test
    public void testGetPersistentClass() throws Exception {
        assertThat(User.class.getName(), equalTo(userDAO.getPersistentClass().getName()));
    }
    //################################   END DAO TEST METHODS ############################# //
    //################################   BEGIN JPA TEST RELATIONS ############################# //
    @Test
    @RunAsClient
    /**
     * Check if the relation from user to userrole is loaded eager,
     */
    public void testEagerLoadingOfUserRoles() {

        String userName = userNameSuffix + "1";
        UserDAORemote userDAORemote = ServiceUtil.getUserDAORemote();
        User userFromDB1 = userDAORemote.getByName(userName);

        if (userFromDB1 == null) {
            userFromDB1 = userDAORemote.makePersistent(createUser(userName));
        }

        deleteRolesRemote();
        List<UserRole> createdRoles = createUserRolesRemote();

        userFromDB1.addUserRoles(createdRoles);
        userDAORemote.makePersistent(userFromDB1);

        User userFromDB2 = userDAORemote.getByName(userFromDB1.getName());

        for (UserRole role : userFromDB2.getUserRoles()) {
            assertThat(createdRoles.contains(role), is(true));
        }

        // Remove the roles from userFromDB1. The relation from UserRole to User is lazy.
        userFromDB1.removeUserRoles();
        userDAORemote.makePersistent(userFromDB1);

//        // The back reference should be lazy
//        Exception throwed = null;
//        try {
//            userFromDB2.removeUserRoles();
//        } catch (Exception e) {
//            throwed = ExceptionUtils.getWrappedException(e, LazyInitializationException.class);
//        }
//        assertThat(throwed, notNullValue());
//        assertThat(throwed.getClass().getName(), equalTo(LazyInitializationException.class.getName()));

        User userFromDB3 = userDAORemote.getByName(userFromDB2.getName());
        assertThat(userFromDB3.getUserRoles().size(), is(0));

        deleteRolesRemote();
    }

    @Test
    public void testRelationUserUserRole() {

        deleteRolesRemote();
        deleteUsers();
        /*
            1. Create a User   and Create role1 - role5
            2. Add role1 - role5 to user  and check if the user in the roles
            3. Add the same role again and persist
            4. Remove role4 and role5 from user and check
            5. Use setUseres add all Roles again
            6. Delete the user and check if the roles don't deleted
            7. Delete testdata
         */
        String userName1 = userNameSuffix + "1";


        String roleName1 = userNameSuffix + "1";
        String roleName2 = userNameSuffix + "2";
        String roleName3 = userNameSuffix + "3";
        String roleName4 = userNameSuffix + "4";
        String roleName5 = userNameSuffix + "5";
        {   // 1.

            transactionBegin();
            createAndPersitUser(userName1);

            createAndPersitUserRole(roleName1);
            createAndPersitUserRole(roleName2);
            createAndPersitUserRole(roleName3);
            createAndPersitUserRole(roleName4);
            createAndPersitUserRole(roleName5);

            transactionCommit();
        }

        {   // 2.
            logger.info("##################### BEGIN TRACE #########################");
            transactionBegin();
            User user1 = userDAO.getByName(userName1);


//            User user1 = uDAO.getByName(userName1);

            StringBuffer buff = new StringBuffer();
            buff.append("Hascode of user ").append(user1.hashCode())
                    .append("\n").append("User toString ").append(user1.toString());
            logger.info(buff.toString());

            UserRole role1 = userRoleDAO.getByName(roleName1);
            UserRole role2 = userRoleDAO.getByName(roleName2);
            UserRole role3 = userRoleDAO.getByName(roleName3);
            UserRole role4 = userRoleDAO.getByName(roleName4);
            UserRole role5 = userRoleDAO.getByName(roleName5);

            user1.addUserRole(role1);
            user1.addUserRole(role2);
            user1.addUserRole(role3);
            user1.addUserRole(role4);
            user1.addUserRole(role5);

            user1.setFirstName("Foo");
            user1.setLastName("Bar");
            logger.info("##################### COMMIT  #########################");
            userDAO.makePersistent(user1);
            transactionCommit();
            logger.info("##################### END TRACE #########################");
            // This side is Eager. So we don't need a transaction

            user1 = userDAO.getByName(userName1);

            assertThat(user1.getUserRoles().contains(role1), is(true));
            assertThat(user1.getUserRoles().contains(role2), is(true));
            assertThat(user1.getUserRoles().contains(role3), is(true));
            assertThat(user1.getUserRoles().contains(role4), is(true));
            assertThat(user1.getUserRoles().contains(role5), is(true));

            // Check the reverse side
            assertThat(role1.getUsers().contains(user1), is(true));
            assertThat(role2.getUsers().contains(user1), is(true));
            assertThat(role3.getUsers().contains(user1), is(true));
            assertThat(role4.getUsers().contains(user1), is(true));
            assertThat(role5.getUsers().contains(user1), is(true));
        }

        {  // 3.
            transactionBegin();
            User user = userDAO.getByName(userName1);
            int sizeOfRoles = user.getUserRoles().size();
            user.addUserRoles(user.getUserRoles());
            transactionCommit();

            assertThat(sizeOfRoles, is(user.getUserRoles().size()));

        }

        {   // 4.
            transactionBegin();
            User user = userDAO.getByName(userName1);
            UserRole role1 = userRoleDAO.getByName(roleName1);
            UserRole role2 = userRoleDAO.getByName(roleName2);
            UserRole role3 = userRoleDAO.getByName(roleName3);
            UserRole role4 = userRoleDAO.getByName(roleName4);
            UserRole role5 = userRoleDAO.getByName(roleName5);
            user.removeUserRole(role4);
            user.removeUserRole(role5);
            userDAO.makePersistent(user);
            transactionCommit();

            // This side is Eager. So we don't need a transaction

            user = userDAO.getByName(userName1);

            assertThat(user.getUserRoles().contains(role1), is(true));
            assertThat(user.getUserRoles().contains(role2), is(true));
            assertThat(user.getUserRoles().contains(role3), is(true));
            assertThat(user.getUserRoles().contains(role4), is(false));
            assertThat(user.getUserRoles().contains(role5), is(false));
        }
        {   //5
            transactionBegin();
            Collection<UserRole> roles = new ArrayList<>();
            UserRole role1 = userRoleDAO.getByName(roleName1);
            UserRole role2 = userRoleDAO.getByName(roleName2);
            UserRole role3 = userRoleDAO.getByName(roleName3);
            UserRole role4 = userRoleDAO.getByName(roleName4);
            UserRole role5 = userRoleDAO.getByName(roleName5);

            roles.add(role1);
            roles.add(role3);
            roles.add(role5);

            User user = userDAO.getByName(userName1);
            user.setUserRoles(roles);
            // Int lazy collection
            role1.getUsers().size();
            role2.getUsers().size();
            role3.getUsers().size();
            role4.getUsers().size();
            role5.getUsers().size();
            transactionCommit();

            user = userDAO.getByName(userName1);
            assertThat(user.getUserRoles().contains(role1), is(true));
            assertThat(user.getUserRoles().contains(role2), is(false));
            assertThat(user.getUserRoles().contains(role3), is(true));
            assertThat(user.getUserRoles().contains(role4), is(false));
            assertThat(user.getUserRoles().contains(role5), is(true));


            assertThat(role1.getUsers().contains(user), is(true));
            assertThat(role2.getUsers().contains(user), is(false));
            assertThat(role3.getUsers().contains(user), is(true));
            assertThat(role4.getUsers().contains(user), is(false));
            assertThat(role5.getUsers().contains(user), is(true));
        }
        {   //6
            User user = userDAO.getByName(userName1);
            UserRole role1 = userRoleDAO.getByName(roleName1);
            UserRole role2 = userRoleDAO.getByName(roleName2);
            UserRole role3 = userRoleDAO.getByName(roleName3);
            UserRole role4 = userRoleDAO.getByName(roleName4);
            UserRole role5 = userRoleDAO.getByName(roleName5);

            transactionBegin();
            userDAO.makeTransient(userDAO.getById(user.getId(), false));
            transactionCommit();

            transactionBegin();
            // Roles should not ne deleted if the user is deleted
            assertThat("Not found for roleID " + role1.getId(), userRoleDAO.getById(role1.getId(), false), notNullValue());
            assertThat("Not found for roleID " + role2.getId(), userRoleDAO.getById(role2.getId(), false), notNullValue());
            assertThat("Not found for roleID " + role3.getId(), userRoleDAO.getById(role3.getId(), false), notNullValue());
            assertThat("Not found for roleID " + role4.getId(), userRoleDAO.getById(role4.getId(), false), notNullValue());
            assertThat("Not found for roleID " + role5.getId(), userRoleDAO.getById(role5.getId(), false), notNullValue());
            transactionCommit();
        }
        {   // 7
            // User is deleted in step 5
            userRoleDAO.makeTransient(userRoleDAO.getByName(roleName1));
            userRoleDAO.makeTransient(userRoleDAO.getByName(roleName2));
            userRoleDAO.makeTransient(userRoleDAO.getByName(roleName3));
            userRoleDAO.makeTransient(userRoleDAO.getByName(roleName4));
            userRoleDAO.makeTransient(userRoleDAO.getByName(roleName5));
        }

    }
    //################################   END JPA TEST RELATIONS ############################# //

    //################################   BEGIN TEST HELPER METHODS ############################# //


    private void deleteRolesRemote() {
        UserRoleDAORemote userRoleDAORemote = ServiceUtil.getUserRoleDAORemote();
        for (UserRole role : userRoleDAORemote.findAll()) {
            if (role.getName().startsWith(userNameSuffix)) {
                userRoleDAORemote.makeTransient(role);
            }
        }
    }
    private List<UserRole> createUserRolesRemote() {
        List<UserRole> created = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String roleName = userNameSuffix + "Role" + i;
            UserRole byName = ServiceUtil.getUserRoleDAORemote().getByName(roleName);
            if (byName == null) {
                byName = ServiceUtil.getUserRoleDAORemote().makePersistent(createUserole(roleName));
            }
            created.add(byName);
        }

        return created;
    }

    public void deleteUsers() {
        for (User user : findAllTestObjects()) {
            userDAO.makeTransient(user);
        }
    }

    private List<User> findAllTestObjects() {
        List<User> all = userDAO.findAllWithSecrets();
        List<User> result = new ArrayList<>();
        for (User user : all) {
            if (user.getName().startsWith(userNameSuffix)) {
                result.add(user);
            }
        }
        return result;
    }

    public List<User> createUsers() {
        List<User> result = new ArrayList<>();
        for (String userName : tmpUserNames) {
            User byName = userDAO.getByName(userName);
            if (byName == null) {
                byName = userDAO.makePersistent(createUser(userName));
            }
            result.add(byName);
        }
        return result;
    }

    public User createAndPersitUser(String userName) {
        User user = userDAO.getByName(userName);
        if (user == null) {
            user = createUser(userName);
            user = userDAO.makePersistent(user);
        }
        return user;
    }

    public UserRole createAndPersitUserRole(String roleName) {
        UserRole role = userRoleDAO.getByName(roleName);
        if (role == null) {
            role = createUserole(roleName);
            role = userRoleDAO.makePersistent(role);
        }
        return role;
    }

    public String getUserNameSuffix() {
        return userNameSuffix;
    }





    private UserRole createUserole(String roleName) {
        UserRole role = new UserRole();
        role.setName(roleName);
        return role;
    }

    public User createUser(String name) {
        User user = new User();
        user.setName(name);
        user.setLocale(Locale.ENGLISH);
        user.setFirstName("FirstName" + name);
        user.setLastName("LastName" + name);
        user.setEmailAddress("1234@23.de");
        user.setPassword(name);
        return user;
    }
}
