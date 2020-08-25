#!/bin/bash
# Tested using bash version 4.1.5
for ((i=1;i<=20;i++)); 
do
	bash rapl.sh &> logs$i
	mv kenan.csv kenan.${i}.csv
done

