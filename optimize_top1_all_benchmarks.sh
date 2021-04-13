name=$1
settings=$2
trace=$3
array=()

if [ ! -d $name ];
then
	mkdir $name
fi

array=(pmd avrora fop luindex bloat anltr sunflow)
for i in "${array[@]}"
do
	bash optimize_bench.sh $i $name $settings $trace
done
