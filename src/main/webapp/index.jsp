<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="rdf" uri="http://rdfserver.lindenb.org/tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<rdf:lock>
Hello world!
<rdf:define name="s" uri="http://www.google.com"/>
${s}

<rdf:define name="s2" qName="rdf:type"/>
${s2}

<rdf:define name="s3" lang="en">Hello world</rdf:define>
${s3}

<rdf:list name="iter"  predicate="${s2}">
	<span>${iter}</span><br/>
</rdf:list>

</rdf:lock>

</body>
</html>