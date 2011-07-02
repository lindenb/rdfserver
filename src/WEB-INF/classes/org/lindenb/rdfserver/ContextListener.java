package org.lindenb.rdfserver;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;

public class ContextListener implements ServletContextListener
	{
	
	@Override
	public void contextInitialized(ServletContextEvent evt)
		{
		ServletContext ctx=evt.getServletContext();
		Model schema=ModelFactory.createDefaultModel();
	
		schema.read(ctx.getInitParameter("rdf.schema"));
		ctx.setAttribute("rdf.schema",schema);
		
		Model instances=ModelFactory.createDefaultModel();
		instances.read(ctx.getInitParameter("rdf.data"));
		ctx.setAttribute("rdf.data",instances);
		
		
		}
	
	@Override
	public void contextDestroyed(ServletContextEvent evt)
		{
		ServletContext ctx=evt.getServletContext();
		Model m=(Model)ctx.getAttribute("rdf.schema");
		m.close();
		m=(Model)ctx.getAttribute("rdf.data");
		m.close();
		}

	}
