package nz.net.osnz.ajax

import groovy.transform.CompileStatic
import nz.net.osnz.model.BaseEntityBean

/**
 * @author Kefeng Deng
 */
@CompileStatic
class JsonModelResult {

    AjaxStatus status

    BaseEntityBean data

    public JsonModelResult(AjaxStatus status, BaseEntityBean data) {
        this.status = status
        this.data = data
    }

}
