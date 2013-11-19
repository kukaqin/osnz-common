package nz.net.osnz.common.scraper

import org.apache.commons.lang3.StringUtils


class ScraperLayout {

  public static final String DEFAULT_LAYOUT = "default";

  private String url;
  private String assets;
  private List<String> filter;
  private String container;

  /**
   * Initialize data-members
   *
   * @param name
   * @param url
   * @param assets
   */

  private ScraperLayout(final String url, final String assets, final String filter, final String container) {
    this.url = url;
    this.assets = assets;

    if ( StringUtils.isNotBlank(filter) ) {
      if ( filter.indexOf(',') ) {
        this.filter = StringUtils.split( filter, ',' ).toList()
      } else {
        this.filter = [ StringUtils.trimToEmpty(filter) ]
      }
    } else {
      this.filter = null;
    }

    this.container = container;
  }

  /**
   * Return a scraper layout instance
   *
   * @param url is the url to pass through
   * @param assets is the asset url to pass through
   * @param filter is the filter to remove remote resources from header
   * @return
   */
  public static ScraperLayout newLayout(final String url, final String assets, final String filter, final String container) {
    if ( url && assets ) {
      return new ScraperLayout(url, assets, filter, container);
    }
    throw new IllegalArgumentException("Either url, assets, or ignores were empty");
  }

  public String getAssets() {
    return assets;
  }

  public void setAssets(final String assets) {
    this.assets = assets;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public List<String> getFilter() {
    return this.filter;
  }

  public void setFilter(final List<String> filter) {
    this.filter = filter;
  }

  public void setContainer(final String container) {
    this.container = container;
  }

  public String getContainer() {
    return this.container;
  }

}
