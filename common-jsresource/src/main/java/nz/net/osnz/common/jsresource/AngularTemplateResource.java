package nz.net.osnz.common.jsresource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application resource that exposes angular templates
 */
@Component("angularTemplateResource")
class AngularTemplateResource implements ApplicationResource {

    private final static Logger LOG = LoggerFactory.getLogger(AngularTemplateResource.class);

    /**
     * Template service injected here
     */
    @Inject
    private AngularTemplates angularTemplates;

    /**
     * @return the global resource scope
     */
    @Override
    public List<ResourceScope> getResourceScope() {
        List<ResourceScope> resourceList = new ArrayList<ResourceScope>();
        resourceList.add(ResourceScope.Angular);
        return resourceList;
    }

    /**
     * @return a map of angular templates
     */
    @Override
    public Map<String, Object> getResourceMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("angularTemplates", angularTemplates.getAngularTemplates());
        return map;
    }

    @PostConstruct
    public void init() {
        LOG.debug( String.format("AngularTemplateResources status : %s", angularTemplates != null) );
    }

}