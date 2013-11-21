package nz.net.osnz.helper

import groovy.transform.CompileStatic
import nz.net.osnz.form.BaseFormBean
import nz.net.osnz.model.BaseEntityBean


/**
 * @author Kefeng Deng
 */
@CompileStatic
class EntityHelper {

    /**
     * this class is not instantiable
     */
    private EntityHelper() {}

    /**
     * Copy all properties to entity object
     *
     * @param source - form object
     * @param target - entity object
     */
    public static <T extends BaseEntityBean, S extends BaseFormBean> void entityDecorate(final T target, final S source) {
        target.metaClass.properties.each { MetaProperty metaProperty ->
            if ( source.metaClass.hasProperty(source, metaProperty.name) && metaProperty.name != 'metaClass' && metaProperty.name != 'class' ) {
                metaProperty.setProperty(target, source.metaClass.getProperty(source, metaProperty.name))
            }
        }
    }

}
