input="modified_kenan_files"

while IFS= read -r var
do
	if [ -f $var ]
	then
		new_name=${var:2}
		new_name=$(echo $new_name | sed -r 's#/#_#g')
		cp $var new_normalized/$new_name
	else
		echo $var
	fi

done < "$input"
