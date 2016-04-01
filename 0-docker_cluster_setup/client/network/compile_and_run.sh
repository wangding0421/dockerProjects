WORK_DIR=$1
DATA=$2
SERVER_IP=$3
PORT=$4

g++ -o $WORK_DIR/client.out $WORK_DIR/client.cpp
$WORK_DIR/client.out $DATA $SERVER_IP $PORT
