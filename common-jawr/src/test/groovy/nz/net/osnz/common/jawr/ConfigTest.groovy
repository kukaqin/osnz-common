package nz.net.osnz.common.jawr

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class ConfigTest {


  @Test
  public void jarScanner() {
    File f = new File("src/test/resources/samplejar.jar")

    if ( f.exists() ) {
      List<ConfigScanner.JawrFile> files = new ArrayList<>()
      ConfigScanner.checkJar(files, f.toURI().toURL())

      assert files.size() == 1
      assert files.get(0).expect == 'META-INF/jawr-test.properties'
      assert files.get(0).properties.getProperty('shucks.test') == '1'
    }
  }

  private File findResourcesDir() {
    File f = new File("src/test/resources")

    if (!f.exists())
      throw new RuntimeException("You are running the tests in the IDe with the wrong working directory")

    return f
  }

  @Test
  public void cpScanner() {
    List<ConfigScanner.JawrFile> files = new ArrayList<>()
    ConfigScanner.checkDir(files, findResourcesDir().toURI().toURL(), "META-INF")

    assert files.size() == 2
    ConfigScanner.JawrFile file = files.find { ConfigScanner.JawrFile jawrFile -> jawrFile.file.name == 'jawr-test.properties' }
    assert file != null
    assert file.properties.getProperty('shucks.test') == '1'

    file = files.find { ConfigScanner.JawrFile jawrFile -> jawrFile.file.name == 'jawr-empty.properties' }
    assert file != null
    assert file.properties.size() == 0
  }

  @Test
  public void multipleFolders() {
    List<ConfigScanner.JawrFile> files = new ArrayList<>()
    ConfigScanner.checkDir(files, findResourcesDir().toURI().toURL(), "META-INF", "cooties")

    assert files.size() == 3
  }

  @Test
  public void basic() {
    ConfigScanner scanner = new ConfigScanner()
    Properties properties = scanner.configProperties

    assert properties.get("shucks.test") == "1"
    assert properties.get("pimples.sports") == "2"

    scanner.configChanged()

    assert properties.get("shucks.test") == "1"
    assert properties.get("pimples.sports") == "2"

    System.setProperty(ConfigScanner.WEBAPP_DEV, "true")

    scanner.configChanged()

    assert properties.get("shucks.test") == "1"
    assert properties.get("pimples.sports") == "2"
  }

}
