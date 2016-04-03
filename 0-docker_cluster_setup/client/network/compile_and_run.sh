WORK_DIR=$1
DATA=$2
SERVER_IP=$3
PORT=$4

g++ -o $WORK_DIR/catclient.out $WORK_DIR/catclient.cpp
$WORK_DIR/catclient.out $DATA $SERVER_IP $PORT
