#!/bin/bash
DEBUG=false
freqOpt=8

pbench=$1
ptype=$2

if [ "$ptype" == "old" ];
then
	dacapoJar="dacapo-2006-10-MR2.jar"
	callbackClass="kenan.OIterationCallBack"
	iters="11"
else

	callbackClass="kenan.IterationCallBack"
	dacapoJar="dacapo-9.12-bach.jar"
	iters="10"
fi	


bench="$pbench"
size="default"
freq=('0' '2201000' '2200000' '2100000' '2000000' '1900000' '1800000' '1700000' '1600000' '1500000' '1400000' '1300000' '1200000')
#freq=('0' '2601000' '2400000' '2200000' '2000000' '1800000' '1600000' '1400000' '1200000' '2600000');
events=('cache-misses' 'cache-references' 'cpu-cycles' 'branches' 'branch-misses' 'cpu-clock' 'page-faults' 'context-switches' 'cpu-migrations');
timeSlice=('0' '8.0' '4.0' '2.0' '1.0' '0.5' '0.25' '0.125')
hotMin=('0' '50' '100' '150' '200' '250' '300' '350' '400')
hotMax=('0' '100' '150' '200' '250' '300' '350' '400' '1000000')
threads=('2' '4' '8')
eventNum=8
freqScaling=1

#cache-misses,cache-references

runJikesNoProfileWhole() {
	sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=1" "-X:aos:hot_method_time_max=1" "-X:aos:frequency_to_be_printed=${1}" "-X:aos:event_counter=3" "-X:aos:enable_counter_profiling=false" "-X:aos:enable_energy_profiling=false" "-X:aos:profiler_file=${2}threads.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "EnergyCheckUtils" ${1} ${2}
}


runJikes() {
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=${hotMin[${1}]}" "-X:aos:hot_method_time_max=${hotMax[${1}]}" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:profiler_file=${4}_${6}threads.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-jar" "dacapo-9.12-bach.jar" "-s" "large" "sunflow" ${5} ${6}

}

runJikesNoCounterProfile() {
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=${hotMin[1]}" "-X:aos:hot_method_time_max=${hotMax[8]}" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:enable_counter_profiling=false" "-X:aos:enable_energy_profiling=true" "-X:aos:profiler_file=${4}_${6}threads_proportional_sampling.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-jar" "dacapo-9.12-bach.jar" "-s" "large" "sunflow" ${5} ${6} #"dacapo-2006-10-MR2.jar" "-s" "large" "lusearch" 

}

runJikesNoEnergyProfile() {
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=${hotMin[1]}" "-X:aos:hot_method_time_max=${hotMax[${1}]}" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:enable_counter_profiling=true" "-X:aos:enable_energy_profiling=false" "-X:aos:profiler_file=${4}_${6}threads.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-jar" "dacapo-9.12-bach.jar" "-s" "large" "sunflow" ${5} ${6} 

}
runJikesProfile() {
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx4000M" "-X:gc:eagerMmapSpaces=true"  "-X:vm:errorsFatal=true" "-X:gc:printPhaseStats=true" "-X:vm:interruptQuantum=${4}" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=0.1" "-X:aos:hot_method_time_max=1" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:enable_counter_profiling=false" "-X:aos:enable_energy_profiling=true" "-X:aos:profiler_file=doubleSampleWindow_1ms.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-cp" "$dacapoJar:." "Harness" "-s" "$size" "-n" "$iters" "-c" "$callbackClass" ${bench} &> freq_0
}

runJikesNoEnergyProfileGraphchi() {
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=${hotMin[${1}]}" "-X:aos:hot_method_time_max=${hotMax[${1}]}" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:enable_counter_profiling=true" "-X:aos:enable_energy_profiling=false" "-X:aos:profiler_file=${4}_${6}threads.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-cp" "graphchi-java-0.2.2.jar" "edu.cmu.graphchi.apps.Pagerank" "facebook/414.edges" "20" "edgelist"

}
runJikesNoEnergyHSQLDB() {
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=${hotMin[${1}]}" "-X:aos:hot_method_time_max=${hotMax[${1}]}" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:enable_counter_profiling=true" "-X:aos:enable_energy_profiling=false" "-X:aos:profiler_file=hsqldb_${4}_${6}threads.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-jar" "dacapo-2006-10-MR2.jar" "-s" "large" "hsqldb" 

}

runJikesNoCounterHSQLDB() {
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=${hotMin[${1}]}" "-X:aos:hot_method_time_max=${hotMax[${1}]}" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:enable_counter_profiling=false" "-X:aos:enable_energy_profiling=true" "-X:aos:profiler_file=${4}_threads.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-jar" "dacapo-2006-10-MR2.jar" "-s" "large" "hsqldb"

}


if [ -f kenan.csv ];
then
	rm kenan.csv
fi

i=0
timeSlice=$((${timeSlice}))		
sudo java energy.Scaler 1 ondemand
runJikesProfile 4 ${freq[$i]} ${events[0]},${events[1]} ${timeSlice[2]} Energy -t 8 
sudo mv iteration_times counter_based_sampling_iteration_times_$i
sudo mv kenan.csv counter_based_sampling_kenan.${i}.csv
