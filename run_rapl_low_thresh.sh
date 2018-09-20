#!/bin/bash
DEBUG=false
freqOpt=8
freq=('0' '2601000' '2400000' '2200000' '2000000' '1800000' '1600000' '1400000' '1200000' '2600000');
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
		sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx4000M" "-X:vm:errorsFatal=true" "-X:vm:interruptQuantum=${4}" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=0.1" "-X:aos:hot_method_time_max=1" "-X:aos:frequency_to_be_printed=${2}" "-X:aos:event_counter=${3}" "-X:aos:enable_counter_profiling=true" "-X:aos:enable_energy_profiling=true" "-X:aos:profiler_file=${4}_threads_proportional_sampling2.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-jar" "dacapo-9.12-bach.jar" "-s" "large" "sunflow" 
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

#runJikesNoEnergyLusearch 8 ${freq[3]} ${events[0]},${events[1]} ${events[0]} -t ${threads[1]}
#runJikesNoCounterLusearch 8 ${freq[3]} ${events[0]},${events[1]} ${events[0]} -t ${threads[1]}
#Execute Jikes to measure hardware counters only
#for((j=1;j<=8;j++))
#do
#	for((i=1;i<=9;i++))
#	do
#		for((k=0;k<=2;k++))
#		do
#			sudo java energy.Scaler $i userspace
#			runJikesNoEnergyHSQLDB ${j} ${freq[$i]} ${events[0]},${events[1]} ${events[0]} -t ${threads[$k]}
#			runJikesNoEnergyHSQLDB ${j} ${freq[$i]} ${events[2]} ${events[2]} -t ${threads[$k]}
#
#			runJikesNoEnergyHSQLDB ${j} ${freq[$i]} ${events[4]},${events[3]} ${events[4]} -t ${threads[$k]}
#
#			runJikesNoEnergyHSQLDB ${j} ${freq[$i]} ${events[5]} ${events[5]} -t ${threads[$k]}
#
#			runJikesNoEnergyHSQLDB ${j} ${freq[$i]} ${events[6]} ${events[6]} -t ${threads[$k]}
#
#		done
#	done
#done
#echo "Hot method filter ${j} in perf counter measurement is finished" >> progress.txt
#
#echo "Perf counter measurement is finished" >> progress.txt
##Execute Jikes to measure energy only
#
#for((j=1;j<=8;j++))
#do
#	for((i=1;i<=9;i++))
#	do
#		sudo java energy.Scaler $i userspace
#		runJikesNoCounterHSQLDB ${j} ${freq[$i]} dummy Energy
#	done
#done
#echo "Hot method filter ${j} in energy measurement is finished" >> progress.txt
#
#runJikesNoCounterHSQLDB 7 ${freq[7]} dummy Energy -t ${threads[$k]}
#Execute Jikes to measure hardware counters only
#for((j=1;j<=8;j++))
#do
#	for((i=1;i<=9;i++))
#	do
#		for((k=0;k<=2;k++))
#		do
#			sudo java energy.Scaler $i userspace
#			runJikesNoEnergyProfile ${j} ${freq[$i]} ${events[0]},${events[1]} ${events[0]} -t ${threads[$k]}
#			runJikesNoEnergyProfile ${j} ${freq[$i]} ${events[2]} ${events[2]} -t ${threads[$k]}
#
#			runJikesNoEnergyProfile ${j} ${freq[$i]} ${events[4]},${events[3]} ${events[4]} -t ${threads[$k]}
#
#			runJikesNoEnergyProfile ${j} ${freq[$i]} ${events[5]} ${events[5]} -t ${threads[$k]}
#
#			runJikesNoEnergyProfile ${j} ${freq[$i]} ${events[6]} ${events[6]} -t ${threads[$k]}
#
#		done
#	done
#	echo "Hot method filter ${j} in perf counter measurement is finished" >> progress.txt
#done
#
#echo "Perf counter measurement is finished" >> progress.txt
#Execute Jikes to measure energy only
#for ((j=1;j<=6;j++))
#do
#	timeSlice=$((${timeSlice}))
#		
#	for((i=1;i<=6;i++))
#	do
#		sudo /home/kmahmou1/jdk1.6.0_45/bin/java energy.Scaler $i userspace
#		time runJikesProfile 4 ${freq[$i]} ${events[0]},${events[1]} ${timeSlice[$j]} Energy -t 8 
#		#runJikesProfile 4 ${freq[$i]} ${events[6]} Energy -t 8
#	done
#done
sudo /home/kmahmou1/jdk1.6.0_45/bin/java energy.Scaler 1 userspace
time runJikesProfile 4 ${freq[1]} ${events[0]},${events[1]} ${timeSlice[2]} Energy -t 8 
	
#sudo java energy.Scaler 8 userspace
#runJikesProfile 5 ${freq[1]} ${events[0]},${events[1]} Energy -t 8 

#for ((i=1;i<=9;i++))
#do
#	sudo java energy.Scaler $i userspace
#	runJikesNoProfileWhole ${freq[$i]} 8 &>> fixed_sunflow_sampling.csv
#done

#sudo java energy.Scaler 1 userspace
#runJikesNoProfile 4 ${freq[9]} dummy dummy -t 8
#
#sudo java energy.Scaler 8 userspace
#runJikesNoProfile 4 ${freq[8]} dummy dummy -t 8


#runJikesNoEnergyProfileLusearch 4 ${freq[1]} ${events[5]} ${events[5]} 
#sudo java energy.Scaler 1 userspace
#time runJikesNoEnergyProfile 8 ${freq[1]} ${events[5]} ${events[5]} 
#runJikesNoCounterProfile 7 ${freq[1]} ${events[3]} ${events[3]}
#sudo java energy.Scaler 8 userspace
##runJikesNoEnergyProfile 7 ${freq[8]} ${events[5]} ${events[5]} 
#runJikesNoCounterProfile 7 ${freq[8]} ${events[3]} energy -t 4

#runJikesNoProfile 4 ${freq[1]} ${events[5]} ${events[5]} 
#Default threads
#for((j=1;j<=8;j++))
#do
#	for((i=1;i<=9;i++))
#	do
#		sudo java energy.Scaler $i userspace
#		runJikes ${j} ${freq[$i]} ${events[0]},${events[1]} ${events[0]} "" ""
#		runJikes ${j} ${freq[$i]} ${events[2]} ${events[2]} "" ""
#		runJikes ${j} ${freq[$i]} ${events[4]},${events[3]} ${events[4]} "" ""
#		runJikes ${j} ${freq[$i]} ${events[5]} ${events[5]} "" ""
#		runJikes ${j} ${freq[$i]} ${events[6]} ${events[6]} "" ""
#
#	done
#done

#for((j=1;j<=8;j++))
#do
#for((i=1;i<=9;i++))
#do
#	sudo java energy.Scaler $i userspace
#	runJikesNoProfile ${j} ${freq[$i]} "" ""
#		runJikes ${j} ${freq[$i]} ${events[2]} ${events[2]} "" ""
#		runJikes ${j} ${freq[$i]} ${events[4]},${events[3]} ${events[4]} "" ""
#		runJikes ${j} ${freq[$i]} ${events[5]} ${events[5]} "" ""
#		runJikes ${j} ${freq[$i]} ${events[6]} ${events[6]} "" ""

#done
#done

