#!/bin/sh
scp -q secure@manifesto.cs.st-andrews.ac.uk:/home/secure/maven/uk/ac/standrews/cs/data-skye/1.0/data-skye-1.0-jar-with-dependencies.jar .
mvn -q install:install-file -Dfile=data-skye-1.0-jar-with-dependencies.jar -DgroupId=uk.ac.standrews.cs -DartifactId=data-skye -Dversion=1.0 -Dpackaging=jar
rm data-skye-1.0-jar-with-dependencies.jar
