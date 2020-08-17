# kabod-batch-processing @ We Get IT

## Description
Kabod is a simple project that shows capabilities of spring batch processing.

## System requirements
 - JDK 11+
 - Maven 3.6.1+
 - Docker 19.03.12+

## Services
  - data-feed - service that produces every minute 5 bank transaction csv file containing between 10000 and 12000 records (between 50k and 60k records produced)
  - data-processor - service using spring batch that process csv files and persists them into mongo storage.

## Build tools and Docker
  - build maven project, docker images and run services ```docker_build_deploy.sh```