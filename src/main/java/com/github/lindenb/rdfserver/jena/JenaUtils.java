package com.github.lindenb.rdfserver.jena;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class JenaUtils
	{
	public static String uri(final Resource rsrc)
		{
		return rsrc==null?null:rsrc.getURI();
		}
	
	public static Model model(
			HttpServletRequest req
			) throws ServletException
			{
			return null;
			}
	}
