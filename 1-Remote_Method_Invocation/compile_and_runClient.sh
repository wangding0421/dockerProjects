SERVER_IP=$1

make

java hellotest/HelloClient $SERVER_IP
