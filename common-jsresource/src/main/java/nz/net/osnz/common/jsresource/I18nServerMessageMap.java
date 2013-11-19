package nz.net.osnz.common.jsresource;

import org.springframework.stereotype.Component;

@Component("i18nServerMessageMap")
class I18nServerMessageMap  extends I18nMessageMapService {

  private static final String DEFAULT_BUNDLE = "classpath*:/i18n/messages/server_*.properties";

  @Override
  protected String getResourceMatchingPattern() {
    return DEFAULT_BUNDLE;
  }

}