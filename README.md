# IS Project 2021

http://131.246.118.243:8080/is-project/index.html

## Deploying Web Server
Run script `/home/project/deploy.sh`
This just pulls latest code from the repo along with the `.war` file, copies the `.war` file to the Tomcat `webapps` folder and restarts the server.

Script contents:

```
#!/bin/bash

eval "$(ssh-agent -s)"
ssh-add -k /home/project/.ssh/y
cd /home/project/group-03/.git/objects
sudo chown -R project:project *

cd /home/project/group-03
git pull origin main
sudo service tomcat9 stop
sudo cp /home/project/group-03/build/CrawlSearch.war /var/lib/tomcat9/webapps/
sudo service tomcat9 start
```

## Buildig and running crawler
### First run
Resets index and language disctionaries

Run script `/home/project/first_crawl.sh`

This builds the crawler classes and starts a crawl run. Output of the crawl run is appended to a log file `/home/project/crawl_log.txt`

first_crawl.sh
```
#!/bin/bash

cd /home/project/group-03

javac -d bin -cp lib/jtidy-r938.jar:lib/la4j-0.6.0.jar:lib/postgresql-42.3.1.jar:lib/la4j-0.6.0.jar:lib/commons-cli-1.3.1.jar src/com/cli/QueryCLI.java src/com/common/ConnectionManager.java src/com/crawler/Crawler.java src/com/crawler/Driver.java src/com/crawler/Page.java src/com/crawler/Url.java src/com/indexer/Indexer.java src/com/indexer/Stemmer.java src/com/indexer/StopwordRemover.java src/com/indexer/TFIDFScoreComputer.java src/com/scoring/PageRank.java src/com/search/Query.java src/com/search/Result.java src/com/search/ApiResult.java src/com/search/Stat.java com/search/SpellChecker.java src/com/scoring/CombinedScore.java src/com/scoring/Okapi.java src/com/scoring/PageRank.java src/com/scoring/updateMatrix.java src/com/scoring/VectorProc.java src/com/scoring/ViewCreator.java src/com/languageclassifier/LanguageClassifier.java src/com/languageclassifier/DictionaryBootstrapper.java

echo "CRAWL LOG" >> /home/project/crawl_log.txt

echo `date` >> /home/project/crawl_log.txt

java -cp bin:lib/jtidy-r938.jar:lib/la4j-0.6.0.jar:lib/postgresql-42.3.1.jar:lib/commons-cli-1.3.1.jar com.crawler.Driver --maxDocs 50 --maxDepth 10 --fanOut 10 --resetIndex true --resetDict true >> /home/project/crawl_log.txt
```

Subsequent runs

Run script `/home/project/crawl.sh`

This builds the crawler classes and starts a crawl run. Output of the crawl run is appended to a log file `/home/project/crawl_log.sh`

crawl.sh
```
#!/bin/bash

cd /home/project/group-03

javac -d bin -cp lib/jtidy-r938.jar:lib/la4j-0.6.0.jar:lib/postgresql-42.3.1.jar:lib/la4j-0.6.0.jar:lib/commons-cli-1.3.1.jar src/com/cli/QueryCLI.java src/com/common/ConnectionManager.java src/com/crawler/Crawler.java src/com/crawler/Driver.java src/com/crawler/Page.java src/com/crawler/Url.java src/com/indexer/Indexer.java src/com/indexer/Stemmer.java src/com/indexer/StopwordRemover.java src/com/indexer/TFIDFScoreComputer.java src/com/scoring/PageRank.java src/com/search/Query.java src/com/search/Result.java src/com/search/ApiResult.java src/com/search/Stat.java com/search/SpellChecker.java src/com/scoring/CombinedScore.java src/com/scoring/Okapi.java src/com/scoring/PageRank.java src/com/scoring/updateMatrix.java src/com/scoring/VectorProc.java src/com/scoring/ViewCreator.java src/com/languageclassifier/LanguageClassifier.java src/com/languageclassifier/DictionaryBootstrapper.java

echo "CRAWL LOG" >> /home/project/crawl_log.txt

echo `date` >> /home/project/crawl_log.txt

java -cp bin:lib/jtidy-r938.jar:lib/la4j-0.6.0.jar:lib/postgresql-42.3.1.jar:lib/commons-cli-1.3.1.jar com.crawler.Driver --maxDocs 50 --maxDepth 10 --fanOut 10 --resetIndex false --resetDict false >> /home/project/crawl_log.txt
```

## Cron job
Modify cron config with `crontab -e`
View cron config with `crontab -l`

Cron config:
```
0 23 * * * /bin/sh /home/project/crawl.sh
```
The cron job executes the above `/home/project/crawl.sh` file every night at 2300 hrs UTC.


## Miscellaneous

Since Sheet-02 requires that the server URL be `/is-project`
Set application context path in `/var/lib/tomcat9/conf/server.xml`
```
<Context path="/is-project" docBase="CrawlSearch.war" />
```

Checking request access logs
Access logs are available in `/var/lib/tomcat9/logs/localhost_access_log_<date>.txt`

Checking system.out logs
```
journalctl -u tomcat9.service --reverse
```
Check DB
```
sudo -u postgres psql search_engine
```

## API Definition
https://www.postman.com/interstellar-astronaut-887900/workspace/is-project/collection/3190072-d89ed8ac-1330-4d3a-9227-0ae33e8b0258?ctx=documentation


## Eclipse setup
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
