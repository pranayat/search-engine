#!/bin/bash

cd /home/project/group-03

javac -d bin -cp lib/jtidy-r938.jar:lib/la4j-0.6.0.jar:lib/postgresql-42.3.1.jar:lib/la4j-0.6.0.jar:lib/commons-cli-1.3.1.jar src/com/cli/QueryCLI.java src/com/common/ConnectionManager.java src/com/crawler/Crawler.java src/com/crawler/Driver.java src/com/crawler/Page.java src/com/crawler/Url.java src/com/indexer/Indexer.java src/com/indexer/Stemmer.java src/com/indexer/StopwordRemover.java src/com/indexer/TFIDFScoreComputer.java src/com/scoring/PageRank.java src/com/search/Query.java src/com/search/Result.java src/com/search/ApiResult.java src/com/search/Stat.java com/search/SpellChecker.java src/com/scoring/CombinedScore.java src/com/scoring/Okapi.java src/com/scoring/PageRank.java src/com/scoring/updateMatrix.java src/com/scoring/VectorProc.java src/com/scoring/ViewCreator.java src/com/languageclassifier/LanguageClassifier.java src/com/languageclassifier/DictionaryBootstrapper.java

echo "CRAWL LOG" >> /home/project/crawl_log.txt

echo `date` >> /home/project/crawl_log.txt

java -cp bin:lib/jtidy-r938.jar:lib/la4j-0.6.0.jar:lib/postgresql-42.3.1.jar:lib/commons-cli-1.3.1.jar com.crawler.Driver --maxDocs 100 --maxDepth 10 --fanOut 10 --resetIndex false --resetDict false >> /home/project/crawl_log.txt