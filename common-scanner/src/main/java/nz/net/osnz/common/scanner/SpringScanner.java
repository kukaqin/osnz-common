package nz.net.osnz.common.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class appears to get used at least twice - Spring scanning for varioous resources. We can be efficient about it.
 */
@SuppressWarnings("unused")
public abstract class SpringScanner {

    private static final Logger log = LoggerFactory.getLogger(SpringScanner.class);

    /**
     * Tells us what we are looking for
     * @return pattern to match, e.g. classpath*:/i18n/messages*.properties
     */
    protected abstract String getResourceMatchingPattern();

    protected boolean includeJars = true;

    protected boolean includeWarUnderlays = true;

    // .../resources/ directories
    protected boolean includeResources = true;

    // .../webapp/ directories
    protected boolean includeWebapps = true;

    protected ClassLoader springScannerClassLoader;

    protected boolean inDevMode;

    protected ClassLoader scanClassLoader() throws Exception {

        inDevMode = MultiModuleConfigScanner.inDevMode();

        if (inDevMode) {

            final List<URL> urls = new ArrayList<>();

            MultiModuleConfigScanner.scan(new Notifier() {
                @Override
                public void underlayWar(URL url) throws Exception {
                    if (includeWarUnderlays) {
                        urls.add(url);
                    }
                }

                @Override
                public void jar(URL url) throws Exception {
                    if (includeJars) {
                        urls.add(url);
                    }
                }

                @Override
                public void dir(URL url) throws Exception {
                    if (url.toString().endsWith("/webapp/") && includeWebapps) {
                        urls.add(url);
                    }
                    if (url.toString().endsWith("/resources/") && includeResources) {
                        urls.add(url);
                    }
                }
            });

            URL[] warUrls = new URL[urls.size()];
            urls.toArray(warUrls);

            springScannerClassLoader = new URLClassLoader(warUrls, null);
        } else {
            // default to running not in dev mode (war is on classpath)
            springScannerClassLoader = this.getClass().getClassLoader();
        }

        return springScannerClassLoader;
    }


    /**
     * Get an array of angular template view resources
     * @param cl is the container's http request
     * @return an array of resources we want to output
     */
    protected Resource[] collectResources(ClassLoader cl) throws IOException {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);

        Resource[] templates = resolver.getResources( getResourceMatchingPattern() );

        return templates;
    }

    protected Resource[] collectResources() throws IOException {
        return collectResources(springScannerClassLoader);
    }
}