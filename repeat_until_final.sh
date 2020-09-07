pbench=$1
ptype=$2
iters=$3
samples="$4"
frequency="$5"
check=$iters
if [ $ptype = "new" ]
then
	echo "Yeah"
	check=$((check+1))	
fi

echo $check
for freq in {0..12}
do
	echo $freq
done
