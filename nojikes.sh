#!/bin/bash
DEBUG=false
pbench=$1
ptype=$2
iters="$3"
expected=$iters

echo "$1-$2-$3-$4-$5"

if [ "$ptype" == "old" ];
then
	dacapoJar="dacapo-2006-10-MR2.jar"
	callbackClass="kenan.OIterationCallBack"
	expected=$((iters-1))
else

	callbackClass="kenan.NoJikesIterationCallBack"
	dacapoJar="dacapo-9.12-bach.jar"
fi	

#callbackClass="kenan.IterationCallBack"
bench="$pbench"
size="default"
freqOpt=8
freq=('0' '2201000' '2200000' '2100000' '2000000' '1900000' '1800000' '1700000' '1600000' '1500000' '1400000' '1300000' '1200000')
#freq=('0' '2601000' '2400000' '2200000' '2000000' '1800000' '1600000' '1400000' '1200000' '2600000');
events=('cache-misses' 'cache-references' 'cpu-cycles' 'branches' 'branch-misses' 'cpu-clock' 'page-faults' 'context-switches' 'cpu-migrations');
timeSlice=('0' '8.0' '4.0' '2.0' '1.0' '0.5' '0.25' '0.125')
hotMin=('0' '50' '100' '150' '200' '250' '300' '350' '400')
hotMax=('0' '100' '150' '200' '250' '300' '350' '400' '1000000')
threads=('2' '4' '8')
eventNum=8
freqScaling=1
kkfreq="0"
#cache-misses,cache-references

runJikesProfile() {
		java "-cp" "$dacapoJar:." "Harness" "-s" "$size" "-n" "${iters}" "-c" "$callbackClass"  "$bench"
}


sudo java energy.Scaler 1 userspace
./read_energy.o > start_energy
runJikesProfile
./read_energy.o > end_energy
starte=$(head -1 start_energy)
ende=$(head -1 end_energy)
echo $starte
echo $ende
call="print(-${starte} + ${ende})"
python -c "$call"
#echo $((ende - starte))

