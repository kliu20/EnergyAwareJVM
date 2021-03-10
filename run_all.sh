expname=$1
iters=$2
samples="1"
ondemand=$3
period="2"
#if [ -d $expname ]
#then
#	rm -r $expname
#fi

mkdir $expname



##Todo The number of samples for bloat is 64 ... but were are experimenting with 8 to see how different the results are ...

sudo bash run_experiments.sh sunflow new 10 8 2  $expname
sudo bash run_experiments.sh jython old $iters 64 2 $expname
sudo bash run_experiments.sh pmd old $((iters)) 64 2 $expname
sudo bash run_experiments.sh xalan old $((iters)) 64 2 $expname
sudo bash run_experiments.sh fop old $((iters)) 128 2 $expname
sudo bash run_experiments.sh avrora new 5 32 2 $expname
sudo bash run_experiments.sh bloat old $((iters)) 64 2 $expname
sudo bash run_experiments.sh antlr old $((iters)) 64 2 $expname
sudo bash run_experiments.sh luindex new $iters 64 2 $expname
sudo bash run_experiments.sh lusearch new 10 32 2 $expname
