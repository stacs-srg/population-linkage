#!/bin/sh
scp -rq secure@manifesto.cs.st-andrews.ac.uk:/home/secure/maven_repo/uk/ac/standrews/cs/data-kilmarnock/1.0-SNAPSHOT/data-kilmarnock-*-jar-with-dependencies.jar data-kilmarnock-jar-with-dependencies.jar
mvn -q install:install-file -Dfile=data-kilmarnock-jar-with-dependencies.jar -DgroupId=uk.ac.standrews.cs -DartifactId=data-kilmarnock -Dversion=1.0-SNAPSHOT -Dpackaging=jar
rm data-kilmarnock-jar-with-dependencies.jar
