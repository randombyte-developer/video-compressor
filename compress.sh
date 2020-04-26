#!/bin/bash

# rename all *.ts files to *.mp4
find . -type f | perl -pe "print $_; s/(.*)\.ts/\1.mp4/" | xargs -n2 mv


