expname=$1
iters=$2

mkdir $expname
sudo bash jikes.sh jython old $((iters+1)) $expname
sudo bash jikes.sh pmd old $((iters+1))  $expname
sudo bash jikes.sh xalan old $((iters+1))  $expname
sudo bash jikes.sh sunflow new $iters   $expname
sudo bash jikes.sh fop old $((iters+1)) $expname
sudo bash jikes.sh avrora new $iters  $expname
sudo bash jikes.sh hsqldb old $((iters+1)) $expname
sudo bash jikes.sh luindex new $iters   $expname
sudo bash jikes.sh lusearch new $iters   $expname
mv *execution_time $expname
mv *kenan_energy   $expname
