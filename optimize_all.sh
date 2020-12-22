name=$1
settings=$2
trace=$3
array=()

if [ ! -d $name ];
then
	mkdir $name
fi

#array+=("pmd")
#array+=("avrora")
#array+=("fop")
#array+=("jython")
#array+=("lusearch")
#array+=("luindex")
#Earray+=("sunflow")
#array+=("bloat")
#array+=("antlr")
#array+=("lusearch")
#array+=("luindex")
#array=(pmd lusearch luindex avrora jython fop sunflow antlr bloat)
#array=(sunflow antlr bloat jython lusearch luindex avrora fop pmd)
#array=(pmd sunflow antlr bloat jython fop avrora lusearch luindex)
#array=(pmd avrora lusearch luindex)
#array=(luindex avrora fop pmd lusearch)

array=("lusearch")
for i in "${array[@]}"
do
	bash optimize_bench.sh $i $name $settings $trace
done
