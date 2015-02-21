path="$1"
conf="$2"
n="$3"

if [[ path == "" || conf == "" || n == "" ]]; then
	echo "Usage: $0 <path to project> <config file> <number of values per proposer>"
fi

./generate.sh $n > prop1
./generate.sh $n > prop2

cd $path

./acceptor.sh 1 $conf &
./acceptor.sh 2 $conf &
./acceptor.sh 3 $conf &

./learn.sh 1 $conf > ../learn1 &
./learn.sh 2 $conf > ../learn2 &

./leader.sh 1 $conf &
./propose.sh 1 $conf < ../prop1 &
./propose.sh 2 $conf < ../prop2 &
