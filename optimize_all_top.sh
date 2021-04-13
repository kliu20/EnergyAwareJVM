name=$1
settings=$2
array=()

if [ ! -d $name ];
then
	mkdir $name
fi

array=(pmd avrora jython fop antlr bloat luindex)
for i in "${array[@]}"
do
	bash optimize_bench_top.sh $i $name $settings
done
