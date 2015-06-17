package org.lindenb.rdfserver.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class DefNode extends BodyTagSupport
	{
	private static final long serialVersionUID = 1L;
	private String name=null;
	private String uri=null;
	private String qName=null;
	private String datatype;
	private String lang=null;
	private Model model;
	
	public void setModel(Model model)
		{
		this.model=model;
		}
	
	public void setUri(String uri)
		{
		this.uri = uri;
		}
	public void setName(String name)
		{
		this.name = name;
		}
	public void setqName(String qName)
		{
		this.qName = qName;
		}
	public void setLang(String lang)
		{
		this.lang = lang;
		}
	public void setDataType(String datatype)
		{
		this.datatype = datatype;
		}
	@Override
	public int doStartTag() throws JspException
        {
        if(this.uri!=null || qName!=null || name==null) return SKIP_BODY;
        return EVAL_BODY_BUFFERED;
        }
	@Override
	public int doEndTag() throws JspException
		{
		if(name==null) return EVAL_PAGE;
		Model m=this.model;
		if(m==null) m=ModelFactory.createDefaultModel();
		RDFNode node=null;
		if(this.uri!=null)
			{
			node=m.createResource(this.uri);
			}
		else if(this.qName!=null)
			{
			this.qName=m.expandPrefix(this.qName);
			node=m.createResource(this.qName);
			}
		else if(super.bodyContent!=null)
			{
			String s=super.bodyContent.getString();
			if(s!=null)
				{
				if(lang!=null)
					{
					node=m.createLiteral(s,lang);
					}
				else if(datatype!=null)
					{
					node=m.createTypedLiteral(s, datatype);
					}
				else
					{
					node=m.createLiteral(s);
					}
				}
			}
		if(node==null) throw new JspException("Nothing defined");
		pageContext.setAttribute(this.name, node);
		return EVAL_PAGE;
		}
	
	@Override
	public void release()
		{
		this.lang=null;
		this.uri=null;
		this.name=null;
		this.qName=null;
		this.datatype=null;
		this.model=null;
		super.release();
		}
	}
