package nz.net.osnz.common.jsresource;

import nz.ac.auckland.util.JacksonHelperApi;
import nz.net.osnz.common.scanner.SpringScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Expects messages to be in
 */
abstract class I18nMessageMapService extends SpringScanner implements I18nMessageMap {

    private static final Logger log = LoggerFactory.getLogger(I18nMessageMapService.class);

    @Inject
    private JacksonHelperApi jacksonHelperApi;

    /**
     * Extended message bundle instance
     */
//  private static final String DEFAULT_BUNDLE = "classpath*:/i18n/messages/**"
//  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH


    /**
     * Message map
     */
    private Map<String, String> s_messageMap = null;

    @PostConstruct
    public void init() {
        try {
            scanClassLoader();
            getMessagesMap(collectResources());
        } catch ( IOException ioe ) {
            log.error("Error when reading I18n messages", ioe);
        } catch ( Exception ex ) {
            log.error("Cannot initialise I18nMessage Map", ex);
        }
    }

    @Override
    public Map<String, String> getMessagesMap() {
        try {
            Map<String, String> messages = (s_messageMap == null || inDevMode) ?
                    getMessagesMap( collectResources() ) :
                    s_messageMap;
            return messages;
        } catch ( IOException ioe ) {
            log.error("Unexpected error", ioe);
        }
        return null;
    }

    /**
     * Return a map of messages
     *
     * @return the map with key/value being message-id/message-value
     */
    protected Map<String, String> getMessagesMap(Resource[] resources) {
        Map<String, String> messageMap = new HashMap<String, String>();

        for ( int idx=0; idx<resources.length; idx++ ) {
            Properties p = new Properties();
            Resource resource = resources[idx];

            try {
                if ( inDevMode && resource instanceof FileSystemResource) {
                    p.load( new FileReader(((FileSystemResource) resource).getPath()) );
                } else {
                    p.load(resource.getInputStream());
                }
            } catch ( Exception ex ) {
                log.error("Unable to load properties", ex);
            }

            for ( String key : p.stringPropertyNames()) {
                messageMap.put(key, p.getProperty(key));
            }

        }

        s_messageMap = messageMap;

        return messageMap;
    }

    @Override
    public String getMessagesMapAsJson() {
        return jacksonHelperApi.jsonSerialize(getMessagesMap());
    }

}