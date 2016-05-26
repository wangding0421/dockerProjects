#########################################################################
# Config vars
#########################################################################
# Set to the name of the Docker machine you want to use
DOCKER_MACHINE_NAME=default

# Master Image Name
IMAGE=dingwang/hadoop-master

# Set the names of the Docker containers for corresponding images
MASTER_CONTAINER=master
SLAVE_CONTAINER=slave

# Set the local directories to the server and the client
LOCAL_DIR=$(pwd)

# Set the image directories
WORK_DIR='/root/RMI'

# Set the idNumber and the output file name
N=5
OUTPUT_FILE=bigramResult
#########################################################################
# Create Docker machine (if neccesary)
#########################################################################

echo "-----------------------------------------------------------"
echo "List of existing Docker machines"
echo "-----------------------------------------------------------"
docker-machine ls
echo "-----------------------------------------------------------"

# Machine exists and is Running
if (docker-machine ls | grep "^$DOCKER_MACHINE_NAME .* Running"); then
    echo "-----------------------------------------------------------"
    echo "Machine exists and is already running"
    echo "Moving over to next step ..."
    echo "-----------------------------------------------------------"
else

    # Machine doesnt exist
    if !(docker-machine ls | grep "^$DOCKER_MACHINE_NAME "); then
        echo "-----------------------------------------------------------"
        echo "Creating Docker machine: $DOCKER_MACHINE_NAME"
        echo "-----------------------------------------------------------"
        docker-machine create --driver=virtualbox $DOCKER_MACHINE_NAME
    fi

    # Machine exists but Stopped
    if (docker-machine ls | grep "^$DOCKER_MACHINE_NAME .* Stopped"); then
        echo "-----------------------------------------------------------"
        echo "Starting Docker machine ... $DOCKER_MACHINE_NAME"
        echo "-----------------------------------------------------------"
        docker-machine start $DOCKER_MACHINE_NAME
    fi
fi

#########################################################################
# Build images:
#########################################################################
echo "-----------------------------------------------------------"
echo "Building images"
echo "-----------------------------------------------------------"
eval $(docker-machine env $DOCKER_MACHINE_NAME) 
# Build the client and server ubuntu Images
docker pull kiwenlau/hadoop-master:0.1.0
docker pull kiwenlau/hadoop-slave:0.1.0
docker pull kiwenlau/hadoop-base:0.1.0
docker pull kiwenlau/serf-dnsmasq:0.1.0
docker build -t $IMAGE ./hadoop-master/

# delete old master container and start new master container
docker rm -f $MASTER_CONTAINER &> /dev/null
echo "start master container..."
docker run -d -t --dns 127.0.0.1 -P --name $MASTER_CONTAINER -v $(pwd)/codes/:/root/codes/ -v $(pwd)/input/:/root/input/ -h master.kiwenlau.com -w /root dingwang/hadoop-master &> /dev/null

# get the IP address of master container
FIRST_IP=$(docker inspect --format="{{.NetworkSettings.IPAddress}}" $MASTER_CONTAINER)

# delete old slave containers and start new slave containers
i=1
while [ $i -lt $N ]
do
	  docker rm -f $SLAVE_CONTAINER$i &> /dev/null
	  echo "start slave$i container..."
	  docker run -d -t --dns 127.0.0.1 -P --name $SLAVE_CONTAINER$i -h slave$i.kiwenlau.com -e JOIN_IP=$FIRST_IP kiwenlau/hadoop-slave:0.1.0 &> /dev/null
	  i=$(( $i + 1 ))
done 


# create a new Bash session in the master container
docker exec -it $MASTER_CONTAINER bash ./run-wordcount.sh

docker cp master:/root/$OUTPUT_FILE ./$OUTPUT_FILE

python bigramSort.py $OUTPUT_FILE

rm $OUTPUT_FILE

docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

