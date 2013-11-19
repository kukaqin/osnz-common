package nz.net.osnz.common.scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If the scanner is called, then it will work its way through the classpath, firing off events for each directory, jar file or war underlay that it finds.
 * <p/>
 * If it finds a test-classes path, it will also go and attempt to derive the application's name, and load in the default set of properties for that application as well. It provides
 * a loader for non-scanner based applications to be able to load system properties from known locations. If it encounters a special property SCANNER_COMMON_CONFIG_FILES it will attempt to load those
 * properties into system properties as well.
 */
public class MultiModuleConfigScanner {

    private static final Logger log = LoggerFactory.getLogger(MultiModuleConfigScanner.class);

    public static final String SCANNER_DEVMODE = "scanner.devmode";
    public static final String SCANNER_APPNAME = "scanner.appname";
    public static final String SCANNER_APPPATH = "scanner.apppath";
    public static final String SCANNER_BASEPATH = "scanner.basepath";
    public static final String SCANNER_HOME = "scanner.home";
    private static final String SCANNER_COMMON_CONFIG_FILES = "scanner.commonConfigFiles";

    private static final String LOG_PREFIX = "system property loader: ";
    private static final String WAR_PROPERTIES = "war.properties";
    private static final String RWAR_PROPERTIES = ".webdev";

    public static boolean propertiesLoaded = false;

    public static final Map<String, ArtifactVersion> classpathGavs = new HashMap<String, ArtifactVersion>();

    private static void checkPath(String path, Notifier notifier, String matcher, String... suffixes) throws Exception {

        if (path.endsWith(matcher)) {

            String base = getSubPath(path, matcher);

            File baseFile = new File(base);

            File pomCheck = new File(baseFile, "pom.xml");

            if (pomCheck.exists() && classpathGavs.get(pomCheck.getAbsolutePath()) == null) {                
                parseGAVfromPOM(pomCheck);
            }

            for (String suffix : suffixes) {
                File suffixFile = null;

                if (baseFile.isDirectory()) {
                    suffixFile = new File(baseFile, suffix);
                }

                if (suffixFile != null && suffixFile.exists()) {
                    notifier.dir(suffixFile.toURI().toURL());
                }
            }
        }
    }

    private static String getSubPath(String path, String matcher) {
        return path.substring(0, path.length() - matcher.length());
    }


    public static String appName() {
        return System.getProperty(SCANNER_APPNAME);
    }

    public static String appPath() {
        return System.getProperty(SCANNER_APPPATH);
    }

    public static boolean inDevMode() {
        return System.getProperty(SCANNER_DEVMODE) != null;
    }

    public static void scan(Notifier notifier) throws Exception {
        if (!(MultiModuleConfigScanner.class.getClassLoader() instanceof URLClassLoader)) {
            throw new RuntimeException("Only the URL classloader is supported.");
        }

        URLClassLoader cl = (URLClassLoader) MultiModuleConfigScanner.class.getClassLoader();

        scan(cl, notifier);
    }

    // allows for testing
    public static void scan(URLClassLoader cl, Notifier notifier) throws Exception {

        for (URL url : cl.getURLs()) {

            String urlPath = url.getPath();

            if (urlPath.endsWith(".jar")) {
                notifier.jar(url);
            } else if (urlPath.endsWith(".war!/WEB-INF/classes/")) {  // this is the war file
                urlPath = urlPath.substring(0, urlPath.indexOf("!"));
                notifier.jar(new URL(urlPath));
            } else if (urlPath.endsWith("-underlay.war")) {
                notifier.underlayWar(url);
            } else {
                try {

                    // Maven repositroy occasionally has a target directory in it, its an invalid path, so ignore it
                    if (urlPath.contains(".m2")) {
                        continue;
                    }

                    checkPath(urlPath, notifier,
                            "/target/classes/",
                            "src/main/webapp",
                            "src/main/resources",
                            "src/main/resources/META-INF/resources/"
                    );

                    // test-classes is special because it means it is the artifact that we are running in (our tests). Only one is ever on the classpath.
                    String testClassPath = "/target/test-classes/";

                    checkPath(urlPath, notifier, testClassPath,
                        "src/test/webapp",
                        "src/test/resources"
                    );

                    if (urlPath.endsWith(testClassPath) ) {
                        String basePath = getSubPath(urlPath, testClassPath);

                        if (appName() == null) {
                            String[] subpath = basePath.split("/");

                            if (subpath.length > 2) {
                                String appname = "checkout".equals(subpath[subpath.length - 1]) && "target".equals(subpath[subpath.length - 2]) ? subpath[subpath.length - 3] : subpath[subpath.length - 1]; // release workaround

                                System.setProperty(SCANNER_APPNAME, appname);
                                System.setProperty(SCANNER_APPPATH, basePath + "/src/test/resources");
                                System.setProperty(SCANNER_BASEPATH, basePath);
                            }
                        }
                    }
                } catch (MalformedURLException mfe) {
                    throw new RuntimeException("File invalid " + urlPath, mfe);
                }
            }
        }

        if (!propertiesLoaded && appName() != null) {
            loadIntoSystemProperties(appName(), appPath());
            propertiesLoaded = true;
        }
    }


