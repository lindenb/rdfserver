package org.lindenb.rdfserver.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.IterationTag;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ListStatements extends BodyTagSupport
	implements IterationTag
	{
	private static final long serialVersionUID = 1L;
	private Resource subject=null;
	private Resource predicate=null;
	private RDFNode object=null;
	private StmtIterator iter;
	private String name;
	private Integer skip=0;
	private Integer limit=null;
	private int count=0;
	private Model model;
	
	
	
	public ListStatements()
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
	
	public void setName(String name)
		{
		this.name = name;
		}
	@Override
	public int doStartTag() throws JspException
		{
		if(this.limit!=null && this.limit==0)
			{
			return SKIP_BODY;
			}
		Model m=this.model;
		if(m==null) throw new JspException("model undefined");
		Property p=null;
		this.count=0;
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
		
		this.iter=m.listStatements(this.subject,p,this.object);
		
		int n=this.skip;
		while(n>0 && iter.hasNext())
			{
			iter.next();
			--n;
			}
		
		if(!iter.hasNext())
			{
			return SKIP_BODY;
			}
		this.count++;
		super.pageContext.setAttribute(name, iter.nextStatement());
		return EVAL_BODY_INCLUDE;
		}
	
	
	@Override
	public int doAfterBody() throws JspException
		{
		if(this.limit!=null && this.count>=this.limit)
			{
			return SKIP_BODY;
			}
		if(iter!=null && iter.hasNext())
			{
			this.count++;
			super.pageContext.setAttribute(name, iter.nextStatement());
			return EVAL_BODY_AGAIN;
			}
		return SKIP_BODY;
		}
	
	@Override
	public int doEndTag() throws JspException
		{
		if(this.iter!=null) this.iter.close();
		return EVAL_PAGE;
		}
	
	@Override
	public void release()
		{
		this.iter=null;
		this.subject=null;
		this.predicate=null;
		this.object=null;
		this.skip=0;
		this.limit=null;
		this.count=0;
		this.model=null;
		super.release();
		}
	}
