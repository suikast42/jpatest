package com.siemag.jpatest.backend.dao.api.remote;

import com.siemag.jpatest.backend.model.Editable;
import org.hibernate.criterion.Criterion;

import javax.ejb.Remote;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;


@Remote
public interface EditableDAORemote {

    public Editable getById(Class<? extends Editable> clazz, Long anId, boolean aLock);


    public <T extends Editable> Collection<T> findEditablesByCriteria(
            Class<T> aParClass, Collection<Criterion> aParCriteriaCollection);


    Future<List<? extends Editable>> findAllFromSerivce(String serivceName);


    public Class<? extends Editable> getConcreteClassOfEditable(Class<? extends Editable> superClass, Long id);


    <E extends Editable> E initializeAndUnproxy(E editable);

}