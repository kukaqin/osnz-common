package nz.net.osnz.common.jsresource;

import nz.ac.auckland.util.JacksonHelperApi;
import nz.net.osnz.common.scanner.MultiModuleConfigScanner;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.*;

@Component("angularTemplateService")
class AngularTemplateService implements AngularTemplates {

    private static final Logger log = LoggerFactory.getLogger(AngularTemplateService.class);

    /**
     * Resource scanner matcher for angular templates
     */
    private static final String ANGULAR_TEMPLATES = "classpath*:**/*.html";

    private Map<String, String> templates;

    @Inject
    private JacksonHelperApi jacksonHelperApi;

    @Inject
    private ServletContext context;

    private boolean inDevMode;

    @PostConstruct
    public void init() {
        inDevMode = MultiModuleConfigScanner.inDevMode();
        log.debug("Current mode is DEV : " + inDevMode);
    }

    protected void collectResources(Set<String> paths, List<String> collected) {
        for (String path : paths) {
            if (path.endsWith(".html")) {
                collected.add(path);
            } else if (path.endsWith("/")) {
                collectResources(context.getResourcePaths(path), collected);
            }
        }
    }

    protected List<String> collectResources() {

        List<String> collected = new ArrayList<String>();

        collectResources(context.getResourcePaths("/angular/"), collected);

        return collected;
    }

    protected Map<String, String> collectAngularTemplates(List<String> resources) {

        Map<String, String> templateMapping = new HashMap<String, String>();

        for ( String url : resources ) {
            String mapping = url.substring(url.indexOf("/angular/") + "/angular".length());
            try {
                templateMapping.put(mapping, IOGroovyMethods.getText(context.getResourceAsStream(url)));
            } catch ( IOException ioe ) {
                log.error("Cannot read " + url);
            }

            log.debug( String.format("angular-template: %s\n%s ", url, templateMapping.get(url)) );
        }

        templates = templateMapping;

        return templateMapping;
    }

    @Override
    public Map<String, String> getAngularTemplates() {
        if (inDevMode || templates == null) {
            return collectAngularTemplates(collectResources());
        } else {
            return templates;
        }
    }

    @Override
    public String getAngularTemplatesAsJson() {
        return jacksonHelperApi.jsonSerialize(getAngularTemplates());
    }

    protected String getResourceMatchingPattern() {
        return ANGULAR_TEMPLATES;
    }

}