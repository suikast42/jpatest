package com.siemag.jpatest.backend.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * <pre>
 * --------------------------------------------------------------------------------------------------------------------
 * --------------------------------------------------------------------------------------------------------------------
 * History:
 * --------------------------------------------------------------------------------------------------------------------
 * Key: (usryyyymmdd) | Description
 * --------------------------------------------------------------------------------------------------------------------
 * kho27.04.2007        | Reference <tt>UserRole</tt> must be <tt>transient</tt> (ClassNotFoundException)
 * ----------------------------------------------------------------------------------------------------
 * </pre>
 */

@Entity
@Table(name = "APP_USER")
@NamedQueries(
        value = {
                @NamedQuery(name = User.NQ_ALL, query = "select o from User o where secret=false order by o.name "),
                @NamedQuery(name = User.NQ_ALL_WITH_SECRET, query = "select o from User o order by o.name "),
                @NamedQuery(name = User.NQ_BYNAME_AND_PASSWD, query = "select o from User o  where o.name = :name and o.password = :password "),
                @NamedQuery(name = User.NQ_UNBLOCKED_BY_NAME, query = "select o from User o  where o.name = :name and o.blocked=false ")
        })

@Cacheable
public class User extends Editable {
    public static final String NQ_ALL = "User.all";
    public static final String NQ_UNBLOCKED_BY_NAME = "User.UNBLOCKED_BY_NAME";
    public static final String NQ_BYNAME_AND_PASSWD = "User.by.name.and.passwd";
    public static final String NQ_ALL_WITH_SECRET = "User.withsecret.all";

    private static final long serialVersionUID = 1328270627488728530L;


    @Column(name = "BLOCKED", nullable = false, length = 1)
    private boolean blocked;

    @Column(name = "PASSWORD", length = 80)
    @Size(min = 5)
    @NotNull
    private String password;

    @Column(name = "LASTNAME", length = 40)
    @Size(min = 1)
    @NotNull
    private String lastName;

    @Column(name = "FIRSTNAME", length = 40, insertable = true, updatable = true)
    @Size(min = 1)
    private String firstName;

    @Column(name = "EMAILADDRESS", length = 40)
    @Email
    @NotNull
    private String emailAddress;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    @ForeignKey(name = "USER_ID")
    private Set<UserRole> userRoles = new HashSet<>();

    @Column(name = "LOCALE", nullable = false, insertable = true)
    private Locale locale;

    @Column(name = "SECRET", length = 1, nullable = false)
    private boolean secret;

    /**
     * default constructor
     */
    public User() {
        setDefaults();
    }

    public void setDefaults() {
        setCreationTime(new Date());
        setLastChangeTime(new Date());
        setName("Default");
        setBlocked(false);
        setSecret(false);
    }

    public boolean isUserInRole(String roleName) {
        for (UserRole role : getUserRoles()) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }


    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }


    public Set<UserRole> getUserRoles() {
        return Collections.unmodifiableSet(userRoles);
    }

//    public void setUserRoles(Collection<UserRole> userRoles) {
//        ManyToManyUtil.wireCollection(this,userRoles, User.class, UserRole.class);
//    }

    public void addUserRole(UserRole userRole) {
        if (!userRoles.contains(userRole)) {
            userRoles.add(userRole);
            userRole.addUser(this);
        }
    }

    public void addUserRoles(Collection<UserRole> userRole) {
        for (UserRole currentRole : userRole) {
            addUserRole(currentRole);
        }
    }

    public void removeUserRole(UserRole userRole) {
        if(userRoles.contains(userRole)){
            userRoles.remove(userRole);
            userRole.removeUser(this);
        }
    }

    public void setUserRoles(Collection<UserRole> userRoles) {
        for (UserRole role : new HashSet<>(this.userRoles)) {
            role.removeUser(this);
        }
        this.userRoles.clear();

        for (UserRole role : userRoles) {
            addUserRole(role);
        }
    }


    public void removeUserRoles(Collection<UserRole> userRole) {
        for (UserRole currentRole : userRole) {
            removeUserRole(currentRole);
        }
    }

    public void removeUserRoles() {
        removeUserRoles(new ArrayList<>(getUserRoles()));
    }



    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }




    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    @Override
    public String toString() {
        String superToString = super.toString();
        return new ToStringBuilder(this).append(superToString).append("Firstname ", firstName).append("LastName " + lastName)
                .toString();
    }
}
