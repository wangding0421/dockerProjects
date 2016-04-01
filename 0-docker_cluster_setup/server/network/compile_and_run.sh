WORK_DIR=$1
DATA=$2
PORT=$3

g++ -o $WORK_DIR/server.out $WORK_DIR/server.cpp
$WORK_DIR/server.out $DATA $PORT
