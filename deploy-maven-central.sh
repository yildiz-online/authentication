#!/bin/bash

echo "Building $BRANCH branch"

if [ "$BRANCH" = "develop" ]; then
   mvn -V -s settings.xml org.jacoco:jacoco-maven-plugin:prepare-agent clean package sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=$SONAR_ORGANIZATION -Dsonar.login=$SONAR
elif [ "$BRANCH" = "master" ]; then
  mvn -V -s settings.xml clean package
else
  mvn -V -s settings.xml clean package
fi