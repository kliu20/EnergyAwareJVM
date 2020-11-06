expname=$1
bench=$2
for ((i=0;i<=12;i++)); 
do
	time=$(head -1 $expname/${bench}_${i}_execution_time)
	energy=$(head -1 $expname/${bench}_${i}_kenan_energy)
	echo "$bench,$i,$time,$energy"
done
