#!/bin/sh

rm -rf Enormous.app
rsync -avr src/main/app/Enormous.app .
cp target/enomrous-1.0-SNAPSHOT-jar-with-dependencies.jar \
    Enormous.app/Contents/Resources/Java/enormous.jar
