##Before calling this script, please first make a call to the calculate_execution_times inside the experiment directory
exp=$1
bs=$2
for benchdir in $1/kenan_*;
do
	echo $benchdir
	bench=$(echo $benchdir | cut -f 2 -d '_')	
	for exec in $benchdir/*_execution_time;
	do
		etime=$(head -1 $exec)
		echo $etime
	done		
done;
