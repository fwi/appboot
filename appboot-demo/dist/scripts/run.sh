#!/bin/bash
# For debugging this script
# set -x

SCRIPTDIR=`dirname $0`
MAINJAR="${SCRIPTDIR}/lib/appboot.jar"

# No need to set app.name since directory already has correct name 
# JAVAPAR="-Dapp.name=appboot-demo"

java $JAVAPAR -jar $MAINJAR "$@"
read -n1 -r -p "Press any key to continue..." key
