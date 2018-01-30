for f in *.java
do
	path=$(echo $f | sed s#_#/#g)	
	cp $f ../$path
done

