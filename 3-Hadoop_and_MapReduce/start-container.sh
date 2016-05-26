#!/bin/bash

# run N slave containers
N=5

# delete old master container and start new master container
docker rm -f master &> /dev/null
echo "start master container..."
docker run -d -t --dns 127.0.0.1 -P --name master -v $(pwd)/codes/:/root/codes/ -v $(pwd)/input/:/root/input/ -h master.kiwenlau.com -w /root kiwenlau/hadoop-master &> /dev/null

# get the IP address of master container
FIRST_IP=$(docker inspect --format="{{.NetworkSettings.IPAddress}}" master)

# delete old slave containers and start new slave containers
i=1
while [ $i -lt $N ]
do
	docker rm -f slave$i &> /dev/null
	echo "start slave$i container..."
	docker run -d -t --dns 127.0.0.1 -P --name slave$i -h slave$i.kiwenlau.com -e JOIN_IP=$FIRST_IP kiwenlau/hadoop-slave:0.1.0 &> /dev/null
	i=$(( $i + 1 ))
done 


# create a new Bash session in the master container
docker exec -it master bash ./run-wordcount.sh

docker cp master:/root/bigramResult.txt ./bigramResult.txt

python bigramSort.py bigramResult.txt
