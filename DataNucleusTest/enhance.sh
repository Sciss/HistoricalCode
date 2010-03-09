#!/bin/sh
echo "OBSOLETE !!!! PLEASE USE ant enhance INSTEAD !!!!"

cp resources/package-hsql.orm out/production/DataNucleusTest/de/sciss/datanucleustest
java -cp out/production/DataNucleusTest:libraries/datanucleus-enhancer.jar:libraries/datanucleus-core.jar:libraries/jdo2-api.jar:libraries/asm.jar org.datanucleus.enhancer.DataNucleusEnhancer out/production/DataNucleusTest/de/sciss/datanucleustest/package-hsql.orm