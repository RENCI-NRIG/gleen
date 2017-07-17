GLEEN - A regular path library for ARQ SparQL

The GLEEN library is a property function library for the Jena ARQ SparQL query engine.

GLEEN was developed by:

> Todd Detwiler  
> Structural Informatics Group  
> University of Washington

GLEEN is currently licensed under the Apache License, version 2.0

------

This version of GLEEN is forked from the last released version: 0.6.1

The fork was created by Victor J. Orlikowski (Duke University) in support of
the ORCA project (https://geni-orca.renci.org/trac/), which makes use of GLEEN.

This new revision ports GLEEN forward to currently supported versions of JENA
(http://jena.apache.org/).

------

### Building

For this revision of GLEEN, simply check out the source, then ensure the
following JARs (at a minimum) are copied into the "lib" directory:

- commons-logging-1.1.1.jar
- commons-logging-api-1.0.4.jar
- jena-arq-2.11.0.jar
- jena-core-2.11.0.jar
- jena-iri-1.0.0.jar
- jena-tdb-1.0.0.jar

Afterward, issue an "ant" - and the distribution should build, with the jar
being placed in the "dist" directory.
