package nz.net.osnz.dao

import groovy.transform.CompileStatic
import nz.net.osnz.model.BaseEntityBean
import org.hibernate.criterion.Criterion

/**
 * @author Kefeng Deng
 */
@CompileStatic
public interface EntityService {

    BaseEntityBean findById(Class clazz, String id)

    List<?> findBy(Class<?> clazz, Closure c)

    Map safePersistSaveOrUpdate(BaseEntityBean entity)

    Map safePersistDelete(BaseEntityBean entity)

}
