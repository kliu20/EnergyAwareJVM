#!/bin/bash

#(cd CPUScaler/; make)
#bin/buildit --java-home $JAVA_HOME localhost FullAdaptiveMarkSweep
#(cd papi-5.4.1/src; make static)
#(cd papi-5.4.1/src/libpfm4/lib; make )
#ant -Dconfig.name=FullAdaptiveMarkSweep -Dconfig.default-heapsize.maximum=3000
#ant -Dconfig.bootimage.compiler=opt -Dconfig.assertions=none -Dconfig.name=FullAdaptiveMarkSweep -Dconfig.runtime.compiler=base 

if [ -d "target" ]; then
	echo "Deleting $(pwd)/target"
	rm -r target
fi

echo "Java Home: $JAVA_HOME"
JAVA_HOME="/home/kmahmou1/jdk1.6.0_45"
echo "++++++++ Printing Java Version ++++++++++++"
java -version
echo "+++++++++++++ Printing Java Version"
PATH=/home/kmahmou1/jdk1.6.0_45/bin:/usr/local/bin:/usr/bin:/bin:/usr/local/games:/usr/games
echo "+++++++++++++java version after path set"
java -version

echo $PATH

ant -Dconfig.include.aos=true -Dconfig.default-heapsize.maximum=5000 -Dconfig.runtime.compiler=opt -Dconfig.bootimage.compiler=opt -Dconfig.assertions=none -Dconfig.include.perfevent=true
#ant -Dconfig.bootimage.compiler=opt -Dconfig.assertions=none 

#to build eclipse
#bin/buildit --eclipse -r .. localhost -j `echo $JAVA_HOME`

#running optimize compiler
#~/workspace/jikesrvm/dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  -X:aos:initial_compiler=opt  energy/test/LoopTest

#run dacapo
#dist/FullAdaptiveMarkSweep_x86_64-linux/rvm "-X:aos:enable_recompilation=false" "-X:aos:initial_compiler=opt" "-jar" "dacapo-9.12-bach.jar" "-s" "small" "avrora"
