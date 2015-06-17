package org.lindenb.rdfserver.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.hp.hpl.jena.rdf.model.Model;

public class Lock extends BodyTagSupport
	{
	@Override
	public int doStartTag() throws JspException
		{
		Model m=(Model)super.pageContext.getServletContext().getAttribute("rdf.data");
		if(m.supportsTransactions())
			{
			m.begin();
			}
		return EVAL_BODY_INCLUDE;
		}
	@Override
	public int doEndTag() throws JspException
		{
		Model m=(Model)super.pageContext.getServletContext().getAttribute("rdf.data");
		if(m.supportsTransactions())
			{
			m.commit();
			}
		return EVAL_PAGE;
		}
	}
