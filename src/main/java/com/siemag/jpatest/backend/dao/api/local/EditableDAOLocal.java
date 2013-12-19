package com.siemag.jpatest.backend.dao.api.local;

import com.siemag.jpatest.backend.dao.api.remote.EditableDAORemote;
import com.siemag.jpatest.backend.model.Editable;
import org.hibernate.criterion.Criterion;

import javax.ejb.Local;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Local
public interface EditableDAOLocal extends EditableDAORemote {


    /**
     * Returns the entity that fits to given restrictions (unique result)
     *
     * @param aClass     - Class restriction
     * @param aCriterias - Criterion restrictions
     * @return Editable - entity
     */
    public Editable getEditableByCriteria(Class<? extends Editable> aClass, Criterion... aCriterias);

    /**
     * Returns all entities by given HQL statement.
     *
     * @param aParCriteriaString String HQL statement.
     * @return Collection <Editable> entities
     */
    public Collection<Editable> findCollectionByHQLCriteriaString(String aParCriteriaString);

    /**
     * Returns all entities by given HQL statement (special case BETWEEN with Date format).
     *
     * @param aParCriteriaString String HQL statement.
     * @param dStart             Date - start date
     * @param dEnd               Date - end   date
     * @return Collection <Editable> entities or <tt>empty</tt>
     */
    public Collection<Editable> findCollectionByHQLCriteriaString(String aParCriteriaString, Date dStart, Date dEnd);


    /**
     * Finds all objects by given HIBERNATE <tt>Criterion</tt> objects.
     *
     * @param aParClass         Class<T>              - narrows entity type
     * @param aParCriteriaArray Array<Criterion>      - restrictions
     * @return Collection<T>         - entities or <tt>empty</tt>
     */
    public <T extends Editable> Collection<T> findEditablesByCriteria(
            Class<T> aParClass, Criterion... aParCriteriaArray);

    public <T extends Editable> T getByName(Class<? extends Editable> clazz, String name);

    public <T extends Editable> T getByName(Class<? extends Editable> clazz, String name, boolean cacheable);

    /**
     * Finds all objects by given HIBERNATE <tt>Criterion</tt> objects.
     *
     * @param aParClass              Class<T> - narrows entity type
     * @param aParCriteriaCollection Collection<Criterion> - restrictions
     * @return Collection - entities or <tt>empty</tt>
     */
    public <T extends Editable> Collection<T> findByCriteria(Class<T> aParClass, Collection<Criterion> aParCriteriaCollection);


    /**
     * Save or update given object into database.
     *
     * @param anEditable <Editable> - object
     * @return <Editable> - persistent entity
     */
    public <T extends Editable> T makePersistent(T anEditable);

    <T extends Editable> T mergeIfNeeeded(T editable);

    public <T extends Editable> T makePersistent(T anEditable, boolean partly);

    /**
     * Remove given object from database.
     *
     * @param anEditable <Editable> - object
     */
    public void makeTransient(Editable anEditable);

    /**
     * Delete the list of Editables in bulk mode by creating a delete from statement.<br>
     * This is faster then use {@link #makeTransient(Editable)} for every editable. <br>
     * Before use this delete operation you must be sure that the relations deleted first. Otherwise you'll get an Exception that there's depended data.
     *
     * @param editables The list of editables that should be delete. Be sure that the types of the list is allays the same. <br>
     *                  So be careful with inheritance.
     * @return A List of deleted ids.
     */
    public <T extends Editable> List<Long> makeTransient(List<T> editables);

    /**
     * Find a set of objects by using it's ids. This is faster than do a select for each entity.
     *
     * @param clazz Type of the resultSet
     * @param ids   ids that should be found
     * @return The founded entities.
     */
    public <T extends Editable> List<T> getByIds(Class<T> clazz, List<Long> ids);

    /**
     * Persist a List of editables in Bulk mode.
     *
     * @param editableles The list of editables that should be update or persist. <br>
     *                    Be sure that the types of the list is allways the same. <br>
     *                    All the entities in the list editableles will be detached after this operation. Even if you use the local interface. <br>
     *                    If you need the entites for further work then use {@link #getByIds(Class, java.util.List)} for attach them back.
     *                    So be careful with inheritance.
     * @return A List of object that be persisted.
     */
    public <T extends Editable> List<Long> makePersistent(List<T> editableles);

    public <T extends Editable> List<Long> makePersistent(List<T> editableles, Integer bulkFlushSize);

    public <T extends Editable> T refreshIfNecessary(T editable);

    <T extends Editable> List<Long> makeTransient(List<T> editables, Integer bulkFlushSize);

//  public void flush();

    public <T extends Editable> List<T> refreshIfNecessary(List<T> editables);


    //  public  Editable getEditableById(Class aParClass, Long aParEditableId, boolean aParIsToBeLocked);
    //
    //  public Collection findCollectionBySQLCriteriaString(String aParCriteriaString);
    //
    //  public Editable[] findFirstEditableByCriteriaString(boolean isSQLString, String aParCriteriaString) ;
}