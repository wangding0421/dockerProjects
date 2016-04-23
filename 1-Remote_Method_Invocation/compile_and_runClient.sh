SERVER_IP=$1
ID_NUM=$2
make

java hellotest/HelloClient $SERVER_IP $ID_NUM
