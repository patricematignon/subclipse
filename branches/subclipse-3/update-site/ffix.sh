#!/bin/bash
usage="usage: ffix.sh <newrev>"
if [ "$#" -ne "1" ];then
    echo $usage
    exit 1
fi
jars=`ls features/*.jar`
rev=$1

for i in $jars;do
    jar -xvf $i
    mv feature.xml feature.bak
    sed "s/${rev}\"Subclipse\"/ label=\"Subclipse\"/" < feature.bak > feature.xml
    jar -uvf $i feature.xml
done
rm feature.bak feature.xml
