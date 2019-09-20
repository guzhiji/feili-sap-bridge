#!/usr/bin/env bash

KEY_FILE=src/main/resources/feili-sap-bridge.p12

if [ ! -f "$KEY_FILE" ] ; then
	echo 'Generating keystore file'
	keytool -genkeypair -alias feili-sap-bridge -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore "$KEY_FILE" -validity 3650
else
	echo 'Keystore file already exists.'
fi

