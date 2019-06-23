#!/usr/bin/env bash

cd `dirname "$0"`

PACKAGE_FILE=sap-bridge.tar.gz

if [ -f "$PACKAGE_FILE" ] ; then
	rm -f "$PACKAGE_FILE"
fi

mvn clean package

cd target

mv libs/sapjco-3.0.jar libs/sapjco3.jar

tar -czf "../$PACKAGE_FILE" *.jar libs

