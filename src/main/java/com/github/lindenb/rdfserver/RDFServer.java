package com.github.lindenb.rdfserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@SuppressWarnings("serial")
public class RDFServer extends HttpServlet
	{
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		}
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
		{
		
		}
	
	
	private boolean checkContextError(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
		{
		Throwable err=(Throwable)req.getServletContext().getAttribute(".error");
		if(err!=null)
			{
			resp.setContentType("text/plain");
			resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			PrintWriter pout=resp.getWriter();
			err.printStackTrace(pout);
			pout.close();
			return false;
			}
		return true;
		}
	
	private void handleCreateInstanceOf1(
			HttpServletRequest req,
			HttpServletResponse res,
			XMLStreamWriter w,
			Model model,
			Model dataStore
			)throws ServletException, IOException,XMLStreamException
		{
		String rdfClassUri=req.getParameter("rdfClass");
		if(rdfClassUri==null) 
			{
			putMessage(req, "all", new Message("rdf:Class missing"));
			return;
			}
		
		Resource rdfClass=ResourceFactory.createResource(rdfClassUri);

		String instanceUri=req.getParameter("instance.rdfAbout");
		if(instanceUri==null || instanceUri.isEmpty()) 
			{
			putMessage(req,"instance.rdfAbout", new Message("instance rdf:About missing"));
			doCreateInstanceOf1(req,res,w,model,dataStore,rdfClass);
			return;
			}
		Resource subject=ResourceFactory.createResource(instanceUri);
		if(dataStore.containsResource(subject))
			{
			putMessage(req, "instance.rdfAbout", new Message("instance already defined"));
			doCreateInstanceOf1(req,res,w,model,dataStore,rdfClass);
			return;
			}
		dataStore.add(subject,RDF.type,rdfClass);
		//add creation date TODO
		//add user TODO
		putMessage(req, "all", new Message("instance "+instanceUri+" created.",MessageType.ok));
		req.getRequestDispatcher("TODO").forward(req, res);
		}
	
	private void doCreateInstanceOf1(
			HttpServletRequest req,
			HttpServletResponse res,
			XMLStreamWriter w,
			Model model,
			Model rdfStore,
			Resource rdfClass
			) throws ServletException, IOException,XMLStreamException
		{
		w.writeEmptyElement("div");
		
		w.writeStartElement("form");
		w.writeAttribute("action", "");
		w.writeAttribute("method", "");
		
		w.writeEmptyElement("input");
		w.writeAttribute("type", "hidden");
		w.writeAttribute("name", "rdfClass");
		w.writeAttribute("value",rdfClass.getURI());
		
		String lblId=nextId(req);
		//label
		w.writeStartElement("label");
		w.writeAttribute("for",lblId);
		w.writeCharacters("URI:");
		w.writeEndElement();
		
		//uri
		w.writeEmptyElement("input");
		w.writeAttribute("type", "text");
		w.writeAttribute("name","instance.rdfAbout");
		String uri=req.getParameter("instance.rdfAbout");
		if(uri==null) uri=nextURI(model,rdfClass,rdfStore);
		
		w.writeAttribute("value",uri);
		w.writeAttribute("id", lblId);
		writeMessagesFor(req,w,"instance.rdfAbout");
		
		//submit
		w.writeEmptyElement("input");
		w.writeAttribute("submit", "text");
		w.writeAttribute("value", "Create");
		
		
		w.writeEndElement();//form
		w.writeEndElement();//div
		}
	
	private void doEditInstanceOf1(
			HttpServletRequest req,
			HttpServletResponse res,
			XMLStreamWriter w,
			Model model,
			Model rdfStore,
			Resource subject
			) throws ServletException, IOException,XMLStreamException
		{
		if(!rdfStore.contains(subject,null,(RDFNode)null))
			{
			putMessage(req, "all", new Message("subject missing"));
			//TODO
			return;
			}
		//get RDF class for this instance
		Resource rdfClass=getRDFSClassByInstance(model, rdfStore, subject);
		if(rdfClass==null)
			{
			putMessage(req, "all", new Message("rdfClass missing"));
			//TODO
			return;
			}
		
		w.writeCharacters(subject.getURI());
		w.writeCharacters(" is an instance of ");
		w.writeCharacters(toString(model,rdfClass));
		
		
		
		w.writeEmptyElement("div");
		
		w.writeStartElement("form");
		w.writeAttribute("action", "TODO");
		w.writeAttribute("method", "");
		
		w.writeEmptyElement("input");
		w.writeAttribute("type", "hidden");
		w.writeAttribute("name", "rdfClass");
		w.writeAttribute("value",rdfClass.getURI());
		
		w.writeEmptyElement("input");
		w.writeAttribute("type", "hidden");
		w.writeAttribute("name", "instance.uri");
		w.writeAttribute("value",subject.getURI());
		
		
		w.writeStartElement("table");
		w.writeStartElement("tbody");
		
		StmtIterator stmtIter=null;
		try
			{
			stmtIter=rdfStore.listStatements(subject, null,(RDFNode) null);
			
			while(stmtIter.hasNext())
				{
				Statement stmt=stmtIter.next();
				String rowId=nextId(req);
				w.writeStartElement("tr");
				w.writeAttribute("id", rowId);
				
				w.writeStartElement("th");
				w.writeCharacters(toString(model,stmt.getPredicate()));
				w.writeEndElement();//th
				
				w.writeStartElement("td");
				w.writeCharacters(toString(model,stmt.getObject()));
				w.writeEndElement();//td

				w.writeStartElement("td");
				
				w.writeStartElement("a");
				w.writeAttribute("title", "Remove");
				w.writeAttribute("href", "javascript:removeProperty({});");//TODO
				//TODO icon remove
				w.writeEndElement();//a
				
				w.writeEndElement();
				
				w.writeEndElement();//tr
				}
			
			}
		finally
			{
			if(stmtIter!=null) stmtIter.close();
			}
		w.writeEndElement();//tbody
		w.writeEndElement();//table
		
		
		String lblId=nextId(req);
		//label
		w.writeStartElement("label");
		w.writeAttribute("for",lblId);
		w.writeCharacters("Add Property");
		w.writeEndElement();
		w.writeStartElement("select");
		w.writeAttribute("id", lblId);
		ResIterator resIter=null;
		try
			{
			resIter=model.listResourcesWithProperty(RDFS.domain,(RDFNode) rdfClass);
			while(resIter.hasNext())
				{
				Resource prop=resIter.next();
				if(prop.isAnon()) continue;
				if(!model.contains(prop, RDF.type, RDF.Property) ) continue;
				
				w.writeStartElement("option");
				w.writeAttribute("value", prop.getURI());
				w.writeCharacters(model.shortForm(prop.getURI()));
				w.writeEndElement();//option
				}
			}
		finally
			{
			if(resIter!=null) resIter.close();
			}
		w.writeEndElement();
		
		
		//in range
		Set<Property> predicates=new HashSet<Property>();
		Set<Resource> linked_to=new HashSet<Resource>();
		stmtIter=null;
		try
			{
			stmtIter=rdfStore.listStatements(null,null,subject);
			while(stmtIter.hasNext())
				{
				Statement stmt=stmtIter.next();
				predicates.add(stmt.getPredicate());
				linked_to.add(stmt.getSubject());
				}
			}
		finally
			{
			if(stmtIter!=null) stmtIter.close();
			}
		
		w.writeStartElement("table");
		
		w.writeStartElement("thead");
		w.writeStartElement("tr");
		w.writeStartElement("th");
		w.writeCharacters("URI");
		w.writeEndElement();
		
		for(Property predicate:predicates)
			{
			w.writeStartElement("th");
			w.writeCharacters(predicate.toString());
			w.writeEndElement();
			}
		w.writeEndElement();//tr
		w.writeEndElement();//thead
		
		w.writeStartElement("tbody");
		
	
			for(Resource link:linked_to)
				{
				w.writeStartElement("tr");
				w.writeStartElement("th");
				w.writeCharacters(link.toString());
				w.writeEndElement();
				
				for(Property predicate:predicates)
					{
					stmtIter=null;
					try
						{
						w.writeStartElement("td");
						stmtIter=rdfStore.listStatements(link,predicate,(RDFNode)null);
						while(stmtIter.hasNext())
							{
							Statement stmt=stmtIter.next();
							break;
							}
						w.writeEndElement();//td
						}
					finally
						{
						if(stmtIter!=null) stmtIter.close();
						}
					}
				w.writeEndElement();//tr
				}
			
		
		w.writeEndElement();//tbody
		w.writeEndElement();//table
		
		
		
		//uri
		w.writeEmptyElement("input");
		w.writeAttribute("type", "text");
		w.writeAttribute("name","instance.rdfAbout");
		String uri=req.getParameter("instance.rdfAbout");
		if(uri==null) uri=nextURI(model,rdfClass,rdfStore);
		
		w.writeAttribute("value",uri);
		w.writeAttribute("id", lblId);
		writeMessagesFor(req,w,"instance.rdfAbout");
		
		//submit
		w.writeEmptyElement("input");
		w.writeAttribute("submit", "text");
		w.writeAttribute("value", "Create");
		
		
		w.writeEndElement();//form
		w.writeEndElement();//div
		}
	
	
	private String buildURL(
			HttpServletRequest req,
			String url,
			String...params
			) throws IOException,ServletException
		{
		boolean q=false;
		StringBuilder b=new StringBuilder(url);
		
		HttpSession session=req.getSession(false);
		if(session!=null)
			{
			b.append(";jsessionid=");
			b.append(session.getId());
			}
		
		for(int i=0;i+1<params.length;i+=2)
			{
			if(!q) { b.append("?");q=true;}
			b.append(URLEncoder.encode(params[i+0],"UTF-8"));
			b.append("=");
			b.append(URLEncoder.encode(params[i+1],"UTF-8"));
			}
		
		return b.toString();
		}
	
	private void writeHyperLink(
			XMLStreamWriter w,
			final Model model,
			final Model rdfStore,
			final RDFNode node
			) throws XMLStreamException
		{
		if(node==null)
			{
			w.writeStartElement("span");
			w.writeCharacters("null");
			w.writeEndElement();
			return;
			}
		else if(node.isLiteral())
			{
			w.writeStartElement("span");
			w.writeEndElement();
			}
		}
	
	private void writeRDFSClass(
			XMLStreamWriter w,
			Model model,
			Resource subject
			)
		{
		
		}
	
	private String toString(Model m,RDFNode node)
		{
		if(node==null) return "null";
		if(node.isAnon())
			{
			return node.asResource().getId().getLabelString();
			}
		return node.toString();
		}
	
	private Resource getRDFSClassByInstance(
			Model model,
			Model datastore,
			Resource subject
			)
		{
		Resource rdfClass=null;
		NodeIterator ni=null;
		try
			{
			ni=datastore.listObjectsOfProperty(subject, RDF.type);
			while(ni.hasNext())
				{
				RDFNode node=ni.next();
				if(!node.isResource()) continue;
				if(!model.contains(node.asResource(),RDF.type,RDFS.Class)) continue;
				return node.asResource();
				}
			}
		finally
			{
			if(ni!=null) ni.close();
			}
		return null;
		}
	
	private Literal getLiteralByInstance(
			final Model datastore,
			final Resource subject,
			final Property pred,
			final Literal defaultLit
			)
		{
		if(subject==null) return defaultLit;
		NodeIterator ni=null;
		try
			{
			ni=datastore.listObjectsOfProperty(subject, pred);
			while(ni.hasNext())
				{
				RDFNode node=ni.next();
				if(!node.isLiteral()) continue;
				return node.asLiteral();
				}
			}
		finally
			{
			if(ni!=null) ni.close();
			}
		return defaultLit;
		}
	
	
	private static final String MESSAGES_ATTRIBUTE=".messages";
	private void writeMessagesFor(
		HttpServletRequest req,
		XMLStreamWriter w,
		String key
		) throws  XMLStreamException
		{
		Map<String,List<Message>> msgs=(Map<String,List<Message>>)req.getAttribute(MESSAGES_ATTRIBUTE);
		if(msgs==null || msgs.isEmpty()) return;
		List<Message> L=msgs.get(key);
		if(L==null) return;
		for(Message m:L) m.write(w);
		}
	
	private void putMessage(HttpServletRequest req,String key,Message m)
		{
		if(key==null || m==null) return;
		Map<String,List<Message>> msgs=(Map<String,List<Message>>)req.getAttribute(MESSAGES_ATTRIBUTE);
		if(msgs==null )
			{
			msgs=new HashMap<String, List<Message>>();
			req.setAttribute(MESSAGES_ATTRIBUTE, msgs);
			}
		List<Message> L=msgs.get(key);
		if(L==null)
			{
			L=new ArrayList<Message>();
			L.add(m);
			msgs.put(key,L);
			}
		L.add(m);
		}
	
	
	private String nextURI(
			Model model,
			Resource rdfClass,
			Model rdfStore
			) throws ServletException
		{
		long id=0L;
		for(;;)
			{
			String uri="urn:"+rdfClass.getLocalName()+":"+(++id);
			Resource subject=ResourceFactory.createResource(uri);
			if(rdfStore.containsResource(subject)) continue;
			return uri;
			}
		}
	
	private String nextId(HttpServletRequest req) throws ServletException
		{
		final String att=".id.generator";
		Integer id=(Integer)req.getAttribute(att);
		int new_id=(id==null?1:id+1);
		req.setAttribute(att, new_id);
		return "id"+new_id;
		}
	
	enum MessageType { warning, error, ok, message };
	
	private class Message
		{
		private String content;
		private MessageType type;
		Message(String content,MessageType type)
			{
			this.content=content;
			this.type=type;
			}
		Message(String content)
			{
			this(content,MessageType.error);
			}
		Message()
			{
			this("Error");
			}
		
		public MessageType getType()
			{
			return type;
			}
		
		public String getContent()
			{
			return content;
			}
		
		void write(XMLStreamWriter w)
			throws XMLStreamException
			{
			w.writeStartElement("span");
			w.writeAttribute("class", "msg"+getType().name());
			w.writeCharacters(getContent());
			w.writeEndElement();
			}
		
		@Override
		public String toString()
			{
			return type.name()+":"+content;
			}
		}
	
	
	static <T> Map<T,T> mapOf(T...values)
		{
		Map<T,T> m=new LinkedHashMap<T, T>(values.length/2);
		for(int i=0;i +1 < values.length;i+=2)
			{
			m.put(values[i+1], values[i+2]);
			}
		return m;
		}
	
	
	
	protected XMLStreamWriter createXmlStreamWriter(HttpServletRequest req, HttpServletResponse resp)
				throws XMLStreamException,IOException
		{
		fixEncoding(req,resp);
		XMLOutputFactory xof=XMLOutputFactory.newFactory();
		return xof.createXMLStreamWriter(resp.getWriter());
		}
	
	protected void fixEncoding(HttpServletRequest req, HttpServletResponse resp)
			throws XMLStreamException,IOException
		{
		String enc=req.getCharacterEncoding();
		if(enc==null)
			{
			enc="UTF-8";
			req.setCharacterEncoding(enc);
			}
		
		enc=resp.getCharacterEncoding();
		if(enc==null)
			{
			enc="UTF-8";
			resp.setCharacterEncoding(enc);
			}
		
		}
	
	
	private String toJSON(final Statement stmt)
		{
		return"{\"subject\":"+toJSON(stmt.getObject())+
				",\"predicate\":"+toJSON(stmt.getPredicate())+
				",\"object\":"+toJSON(stmt.getObject())+
				"}";
		}
	private String toJSON(final RDFNode node)
		{
		if(node.isResource())
			{
			Resource rsrc=node.asResource();
			return toJSON(rsrc.getURI());
			}
		else if(node.isLiteral())
			{
			Literal L=node.asLiteral();
			return"{\"dataType\":"+toJSON(L.getDatatype().getURI())+
					",\"value\":"+toJSON(L.getString())+
					(L.getLanguage()==null?"":",\"lang\":"+toJSON(L.getLanguage()))+
					"}";
			}
		else
			{
			return "";
			}
		}

	private String toJSON(final String s)
		{
		return "";
		}
	
	
	
	protected void close(Object o)
		{
		if(o==null) return;
		
		if(o instanceof OutputStream)
			{
			try { OutputStream.class.cast(o).flush();}
			catch(Throwable err) {}
			}
		
		if(o instanceof Writer)
			{
			try { Writer.class.cast(o).flush();}
			catch(Throwable err) {}
			}
		
		if(o instanceof Closeable)
			{
			try { Closeable.class.cast(o).close();}
			catch(Throwable err) {}
			}
		
		try {
			Method m=o.getClass().getMethod("close");
			if(java.lang.reflect.Modifier.isStatic(m.getModifiers())) return;
			if(!java.lang.reflect.Modifier.isPublic(m.getModifiers())) return;
			m.invoke(m);
			}
		catch(Throwable err) {}
			{
			}
		}

	
	
	private interface Handler
		{
		public HttpServletRequest getHttpServletRequest();
		public HttpServletResponse getHttpServletResponse();
		public void apply() throws IOException,ServletException;
		public void cleanup();
		}
	
	private abstract class AbstractHandler
		implements Handler
		{
		private PrintWriter out=null;
		private HttpServletRequest request;
		private HttpServletResponse response;
		
		public AbstractHandler(HttpServletRequest request,HttpServletResponse response)
			{
			this.request=request;
			this.response=response;
			}
		
		public HttpServletRequest getHttpServletRequest()
			{
			return request;
			}
		public HttpServletResponse getHttpServletResponse()
			{
			return response;
			}
		
		protected void fixEncoding()throws IOException,ServletException
			{
			this.getHttpServletRequest().setCharacterEncoding("UTF-8");
			this.getHttpServletResponse().setCharacterEncoding("UTF-8");
			}
		
		@Override
		public void apply() throws IOException,ServletException
			{
			fixEncoding();
			}
		
		/* get printwriter */
		protected PrintWriter w() throws IOException
			{
			if(out==null) out=getHttpServletResponse().getWriter();
			return out;
			}
		
		@Override
		public void cleanup()
			{
			if(this.out!=null) {out.flush();out.close();}
			}
		}
	
	private class DebugHandler
		extends AbstractHandler
		{
		DebugHandler(HttpServletRequest request,HttpServletResponse response)
			{
			super(request,response);
			}
		@Override
		public void apply() throws IOException, ServletException
			{
			fixEncoding();
			getHttpServletResponse().setContentType("text/plain");
			w().println("PathInfo:"+getHttpServletRequest().getPathInfo());
			w().println("ContextPath:"+getHttpServletRequest().getContextPath());
			w().println("getPathTranslated:"+getHttpServletRequest().getPathTranslated());
			w().println("getQueryString:"+getHttpServletRequest().getQueryString());
			}
		}
	
	private class XMLHandler
	extends AbstractHandler
		{
		private XMLStreamWriter xmlStreamWriter=null;
		XMLHandler(HttpServletRequest request,HttpServletResponse response)
			{
			super(request,response);
			}
		
		protected XMLStreamWriter out() throws XMLStreamException,IOException
			{
			if(xmlStreamWriter!=null)
				{
				XMLOutputFactory xof=XMLOutputFactory.newFactory();
				xmlStreamWriter=xof.createXMLStreamWriter(w());
				}
			return xmlStreamWriter;
			}
		@Override
		public void cleanup() {
			if(xmlStreamWriter!=null)
				{
				try {
					xmlStreamWriter.flush();
					xmlStreamWriter.close();
					}
				catch(Exception E) {} 
				}
			super.cleanup();
			}
		
		public String getContentType()
			{
			return "text/plain";
			}
		
		
		
		@Override
		public void apply() throws IOException, ServletException
			{
			fixEncoding();
			getHttpServletResponse().setContentType(getContentType());
			
			}
		}
	
	private class HTMLHandler
		extends XMLHandler
			{
			HTMLHandler(HttpServletRequest request,HttpServletResponse response)
				{
				super(request,response);
				}
			public String getContentType()
				{
				return "text/plain";
				}
			
			@Override
			public void apply() throws IOException, ServletException
				{
				super.apply();
				
				}
			
			}

	
	
	}