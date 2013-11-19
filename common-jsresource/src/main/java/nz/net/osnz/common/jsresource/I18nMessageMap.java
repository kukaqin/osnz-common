package nz.net.osnz.common.jsresource;

import java.util.Map;

public interface I18nMessageMap {
  
  public Map<String, String> getMessagesMap();

  public String getMessagesMapAsJson();

}