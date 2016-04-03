WORK_DIR=$1
DATA=$2
PORT=$3

g++ -o $WORK_DIR/catserver.out $WORK_DIR/catserver.cpp
$WORK_DIR/catserver.out $DATA $PORT
