expname=$1
iters=$2

#if [ -d $expname ]
#then
#	rm -r $expname
#fi

#mkdir $expname


#sudo bash run_experiments.sh luindex new $iters 64 2 $expname
#sudo bash run_experiments.sh lusearch new $iters 8 2 $expname
sudo bash run_experiments.sh jython old $((iters+1)) 64 2 $expname
sudo bash run_experiments.sh pmd old $((iters+1)) 64 2 $expname
sudo bash run_experiments.sh xalan old $((iters+1)) 64 2 $expname
sudo bash run_experiments.sh sunflow new $iters 8 2  $expname
sudo bash run_experiments.sh fop old $((iters+1)) 1 256 $expname
sudo bash run_experiments.sh avrora new iters 64 2 $expname
sudo bash run_experiments.sh hsqldb old $((iters+1)) 256 2 $expname

