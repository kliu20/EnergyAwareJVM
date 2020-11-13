echo "" > "samples.csv"
for bench in kenan_*
do
	benchname=$(echo $bench | cut -d'_' -f 2)
	cd $bench
	for freq in {0..12}
	do
		ffile="$freq.csv"
		pwd
		echo $ffile
		if [ -f "$ffile" ]
		then
			lc=$(wc -l $ffile)
			lc=$(echo $lc | cut -d' ' -f 1)
			echo "$benchname,$freq,$lc" >> ../samples.csv
		fi
	done
	cd ../
done
