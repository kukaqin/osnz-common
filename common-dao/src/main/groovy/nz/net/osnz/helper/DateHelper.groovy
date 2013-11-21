package nz.net.osnz.helper

import groovy.transform.CompileStatic
import nz.net.osnz.constant.AppConstant

/**
 * @author kdeng
 *
 *
 */
@CompileStatic
class DateHelper {

    /**
     * this class is not instantiable
     */
    private DateHelper() {}

    public static String display(Date date) {
        return date.format(AppConstant.DATE_FORMAT)
    }

}
