package nz.net.osnz.common.scraper

import javax.servlet.ServletContext

/**
 * No-javadoc
 */
class ScraperConfigurationLoader {

  /**
   * Where to find the scraper properties file
   */
  public static final String SCRAPER_PROPERTIES_FILE = "scraper.properties"

  /**
   * Filename
   */
  private String filename;

  /**
   * Configuration instance (lazy loading)
   */
  private static ScraperConfiguration configuration;

  /**
   * Initialize data-members
   *
   * @param filename is the file name to go looking for
   */
  public ScraperConfigurationLoader(String filename = SCRAPER_PROPERTIES_FILE) {
    this.filename = filename
  }

  /**
   * Load the scraper configuration
   *
   * @param servletContext is the servlet context to load a resource from
   * @return a scraper configuration that is queryable
   *
   * @throws FileNotFoundException when scraper.properties cannot be loaded
   * @throws IOException when the input stream cannot be read properly.
   */
  public ScraperConfiguration getConfiguration(ServletContext servletContext) throws FileNotFoundException, IOException {

    if (!ScraperConfigurationLoader.configuration) {
      InputStream iStream = getClass().getClassLoader().getResourceAsStream(this.filename);
      if (!iStream) {
        throw new FileNotFoundException("could not find ${this.filename} to load the scraper configuration");
      }

      Properties propDefinitions = new Properties();
      propDefinitions.load(iStream);

      ScraperConfigurationLoader.configuration = new ScraperConfiguration(propDefinitions);
    }

    return ScraperConfigurationLoader.configuration;
  }


}
