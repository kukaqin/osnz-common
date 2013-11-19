package nz.net.osnz.common.jawr

import groovy.transform.CompileStatic
import net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource
import nz.net.osnz.common.scanner.MultiModuleConfigScanner
import nz.net.osnz.common.scanner.Notifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.jar.JarEntry
import java.util.jar.JarFile

@CompileStatic
class ConfigScanner implements ConfigPropertiesSource {

  private static final Logger log = LoggerFactory.getLogger(ConfigScanner)
  public static final String WEBAPP_DEV = MultiModuleConfigScanner.SCANNER_DEVMODE

  static class JawrFile {
    File file
    String expect
    long lastModified
    Properties properties
  }

  static class DuplicateJawrFileException extends RuntimeException {
    DuplicateJawrFileException(File f) {
      super("Duplicate JAWR configuration file detected on classpath at ${f.absolutePath}")
    }
  }

  private List<JawrFile> files = new ArrayList<>();
  private boolean initialized = false
  private Properties allProperties

  protected static void addJawrFile(List<JawrFile> jawrFiles, JawrFile jawrFile) {
    for(JawrFile f : jawrFiles) {
      if (jawrFile.expect == f.expect && jawrFile.file.name == f.file.name) {
        throw new DuplicateJawrFileException(jawrFile.file)
      }
    }
    jawrFiles.add(jawrFile)
  }

  protected static void checkJar(List<JawrFile> jawrFiles, URL url) {
    File f = new File(url.toURI())
    if (f.exists()) {
      JarFile jarFile = new JarFile(f)
      Iterator<JarEntry> it = jarFile.entries().iterator()
      while (it.hasNext()) {
        JarEntry entry = it.next()
        if ((entry.name.startsWith("META-INF/jawr-") ||
          entry.name.startsWith("WEB-INF/jawr-") ||
          entry.name.startsWith("META-INF/resources/WEB-INF/jawr-") ||
          entry.name.startsWith("META-INF/jawr.")) && entry.name.endsWith(".properties") && entry.size > 0) {
          JawrFile jawrFile = new JawrFile()
          jawrFile.file = f
          jawrFile.expect = entry.name
          jawrFile.lastModified = f.lastModified()
          jawrFile.properties = new Properties()
          jawrFile.properties.load(jarFile.getInputStream(entry))
          addJawrFile(jawrFiles, jawrFile)
        }
      }
      jarFile.close()
    }
  }

  protected static void checkDir(List<JawrFile> jawrFiles, URL url, String... expected) {
    for (String expect : expected) {
      File f = new File(url.toURI())
      File file = new File(f, expect)
      file.listFiles().each {File jFile ->
        if (jFile.name.startsWith("jawr-") && jFile.name.endsWith(".properties")) {
          JawrFile jawrFile = new JawrFile()
          jawrFile.file = jFile
          jawrFile.expect = null
          jawrFile.lastModified = jFile.lastModified()
          jawrFile.properties = new Properties()
          jawrFile.properties.load(jFile.newReader())

          addJawrFile(jawrFiles, jawrFile)
        }
      }
    }
  }

  protected void init() {
    MultiModuleConfigScanner.scan(new MultiModuleConfigScannerNotifier())

    amalgamateProperties()

    initialized = true

    StringBuilder sb = new StringBuilder()
    files.each { JawrFile file ->
      if (sb.size() > 0) sb.append(", ")
      if (file.expect)
        sb.append(file.expect)
      else
        sb.append(file.file.name)
    }
    log.debug("jawr: discovered ${sb.toString()}")
  }

  private void amalgamateProperties() {
    allProperties = new Properties()
    files.each { JawrFile file ->
      allProperties.putAll(file.properties)
    }
  }

  @Override
  Properties getConfigProperties() {
    if (!initialized)
      init()

    return allProperties
  }

  private boolean isDevMode() {
    return System.getProperty(WEBAPP_DEV) != null
  }

  private void reloadCheck() {
    boolean changed = false

    log.debug("jawr: reload check")

    files.each { JawrFile file ->
      if (file.lastModified < file.file.lastModified()) {

        if (file.expect == null) {
          log.info("reloading ${file.file.getAbsolutePath()}")
          file.properties = new Properties()
          file.properties.load(file.file.newReader())
        } else {
          log.info("reloading ${file.file.getAbsolutePath()}:${file.expect}")
          JarFile jarFile = new JarFile(file.file)
          JarEntry entry = jarFile.getJarEntry(file.expect)
          file.properties = new Properties()
          file.properties.load(jarFile.getInputStream(entry))
          jarFile.close()
        }

        changed = true
        file.lastModified = file.file.lastModified()
      }
    }

    if (changed)
      amalgamateProperties()
  }

  @Override
  boolean configChanged() {
    if (isDevMode()) {
      reloadCheck()
    } else {
      return false
    }
  }

  protected class MultiModuleConfigScannerNotifier implements Notifier {
    @Override
    void underlayWar(URL url) {
      checkJar(files, url)
    }

    @Override
    void jar(URL url) {
      checkJar(files, url)
    }

    @Override
    void dir(URL url) { //webapp dir or resources, src/test/webapp, src/test/resources, src/main/webapp, src/main/resources
      checkDir(files, url, "WEB-INF", "META-INF")
    }
  }

}
