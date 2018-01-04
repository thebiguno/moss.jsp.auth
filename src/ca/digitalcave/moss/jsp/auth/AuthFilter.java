/*
 * Created on May 28, 2008 by wyatt
 */
package ca.digitalcave.moss.jsp.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.digitalcave.moss.common.LogUtil;
import ca.digitalcave.moss.jsp.auth.config.Config;
import ca.digitalcave.moss.jsp.auth.config.ConfigFactory;

/**
 * A caching filter which works with the browser (via HTTP headers) to keep
 * network traffic down as much as possible.
 * 
 * Accepts the following init-params:
 *	config (where the auth.xml config file is located, relative to /WEB-INF/)
 * 
 * @author wyatt
 *
 */
public class AuthFilter implements Filter {
	
	private FilterConfig filterConfig;
	private Config config = null;
	private long lastConfigLoad = 0;
	
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//Verify that this is an HttpServletRequest, and ignore those which are not.
		if (!(req instanceof HttpServletRequest)){
			chain.doFilter(req, res);
			return;
		}
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if (config == null || lastConfigLoad + 60000 < System.currentTimeMillis()){
			config = ConfigFactory.loadConfig(filterConfig);
			if (config != null){
				lastConfigLoad = System.currentTimeMillis();
				LogUtil.setLogLevel(config.getLogLevel());
			}
		}
		
		//If this not authenticated, stop the request here.
		if (config != null && !config.checkAuthentication(request, response)){
			return;
		}
		
		//Otherwise, continue on down the chain
		chain.doFilter(request, response);
	}
		
	public void destroy() {
		config = null;
	}
}