    public static void loadIntoSystemProperties(File appProps, String scannerHome) {

        if (appProps.exists() && appProps.isFile()) {

            Properties p = new Properties();

            try {
                p.load(new FileReader(appProps));

                System.getProperties().putAll(p);

                log.info(LOG_PREFIX + "loaded {}", appProps.getAbsolutePath());

                if (p.getProperty(SCANNER_COMMON_CONFIG_FILES) != null) {
                    String[] files = p.getProperty(SCANNER_COMMON_CONFIG_FILES).split(",");
                    for (String fName : files) {
                        loadIntoSystemProperties(new File(fName.trim().replace("$home", scannerHome)), scannerHome);
                    }
                }
            } catch (IOException e) {
                log.error(LOG_PREFIX + "unable to load {}", appProps.getAbsolutePath());
            }
        } else {
            log.debug(LOG_PREFIX + "none found at {}", appProps.getAbsolutePath());

        }
    }

    public synchronized static void loadIntoSystemProperties() {
        try {
            MultiModuleConfigScanner.scan(new Notifier() {
                @Override
                public void underlayWar(URL url) throws Exception {
                }

                @Override
                public void jar(URL url) throws Exception {
                }

                @Override
                public void dir(URL url) throws Exception {
                }
            });
        } catch (Exception ex) {
            log.error("Unexpected exception while setting up system properties", ex);
        }
    }


    public synchronized static void loadIntoSystemProperties(String appName) {
        loadIntoSystemProperties(appName, null);
    }

    public synchronized static void loadIntoSystemProperties(String appName, String appPath) {
        String scannerHome = System.getProperty(SCANNER_HOME, System.getenv("WAR_SCANNER_HOME"));

        if (scannerHome == null) {
            scannerHome = System.getProperty("user.home") + File.separator + RWAR_PROPERTIES;
        }


        if (appPath != null) {
            File parent = new File(appPath);
            if (parent.isFile()) {
                loadIntoSystemProperties(parent, scannerHome);
            }
            else {
                loadIntoSystemProperties(new File(parent, WAR_PROPERTIES), scannerHome);
            }
        }

        if (appName != null) {
            File home = new File(scannerHome + File.separator + appName + File.separator + WAR_PROPERTIES);
            loadIntoSystemProperties(home, scannerHome);
        }
    }

    private static Element findChildElement(Node node, String element) {
        for(Node child = node.getFirstChild(); child != null;) {
            if (child.getNodeName().equals(element)) {
                return (Element)child;
            }
            child = child.getNextSibling();
        }

        return null;
    }

    private static Element findChild(Node node, String element) {
        Element el = findChildElement(node, element);
        // did we find it under "project"? if so, return it
        if (el != null) return el;

        // it wasn't specified which means its inherited from the parent, so go get that one
        Element parent = findChild(node, "parent");

        return (parent != null) ? findChildElement(parent, element) : null;
    }

    public static void parseGAVfromPOM(File projDir) {

        if (projDir.getName().equals("pom.xml")) {
            projDir = projDir.getParentFile();
        }

        File pom = new File(projDir, "pom.xml");

        if (!pom.exists()) {
            throw new RuntimeException("Unable to find pom.xml in " + projDir.getAbsolutePath());
        }

        Document doc = null;

        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException("Cannot parse  " + projDir.getAbsolutePath(), e);
        }

        doc.getDocumentElement().normalize(); // apparently recommended
        NodeList projectNode = doc.getElementsByTagName("project");

        if (projectNode.getLength() < 1) {
            throw new RuntimeException("Improperty formed pom.xml file in " + projDir.getAbsolutePath());
        }

        ArtifactVersion gav = new ArtifactVersion();

        Node project = projectNode.item(0);

        gav.groupId = findChild(project, "groupId").getTextContent();
        gav.artfiactId = findChild(project, "artifactId").getTextContent();
        gav.version = findChild(project, "version").getTextContent();

        log.info("loaded pom: " + gav.groupId + ":" + gav.artfiactId + ":" + gav.version + " as key: " + projDir.getAbsolutePath() );

        classpathGavs.put(projDir.getAbsolutePath(), gav);
    }
}