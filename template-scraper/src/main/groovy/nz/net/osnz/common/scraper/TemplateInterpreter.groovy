package nz.net.osnz.common.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.util.StringUtils

class TemplateInterpreter {

  /**
   * The parsed document
   */
  private static Map<ScraperLayout, Document> parsedDoc = [:];

  /**
   * Layout configuration
   */
  private ScraperLayout layout;

  /**
   * Layout information
   *
   * @param layout is the layout to use
   */
  public TemplateInterpreter(ScraperLayout layout) {
    if (layout == null) {
      throw new IllegalArgumentException("Not a valid layout instance");
    }
    this.layout = layout;
  }


  /**
   * Retrieve the template content
   */
  public Document getTemplateContent(boolean doRefresh = false) {

    if (!parsedDoc[layout] || doRefresh) {

      Document doc = getTemplateDocument()

      if (doc) {

        doc.select("html")

        // remove the title
        doc.select("head title").remove()

        // remove the analytics block
        removeAnalyticsScriptBlock(doc)

        // remove resources from header
        removeIgnoredIncludes(doc, layout.filter)

        // sanitize stylesheet urls
        doc.select("[href]").each { node -> rebaseAssetForNode(node, "href") }
        doc.select("[src]").each { node -> rebaseAssetForNode(node, "src") }

        parsedDoc[layout] = doc
      }
    }

    return parsedDoc[layout]

  }

  /**
   * Retrieve the <head /> content
   * @return
   */
  public String getHtmlHead() {
    return this.getTemplateContent().select("head").html();
  }

  /**
   * Get the university body
   *
   * @param leftBar show the left
   * @param rightBar
   * @param content
   * @return
   */
  public String getBodyWithContent(String container, String content, boolean leftBar = false, String leftBarContainerId = 'leftBar', boolean rightBar = false, String rightBarContainerId = 'rightBar') {
    Document freshDoc = this.getTemplateContent().clone()

    if (!leftBar && leftBarContainerId) {
      freshDoc.select("#${leftBarContainerId}")?.remove()
    }

    if (!rightBar && rightBarContainerId ) {
      freshDoc.select("#${rightBarContainerId}")?.remove()
    }

    if ( !StringUtils.isEmpty(this.layout.container) ) {
      container = this.layout.container
    }

    freshDoc.select("#${container}").html(content)

    return freshDoc.select("body").html()
  }

  /**
   * Remove a number of script includes from the header document to take
   * back control over which javascript libraries are included.
   *
   * @param uri is a list of uris to remove
   */
  protected void removeIgnoredIncludes(Document doc, String... filters) {
    removeIgnoredIncludes(doc, filters)
  }

  protected void removeIgnoredIncludes(Document doc, List<String> filters) {
    filters?.each { String location ->
      doc.select("script").findAll {Element element ->
        element.attr('src').toLowerCase().trim().find( location.trim() ) != null
      }*.remove()
    }
  }

  /**
   * Run closure on nodes that have analytics information
   *
   * @param doc is the jsoup document to go through
   * @param closure is the closure to invoke
   */
  protected void removeAnalyticsScriptBlock(Document doc) {
    doc.select("script").each { Element node ->
      if (hasAnalyticsInformation(node)) {
        node.remove()
      }
    }
  }

  /**
   * Analytics information?
   *
   * @param node
   * @return
   */
  protected boolean hasAnalyticsInformation(Element node) {
    node.html().contains("_setAccount")
  }


  /**
   * Sanitize the node
   *
   * @param node node to sanitize
   * @param attr attribute to query
   */
  protected void rebaseAssetForNode(node, attr) {
    def url = node.attr(attr)
    if (url.indexOf("://") == -1) {
      node.attr(attr, this.getRelativeLinksBase() + url)
    }
  }

  /**
   * @return the document parsed from the university's page.
   */
  protected Document getTemplateDocument() {
    return Jsoup.connect(getTemplateBaseLocation()).get()
  }

  /**
   * @return the base location for the static files
   */
  protected String getRelativeLinksBase() {
    return layout.assets
  }

  /**
   * @return configured template location
   */
  protected String getTemplateBaseLocation() {
    return layout.url
  }




}
