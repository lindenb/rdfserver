package org.lindenb.rdfserver;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

public class LockFilter implements Filter
	{
	private ServletContext ctx=null;
	private boolean lock;
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		ctx=cfg.getServletContext();
		String s=cfg.getInitParameter("lock");
		if("read".equals(s))
			{
			lock=Lock.READ;
			}
		else
			{
			lock=Lock.WRITE;
			}
		}
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException
		{
		Model m=(Model)ctx.getAttribute("rdf.data");
		try
			{
			m.enterCriticalSection(this.lock);
			chain.doFilter(req, res);
			}
		finally
			{
			m.leaveCriticalSection();
			}
		}
	@Override
	public void destroy() {
		ctx=null;
		}
	}
