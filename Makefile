lib.dir=libs
all.jars = $(addprefix ${lib.dir}/,$(sort  org/apache/jena/jena-core/2.13.0/jena-core-2.13.0.jar org/slf4j/slf4j-api/1.7.12/slf4j-api-1.7.12.jar org/apache/jena/jena-iri/1.1.2/jena-iri-1.1.2.jar xerces/xercesImpl/2.11.0/xercesImpl-2.11.0.jar org/webjars/jquery/2.1.4/jquery-2.1.4.jar javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar ))

.PHONY:all deploy clean lib/rdfserver.jar

all: ${all.jars}

libs/rdfserver.jar : 
	rm -rf tmp
	mkdir -p tmp/WEB-INF/classes tmp/WEB-INF/lib libs
	rm -rf tmp


deploy: libs/rdfserver.jar

${all.jars} : 
	mkdir -p $(dir $@) && curl -o $@ "http://central.maven.org/maven2/$(patsubst ${lib.dir}/%,%,$@)"

clean:
	rm -rf libs
