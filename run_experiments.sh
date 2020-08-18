expname=$6
benchname=$1
bash rapl.sh $1 $2 $3 $4 $5
bash rapl_on_demand.sh $1 $2 $3 $4 $5

benchdir="kenan_$benchname"
mkdir $benchdir
mv counter_based* $benchdir
mv freq_* $benchdir
bash graph_experiments.sh $benchdir
mv $benchdir $expname/

