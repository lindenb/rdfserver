package org.lindenb.rdfserver;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;

public class DumpServlet extends HttpServlet
	{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
		{
		Model m=(Model)getServletContext().getAttribute("rdf.data");
		resp.setContentType("text/xml");
		OutputStream out=resp.getOutputStream();
		m.write(out);
		out.flush();
		out.close();
		}
	}
