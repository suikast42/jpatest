package com.siemag.jpatest.backend.model;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "APP_USERROLE")
@Inheritance(strategy = InheritanceType.JOINED)

@NamedQueries(
        value = {
                @NamedQuery(name = UserRole.NQ_ALL, query = "select o from UserRole o where secret=false order by o.name "),
                @NamedQuery(name = UserRole.NQ_ALL_WITH_SECRET, query = "select o from UserRole o  order by o.name ")
        })
public class UserRole extends Editable {
    public static final String NQ_ALL = "UserRole.all";
    public static final String NQ_ALL_WITH_SECRET = "UserRole.withSecrets.all";

    private static final long serialVersionUID = 244634620516452317L;

    @Column(name = "DESCRIPTION", length = 80)
    private String description;

    @Column(name = "ISBLOCKED", length = 1, nullable = false)
    private boolean blocked;

    @Column(name = "SECRET", length = 1, nullable = false)
    private boolean secret;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(name = "USERROLE_USER", joinColumns = @JoinColumn(name = "USERROLE_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    @ForeignKey(name = "USER_ROLE_ID")
    private Set<User> users = new HashSet<>();


    /**
     * default constructor
     */
    public UserRole() {
        setSecret(false);
        setName("Default");
        setDefaults();
    }

    public void setDefaults() {
        setCreationTime(new Date());
        setLastChangeTime(new Date());
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.blocked = isBlocked;
    }

    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }


    public Set<User> getUsers() {
        return Collections.unmodifiableSet(users);
    }


    public void addUsers(Collection<User> users) {
        for (User userRole : users) {
            addUser(userRole);
        }
    }

    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
            user.addUserRole(this);
        }
    }

    public void removeUsers(Collection<User> pUsers) {
        for (User currentUser : pUsers) {
            removeUser(currentUser);
        }
    }

    public void removeAllUsers() {
        removeUsers(getUsers());
    }

    public void setUsers(Collection<User> users) {
        for (User user :  new HashSet<>(this.users)) {
            user.removeUserRole(this);
        }
        this.users.clear();

        for (User user : users) {
            addUser(user);
        }
    }

    public void removeUser(User user) {
        if (users.contains(user)) {
            users.remove(user);
            user.removeUserRole(this);
        }
    }


}
