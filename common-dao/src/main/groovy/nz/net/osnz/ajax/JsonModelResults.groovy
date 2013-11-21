package nz.net.osnz.ajax

import groovy.transform.CompileStatic
import nz.net.osnz.model.BaseEntityBean

/**
 * @author Kefeng Deng
 */
@CompileStatic
class JsonModelResults {

  AjaxStatus status

  List<BaseEntityBean> data

  public JsonModelResults(AjaxStatus status, List<BaseEntityBean> data) {
    this.status = status
    this.data = data
  }

}
