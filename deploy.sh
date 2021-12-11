#!/bin/bash

eval "$(ssh-agent -s)"
ssh-add -k /home/project/.ssh/y
cd /home/project/group-03/.git/objects
sudo chown -R project:project *

cd /home/project/group-03
git pull origin main

sudo service tomcat9 stop
sudo cp /home/project/group-03/CrawlSearch.war /var/lib/tomcat9/webapps/
sudo service tomcat9 start