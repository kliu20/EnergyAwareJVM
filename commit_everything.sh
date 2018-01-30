all_sources=$(find . -name '*.cpp') 
all_sources+=$(find . -name '*.java')
all_sources+=$(find . -name '*.c')
all_sources+=$(find . -name '*.h')
all_sources+=(find . -name '*.hpp')
all_sources+=(find . -name '*.xml')

#all_sources=()




for f in $all_sources
do
	
	git commit $f
done




