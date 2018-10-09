#!/bin/bash

events=('cache-misses' 'cache-references' 'cpu-cycles' 'branches' 'branch-misses' 'cpu-clock' 'page-faults' 'context-switches' 'cpu-migrations');
timeSlice=('0' '8.0' '4.0' '2.0' '1.0' '0.5' '0.25' '0.125')
freq=('0' '2201000' '2000000' '1800000' '1600000' '1400000' '1200000');
hotMin=('0' '50' '100' '150' '200' '250' '300' '350' '400')
hotMax=('0' '100' '150' '200' '250' '300' '350' '400' '1000000')
threads=('2' '4' '8')

JAVA_HOME="/home/kmahmou1/jdk1.6.0_45"
PATH="$JAVA_HOME/bin:/usr/local/bin:/usr/bin:/bin:/usr/local/games:/usr/games"

runJikesNoProfile() {

	#sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=1" "-X:aos:hot_method_time_max=1" "-X:aos:frequency_to_be_printed=${1}" "-X:aos:event_counter=3" "-X:aos:enable_counter_profiling=false" "-X:aos:enable_energy_profiling=false" "-X:aos:profiler_file=${2}threads.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "EnergyCheckUtils" ${1} ${2}
	
	sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx2500M" "-X:vm:interruptQuantum=${3}" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=1" "-X:aos:hot_method_time_max=1" "-X:aos:frequency_to_be_printed=${1}" "-X:aos:event_counter=3" "-X:aos:enable_counter_profiling=false" "-X:aos:enable_energy_profiling=false" "-X:aos:profiler_file=${2}threads.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "EnergyCheckUtils" ${1} ${2} ${3}

}

runJikesProfile() {
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx4000M" "-X:vm:errorsFatal=true" "-X:vm:interruptQuantum=${4}" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=0.1" "-X:aos:hot_method_time_max=1" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:enable_counter_profiling=true" "-X:aos:enable_energy_profiling=true" "-X:aos:profiler_file=${4}_threads_proportional_sampling2.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-jar" "dacapo-9.12-bach.jar" "-s" "large" "sunflow" 
}
		#for((k=0;k<=2;k++))
		#do
		#	for ((i=1;i<=9;i++))
		#	do
		#		sudo java energy.Scaler $i userspace
		#		runJikesNoProfile ${freq[$i]} ${threads[$k]} &>> fixed_jython_sampling.csv
		#	done
		#done
		#runJikesNoProfile ${freq[1]} ${threads[1]} 
		#runJikesNoProfile ${freq[1]}  
		#timeSlice=4
		
for ((i=1;i<=6;i++))
do
	sudo java energy.Scaler $i userspace
	time runJikesProfile 4 ${freq[$i]} ${events[0]},${events[1]} ${timeSlice[2]} Energy -t 8 
	#break
done

