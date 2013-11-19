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
 * @author Kefeng Deng
 */
@Component("extTemplateResource")
public class ExtTemplateResource implements ApplicationResource {

    private final static Logger LOG = LoggerFactory.getLogger(ExtTemplateResource.class);

    /**
     * Template service injected here
     */
    @Inject
    private ExtTemplates extTemplates;

    /**
     * @return the global resource scope
     */
    @Override
    public List<ResourceScope> getResourceScope() {
        List<ResourceScope> resourceList = new ArrayList<ResourceScope>();
        resourceList.add(ResourceScope.Ext);
        return resourceList;
    }

    /**
     * @return a map of angular templates
     */
    @Override
    public Map<String, Object> getResourceMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("extTemplates", extTemplates.getExtTemplates());
        return map;
    }

    @PostConstruct
    public void init() {
        LOG.debug( String.format("ExtTemplateResource status : %s", extTemplates != null) );
    }

}
