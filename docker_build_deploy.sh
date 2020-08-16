#!/bin/bash
mvn clean install
docker build -t kabod-data-feed data-feed
docker build -t kabod-data-processor data-processor
docker-compose -f docker-compose-all.yaml up -d