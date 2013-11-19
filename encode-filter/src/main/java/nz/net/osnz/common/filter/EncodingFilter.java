package nz.net.osnz.common.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public final class EncodingFilter implements Filter {

  private FilterConfig filterConfig;

  private String targetEncoding = "UTF-8";

  @Override
  public final void init(FilterConfig config) throws ServletException {
    this.filterConfig = config;
    try {
      this.targetEncoding = config.getInitParameter("encoding");
      if ( this.targetEncoding == null ) {
        this.targetEncoding = "UTF-8";
      }
    } catch ( Exception ex ) {
      this.targetEncoding = "UTF-8";
    }

  }

  @Override
  public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    ((HttpServletRequest) request).setCharacterEncoding(this.targetEncoding);
    chain.doFilter(request, response);
  }

  @SuppressWarnings("unused")
  public final void setFilterConfig(final FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  @Override
  public void destroy() {
    this.filterConfig = null;
  }


}
