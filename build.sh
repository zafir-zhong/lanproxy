#!/bin/bash
rm -rf distribution
mvn clean package
cd distribution
bash buildServer.sh