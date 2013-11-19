package nz.net.osnz.common.jsresource;

import org.springframework.stereotype.Component;

@Component("i18nClientMessageMap")
class I18nClientMessageMap extends I18nMessageMapService {

    private static final String DEFAULT_BUNDLE = "classpath*:/i18n/messages/client_*.properties";

    @Override
    protected String getResourceMatchingPattern() {
        return DEFAULT_BUNDLE;
    }

}