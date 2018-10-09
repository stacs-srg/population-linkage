#!/bin/sh
scp -q secure@manifesto.cs.st-andrews.ac.uk:/home/secure/maven_repo/uk/ac/standrews/cs/data-kilmarnock/1.0/data-kilmarnock-1.0-jar-with-dependencies.jar .
mvn -q install:install-file -Dfile=data-kilmarnock-1.0-jar-with-dependencies.jar -DgroupId=uk.ac.standrews.cs -DartifactId=data-kilmarnock -Dversion=1.0 -Dpackaging=jar
rm data-kilmarnock-1.0-jar-with-dependencies.jar
