a="yeah"
b="yeah"

if [ "$a" = "$b" ]
then
	echo "equal"
fi

ako=$(grep malloc ml)
if [ "$ako" = "" ]
then
	echo "Yeaah"
fi
