expname=$1
iters=$2

mkdir $expname
sudo bash jikes.sh sunflow new 10   $expname
sudo bash jikes.sh jython old 20 $expname
sudo bash jikes.sh pmd old 20  $expname
#sudo bash jikes.sh xalan old 20  $expname
#sudo bash jikes.sh sunflow new 10   $expname
sudo bash jikes.sh fop old 20 $expname
sudo bash jikes.sh avrora new 20 $expname
sudo bash jikes.sh luindex new 20  $expname
#sudo bash jikes.sh lusearch new 20   $expname
sudo bash jikes.sh antlr old 20 $expname
sudo bash jikes.sh bloat old 20 $expname
mv *execution_time $expname
mv *kenan_energy   $expname
mv *iteration_times $expname
mv *iteration_energy $expname
