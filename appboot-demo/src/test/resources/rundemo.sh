#!/usr/bin/env bash
WORKDIR=`pwd`
SCRIPTDIR=`dirname $0`
cd $SCRIPTDIR
java -Dapp.maven.test -jar ../dependency/appboot.jar app.main.class=com.github.fwi.appboot.Demo app.boot.debug "$@"
cd $WORKDIR