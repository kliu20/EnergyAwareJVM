name=$1
settings=$2
ssn=$3
frq=$4
array=()

if [ ! -d $name ];
then
	mkdir $name
fi

#array=(sunflow fop luindex bloat antlr jython pmd avrora)
array=(luindex sunflow fop jython  antlr bloat pmd avrora)
#array=(luindex)
#array=(sunflow fop)
#array=(pmd)
#array=(luindex)
#array=(antlr)
for i in "${array[@]}"
do
	bash optimize_bench.sh $i $name $settings $ssn $frq
done

