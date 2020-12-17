name=$1
settings=$2
array=()

if [ ! -d $name ];
then
	mkdir $name
fi

array+=("pmd")
#array+=("avrora")
#array+=("fop")
#array+=("jython")
#array+=("lusearch")
#array+=("luindex")
#array+=("sunflow")
#array+=("bloat")
#array+=("antlr")
#array+=("lusearch")
#array+=("luindex")
#array=(pmd lusearch luindex avrora jython fop sunflow antlr bloat)
#array=(pmd luindex avrora jython fop sunflow antlr)
for i in "${array[@]}"
do
	bash optimize_bench_top.sh $i $name $settings
done
