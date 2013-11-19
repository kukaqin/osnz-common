package nz.net.osnz.common.jsresource;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The internationalization messages map
 */
@Component("i18nClientMessagesResource")
class I18nClientMessagesResource implements ApplicationResource {

    /**
     * Messages map bound here
     */
    @Inject
    private I18nClientMessageMap messageMap;

    /**
     * @return the global resource scope
     */
    @Override
    public List<ResourceScope> getResourceScope() {
        List<ResourceScope> resourceList = new ArrayList<ResourceScope>();
        resourceList.add(ResourceScope.Global);
        resourceList.add(ResourceScope.Ext);
        resourceList.add(ResourceScope.Angular);
        return resourceList;
    }

    /**
     * @return a map with all internationalization data in it
     */
    @Override
    public Map<String, Object> getResourceMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(I18nConstants.I18N_RESOURCE_MAP_KEY, messageMap.getMessagesMap());
        return map;
    }
}