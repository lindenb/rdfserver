package com.github.lindenb.rdfserver;

import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.util.FileUtils;

public class FilterLoadModel implements Filter
	{
	private static final String RDF_MODEL_PATH="rdf.model.path";
	private String modelPath=null;
	@Override
	public void init(FilterConfig cfg) throws ServletException
		{
		this.modelPath=cfg.getInitParameter(RDF_MODEL_PATH);
		if(this.modelPath==null)
			{
			throw new ServletException(RDF_MODEL_PATH+" undefined");
			}
		
		}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain
			) throws IOException, ServletException
		{
		Model m=null;
		try
			{
			m=ModelFactory.createDefaultModel();
			RDFReader r=m.getReader(FileUtils.langXMLAbbrev);
			r.read(m, this.modelPath);
			req.setAttribute("model", m);
			}
		catch(Exception err)
			{
			throw new ServletException(err);
			}
		chain.doFilter(req, res);
		m=(Model)req.getAttribute("model");
		
		}

	
	
	@Override
	public void destroy()
		{
		

		}
	
	private synchronized void saveModel(final Model model) throws IOException
		{
		FileWriter out=null;
		try {
			out=new FileWriter( this.modelPath );
		    model.write( out,FileUtils.langXMLAbbrev);
			}
		finally
			{
		   try  {
		        if(out!=null)out.close();
		   		}
		   catch (IOException closeException)
			   	{
		       
		   		}
			}
		}
	}
