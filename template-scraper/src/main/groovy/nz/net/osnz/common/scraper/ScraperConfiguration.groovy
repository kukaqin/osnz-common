package nz.net.osnz.common.scraper

import java.text.ParseException

class ScraperConfiguration {

  /**
   * Layout information
   */
  private Map<String, ScraperLayout> layouts;

  /**
   * Initialize data-members
   */
  public ScraperConfiguration(Properties properties) throws ParseException {
    if (!properties) {
      throw new IllegalArgumentException("Please specify a properties instance")
    }

    this.parseProperties(properties)
  }

  /**
   * Parse the properties file
   *
   * @param properties
   */
  protected void parseProperties(Properties properties) throws ParseException {
    this.layouts = [:]

    String layoutStr = properties.getProperty('scraper.layouts')
    layoutStr.split(",").each { String token ->

      String sanitizedToken = token.trim();
      if (!sanitizedToken) {
        return
      }

      this.layouts[sanitizedToken] = this.parseSpecificLayout(properties, sanitizedToken)
    }
  }

  /**
   * Get layout specific information
   *
   * @param properties are the properties to parse
   * @return a mapping for this layout containing the base url, and the scrape url
   */
  protected ScraperLayout parseSpecificLayout(Properties properties, String layout) throws ParseException {

    String  url = properties.getProperty("scraper.${layout}.url"),
            assetBase = properties.getProperty("scraper.${layout}.assetbase"),
            filter = properties.getProperty("scraper.${layout}.filter"),
            container = properties.getProperty("scraper.${layout}.container");

    if ( !url ) {
      throw new IllegalStateException("Unable to properly parse the layout information for: ${layout}")
    }

    if ( !assetBase ) {
      assetBase = url;
    }

    return ScraperLayout.newLayout(url, assetBase, filter, container);
  }


  /**
   * @return a list of layout identifiers
   */
  public String[] getLayoutNames() {
    return this.layouts.keySet().toArray(new String[this.layouts.keySet().size()]);
  }

  /**
   * Get layout information
   *
   * @param layoutName is the layout name
   * @return
   */
  public ScraperLayout getLayoutInformation(String layoutName) {
    if (!isValidLayout(layoutName)) {
      return null;
    }
    return layouts[layoutName]
  }

  /**
   * Is this a valid layout
   *
   * @param layoutName the name to check for
   * @return true if it is a valid layout name
   */
  public boolean isValidLayout(String layoutName) {
    return layoutName.trim() in this.layoutNames
  }



}
