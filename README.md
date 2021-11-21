# IS Project 2021

## Project structure

## Setup
- Clone the repository -
```git clone git@git.cs.uni-kl.de:dbis/is-project-21/group-03.git```

- Go to Eclipse -> Preferences -> Java -> Compiler -> Set compiler compliance level to 1.8
- Go to Eclipse -> Preferences -> Java -> Installed JREs -> Execution Environment -> JavaSE-1.8 -> JRE17

- Go to Help -> Install new software -> Select your eclipse version
  - Scroll down to Web, XML, Java EE and OSGi Enterprise Development and select the following:
    - Eclipse Java EE Developer Tools
    - Eclipse Java Web Developer Tools
    - Eclipse Java Web Developer Tools - Javascript Support
    - Eclipse Web Developer Tools
    - Eclipse Web Javascript Developer Tools
    - Eclipse XML Editors and Tools
    - JST Server Adapters
    - JST Server Adapters Extensions (Apache Tomcat)
  - Allow Eclipse to restart after these installations

- Create new project (name it as CrawlSearch) -> Dynamic Web Project -> Set Dynamic web module version to 3.1 -> set output folder to 'build' -> Open Java EE Perspective
- Import project into this newly created project from the filesystem directory cloned earlier

- Right click on the project -> New -> Other -> Server -> Apache -> Tomcat v9.0 Server -> Next -> Download and Install (install it at the same folder as your project root folder) -> Next -> Add project as configured resource -> Finish

- Right click on project -> Properties -> Project Facets
  - The following should be selected:
  - Dynamic Web Module 3.1
  - Java 1.8, Runtime - apache-tomcat-9.x.x

- Configure build path
  - The following Libraries should be selected
    - Server Runtime -> apache-tomcat-9.0.46
    - Web App Libraries
    - JRE System Library JavaSE-1.8
    - Add External JARS -> Navigate to the 'lib' folder -> Select the 2 JARS 'jtidy-r938.jar' and 'postgresql-42.3.1jar'

- Update DB user and password in comm.common.ConnectionManager.java

## Executable classes
- main.java.crawler.Driver

This contains the main method to run the crawler + indexer. Execute this how you would normally
execute a class with a main method. Once this has been executed, you will have populated the DB with crawled data.

- main.java.com.cli.QueryCLI

This contains the main method to run the search engine CLI. Execute this how you would normally
execute a class with a main method.

## Running the Web App
- Right click on the app -> Run on Server
- Go to http://localhost:8080/CrawlSearch/index.html
- For using the JSON API
```
curl --location --request GET 'http://localhost:8080/CrawlSearch/search?querytext=foo&json=true'
```
