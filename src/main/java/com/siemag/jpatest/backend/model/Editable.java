package com.siemag.jpatest.backend.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * @author num
 *         <p/>
 *         The abstract class Editable is the top of the hierarchy. It contains all methods and fields necessary within the whole model.
 */
@MappedSuperclass
public abstract class Editable implements Serializable {

    private static final long serialVersionUID = -3990491597756443462L;



    @Id
    //  @SequenceGenerator(name="hibernate_sequence",initialValue=1,allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 255, unique = true)
    @NotNull
    @Size(min = 1, max = 255)
    @Index(name = "IDX_NAME")
    private String name;

    @Column(name = "LASTCHANGETIME", nullable = false, length = 11)
    @NotNull
    private Date lastChangeTime;

    @Column(name = "CREATIONTIME", nullable = false, length = 11)
    @NotNull
    private Date creationTime;


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Date getLastChangeTime() {
        return lastChangeTime;
    }

    public void setLastChangeTime(Date lastChangeTime) {
        this.lastChangeTime = lastChangeTime;
    }

    public Date getCreationTime() {
       return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }


    /**
     * @return String consisting of id, name, and creationTime, the same values which are also used for
     * equals {@link #equals(Object)}
     * and hashCode {@link #hashCode()}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("Id", getId()).append("name", getName()).append("creationTime", getCreationTime())
                .toString();
    }

    /**
     * @return int consisting of the hashCode of the ID if there is one, or the hashCodes of 'creationTime' and 'name',
     * which should be sufficient for this case
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        //    int result = super.hashCode();
        int result = 0;
        if (getId() != null) {
            result = prime * getId().hashCode();
        } else {
            result = prime * ((getCreationTime() == null) ? 0 : getCreationTime().hashCode());
            //      result = prime * result + ((getCreationTime() == null) ? 0 : getCreationTime().hashCode());
            result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        }
        return result;
    }

    /**
     * @return boolean if object handed over equals this
     * There are some specialities in this equals method.
     * It does neither compare super not the classes itself, it uses the 'Hibernate.getClass'-method
     * in order to find out the real modell class, even if the class handed over is a Hibernate proxy in case of lazy loading.
     * It first compares the IDs, which are unique. An object must have an ID except for one situation, if it was not persisted yet,
     * it did not yet receive an ID. Therefore, in case the ID is null, the other values are compared.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        //    if (!super.equals(obj))
        //      return false;
        //    if (getClass() != obj.getClass()) {
        if (Hibernate.getClass(this) != Hibernate.getClass(obj)) {
            return false;
        }
        if (!(obj instanceof Editable)) {
            return false;
        }
        Editable other = (Editable) obj;
        //    if (getId() == null) {
        //      if (other.getId() != null) {
        //        return false;
        //      }
        //    } else if (!getId().equals(other.getId())) {
        //      return false;
        //    }
        if (getId() != null && getId().equals(other.getId())) {
            return true;
        }
        if (getCreationTime() == null) {
            if (other.getCreationTime() != null) {
                return false;
            }
        } else if (!getCreationTime().equals(other.getCreationTime())) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }




}
