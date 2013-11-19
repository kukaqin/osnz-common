package nz.net.osnz.common.jsresource;

import java.util.List;
import java.util.Map;

/**
 * This interface describes an application resource. Resources are shared
 * between the server and client via a servlet that poses as a javascript.
 */
public interface ApplicationResource {
  /**
   * @return the type of scope this application resource belongs to
   */
  public List<ResourceScope> getResourceScope();

  /**
   * @return a map with the resource information in it
   */
  public Map<String, Object> getResourceMap();
}