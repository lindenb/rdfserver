package com.github.lindenb.rdfserver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.lindenb.rdfserver.jena.JenaUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class RESTServlet extends HttpServlet
	{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
		{
		Model model=JenaUtils.model(req);
		}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
		{
		this.doGet(req, resp);
		}
	
	protected void jqueryFindInstanceByClass(
			HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
		{
		Resource rdfClass=null;
		resp.setContentType("application/json");
		Model m=JenaUtils.model(req);
		PrintWriter out=resp.getWriter();
		out.print("[");
		int count=0;
		ResIterator ri=null;
		try
			{
			ri=m.listResourcesWithProperty(RDF.type, rdfClass);
			while(ri.hasNext() && count < 10)
				{
				Resource r=ri.next();
				
				if(count>0) out.print(",");
				++count;
				out.print("{\"label\":");
				quote(out,JenaUtils.uri(r));
				out.print(",\"value\":");
				quote(out,JenaUtils.uri(r));
				out.print("}");
				}
			}
		finally
			{
			if(ri!=null) ri.close();
			}		
		
		out.print("]");
		out.flush();
		out.close();
		}
	private static void quote(PrintWriter out,String s)
		{
		if(s==null)
			{
			out.print("null");
			}
		else
			{
			out.print("\"");
			for(int i=0;i< s.length();++i)
				{
				switch(s.charAt(i))
					{
					default: out.print(s.charAt(i));break;
					}
				}
			out.print("\"");
			}
		}
	
	}
