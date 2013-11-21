package nz.net.osnz.ajax

import groovy.transform.CompileStatic
import nz.net.osnz.form.BaseFormBean

/**
 * @author Kefeng Deng
 *
 */
@CompileStatic
class JsonFormResult {

    AjaxStatus status

    BaseFormBean data

    public JsonFormResult(AjaxStatus status, BaseFormBean data) {
        this.status = status
        this.data = data
    }

}
