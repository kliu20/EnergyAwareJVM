benchname="$1"
settings_path="$2"
experiment_path="$3"


while read p; 
do
	cname=$(echo $p | cut -d',' -f 3)
	mname=$(echo $p | cut -d',' -f 4)
	which=$(echo $p | cut -d',' -f 2)
	for ((i=1;i<=12;i++)); 
	do 
		energy=$(head -1 ${experiment_path}/$benchname/kenan_energy_${benchname}_${i}_$cname.$mname)
		time=$(head -1 ${experiment_path}/$benchname/execution_time_${benchname}_${i}_$cname.$mname)
		log="$benchname,$i,$cname.$mname,$time,$energy"
		echo $log
	done

done < "${settings_path}/${benchname}_settings"



