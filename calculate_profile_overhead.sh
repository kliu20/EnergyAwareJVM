##Before calling this script, please first make a call to the calculate_execution_times inside the experiment directory
exp=$1
bs=$2
echo "" > $1_overhead
for benchdir in $1/kenan_*;
do
	echo $benchdir
	bench=$(echo $benchdir | cut -f 2 -d '_')	
	for exec in $benchdir/*_execution_time;
	do
		etime=$(head -1 $exec)
		freq=$(echo $exec | cut -f 3 -d '/')
		freq=$(echo $freq | cut -f 1 -d '_')
		
		#echo $etime
		btime="$bs/${bench}_${freq}_execution_time"
		btime=$(head -1 $btime)
		#echo $btime
		extra=$(($etime - $btime))
		engr="scale=5;$extra / $btime"
		engr=$(bc -l <<< $engr)
		#echo $engr
		echo "$bench,$freq,$engr" >> $1_overhead
	done		
done;
