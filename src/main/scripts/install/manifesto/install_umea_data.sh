#!/bin/sh
mvn -q install:install-file -Dfile=/home/secure/maven/uk/ac/standrews/cs/data-umea/1.0/data-umea-1.0-jar-with-dependencies.jar -DgroupId=uk.ac.standrews.cs -DartifactId=data-umea -Dversion=1.0 -Dpackaging=jar
