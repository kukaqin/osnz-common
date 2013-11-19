package nz.net.osnz.common.scraper

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.jsp.JspWriter
import javax.servlet.jsp.tagext.BodyContent
import javax.servlet.jsp.tagext.BodyTagSupport

class TemplateBodyTag extends BodyTagSupport {

  /**
   * Logger
   */
  private static final Logger LOG = LoggerFactory.getLogger(TemplateBodyTag.class);

  /**
   * the ID of container will be replaced
   */
  private String container = "main";

  /**
   * Has left bar?
   */
  private boolean leftBar = true;

  /**
   * the container ID of left bar
   */
  private String leftBarContainerId = 'leftBar'

  /**
   * Has right bar?
   */
  private boolean rightBar = true;

  /**
   * the container ID of right bar
   */
  private String rightBarContainerId = 'rightBar'

  /**
   * Layout to use
   */
  private String layout = ScraperLayout.DEFAULT_LAYOUT;


  /**
   * Called after this tag was closed off.
   *
   * @return skip_body so we won't render the body.
   */
  @Override
  int doAfterBody() {

    BodyContent body = this.getBodyContent()
    JspWriter out = body.getEnclosingWriter()

    ScraperConfigurationLoader loader = new ScraperConfigurationLoader()
    ScraperConfiguration scraper = loader.getConfiguration(pageContext.servletContext)
    TemplateInterpreter templateInterpreter = new TemplateInterpreter(scraper.getLayoutInformation(this.layout))

    try {
      out.print(
        templateInterpreter.getBodyWithContent(this.container, body.string, this.leftBar, this.leftBarContainerId, this.rightBar, this.rightBarContainerId)
      )
    }
    catch (IOException ioEx) {
      LOG.info("Unable to properly parse the university body for layout ${this.layout}");
    }

    return SKIP_BODY;
  }

  void setContainer(String container) {
    this.container = container;
  }

  public String getContainer() {
    return this.container;
  }

  boolean getRightBar() {
    return rightBar
  }

  void setRightBar(boolean rightBar) {
    this.rightBar = rightBar
  }

  void setRightBarId(String rightBarId) {
    this.rightBarContainerId = rightBarId
  }

  String getRightBarId() {
    return this.rightBarContainerId
  }

  boolean getLeftBar() {
    return leftBar
  }

  void setLeftBar(boolean leftBar) {
    this.leftBar = leftBar
  }

  String getLeftBarId() {
    return this.leftBarContainerId
  }

  void setLeftBarId(String leftBarId) {
    this.leftBarContainerId = leftBarId
  }

  String getLayout() {
    return layout ?: "default"
  }

  /**
   * Set the layout. Will check validity of layout name
   *
   * @param layout is the layout name to use.
   */
  void setLayout(String layout) {
    ScraperConfigurationLoader loader = new ScraperConfigurationLoader();
    ScraperConfiguration sConfig = loader.getConfiguration(pageContext.servletContext)

    if (sConfig.isValidLayout(layout)) {
      this.layout = layout
    }
    else {
      throw new IllegalArgumentException("No such layout defined in the scraper configuration")
    }
  }


}
