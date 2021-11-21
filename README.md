# IS Project 2021

## Project structure

## Setup
- Clone the repository -
```git clone git@git.cs.uni-kl.de:dbis/is-project-21/group-03.git```
- Import project filesystem, located at the cloned repository.
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

- Create new project -> Dynamic Web Project
- Import project into this newly created project from the directory cloned earlier
- Right click on project -> Properties -> Project Facets
  - The following should be selected:
  - Dynamic Web Module 3.1
  - Java 1.8, Runtime - apache-tomcat-9.x.x

- Right click on the project -> New -> Other -> Server -> Apache -> Tomcat v9.0 Server -> Next -> Download and Install (install it at the same folder as your project root folder)
- Configure build path
  - Right click project root -> Build Path -> Configure build path
    - -> Libraries -> Add Library -> Server Runtime -> apache-tomcat-9.0.46 -> Finish
    - Libraries -> Add Library -> Web App Libraries
    - -> Add External JARS -> Navigate to the 'lib' folder -> Select the 2 JARS 'jtidy-r938.jar' and 'postgresql-42.3.1jar'


## Executable classes
- main.java.crawler.Driver

This contains the main method to run the crawler + indexer. Execute this how you would normally
execute a class with a main method. Once this has been executed, you will have populated the DB with crawled data.

- main.java.com.cli.QueryCLI

This contains the main method to run the search engine CLI. Execute this how you would normally
execute a class with a main method.

