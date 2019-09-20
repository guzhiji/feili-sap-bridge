#!/usr/bin/env bash

cd `dirname "$0"`

PACKAGE_FILE=sap-bridge-spring.tar.gz
JAR_FILE=feili-sap-bridge-spring.jar
JCO_PATH=/d/software/sapjco3/sapjco3.jar

if [ -f "$PACKAGE_FILE" ] ; then
	rm -f "$PACKAGE_FILE"
fi

mvn clean package

if [ ! -f "target/$JAR_FILE" ] ; then
	echo 'packaging failure' >&2
	exit 1
fi
echo 'packaging done'

echo 'adding sapjco3.jar'
cd target
mkdir jar
unzip -q -d jar "$JAR_FILE"

cp "$JCO_PATH" jar/BOOT-INF/lib

echo 're-packaging'
cd jar/BOOT-INF
tar -czf "../../../$PACKAGE_FILE" classes lib

echo 'cleaning'
cd ../..
rm -rf jar

