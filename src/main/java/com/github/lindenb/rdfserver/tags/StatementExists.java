package org.lindenb.rdfserver.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


public class StatementExists extends BodyTagSupport
	{
	private static final long serialVersionUID = 1L;
	private Resource subject=null;
	private Resource predicate=null;
	private RDFNode object=null;
	private Model model=null;
	private boolean exists=true;
	
	public StatementExists()
		{
		
		}
	public void setModel(Model model)
		{
		this.model=model;
		}
	
	public void setObject(RDFNode object)
		{
		this.object = object;
		}
	
	public void setSubject(Resource subject)
		{
		this.subject = subject;
		}
	
	public void setPredicate(Resource predicate)
		{
		this.predicate = predicate;
		}
	
	public void setExists(boolean exists)
		{
		this.exists = exists;
		}
	
	
	@Override
	public int doStartTag() throws JspException
		{
		if(this.model==null) throw new JspException("undefined model");
		Model m=this.model;
		Property p=null;
		if(predicate!=null)
			{
			if(predicate instanceof Property)
				{
				p=Property.class.cast(predicate);
				}
			else
				{
				p= m.createProperty(predicate.getURI());
				}
			}
		return (m.contains(this.subject,p,this.object)==this.exists?EVAL_BODY_INCLUDE:SKIP_BODY);
		}
	
	
	@Override
	public int doAfterBody() throws JspException
		{
		return SKIP_BODY;
		}
	
	@Override
	public int doEndTag() throws JspException
		{
		return EVAL_PAGE;
		}
	
	@Override
	public void release()
		{
		this.subject=null;
		this.predicate=null;
		this.object=null;
		this.model=null;
		this.exists=true;
		super.release();
		}
	}
