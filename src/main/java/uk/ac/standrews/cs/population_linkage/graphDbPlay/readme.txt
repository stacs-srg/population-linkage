====
    Copyright 2020 Systems Research Group, University of St Andrews:
    <https://github.com/stacs-srg>
====

This folder contains working neo4j examples as if 23/2/2020.
You can run neo4J:

    1. From their own browser environment - works on a different port from other versions.
    2. As an embedded system - 1 example here  (EmbeddedNeo4J)
    3. As a separate installer - use brew for the mac.

Version 3 above is what is used for all the versions (apart from the Embedded version).

if you run the brew version the neo4J browser runs here: http://localhost:7474/browser/
and the bolt binding to the db is here: bolt://localhost:768

For the neo4j browser version (which you can run without installing neo4j using brew) the binding is: bolt://localhost:11003 - I did not do this!

I set the password for my install to be "password" the default user is neo4j.
You set the password like this:

$ neo4j-admin set-initial-password password

To start the db you do this:

$ brew services start neo4j
of
$ neo4j start

You can get the status like this:

$neo4j status
Which prints this -
Neo4j is running at pid 31107  (which is useless).

You styop the server by doing this:
$neo4j stop

The examples do OGM (Object Graph Modelling) like ORM but for graphs and use use Cypher queries.

You can create objects either using Cypher or using OGM.

The brew installer for Neo4j creates this stuff:

  home:         /usr/local/homebrew/Cellar/neo4j/4.2.2/libexec
  config:       /usr/local/homebrew/Cellar/neo4j/4.2.2/libexec/conf
  logs:         /usr/local/homebrew/var/log/neo4j
  plugins:      /usr/local/homebrew/Cellar/neo4j/4.2.2/libexec/plugins
  import:       /usr/local/homebrew/Cellar/neo4j/4.2.2/libexec/import
  data:         /usr/local/homebrew/var/neo4j/data
  certificates: /usr/local/homebrew/Cellar/neo4j/4.2.2/libexec/certificates
  run:          /usr/local/homebrew/Cellar/neo4j/4.2.2/libexec/run

Note that you need a lot of different Mvn dependencies to get it all working too - in the POM.

See also notes.txt about OGM.

I hope this is useful!



