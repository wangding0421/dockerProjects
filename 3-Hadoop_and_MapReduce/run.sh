#########################################################################
# Config vars
#########################################################################
# Set to the name of the Docker machine you want to use
DOCKER_MACHINE_NAME=default

# Set to the names of the Docker images you want to use
IMAGE=sequenceiq/hadoop-docker:2.7.0

# Set the names of the Docker containers for corresponding images
SERVER_CONTAINER=server
CLIENT_CONTAINER=client

# Set the local directories to the server and the client
LOCAL_DIR=$(pwd)

# Set the image directories
WORK_DIR='/root/RMI'

# Set the idNumber and the output file name
ID_NUM=100
OUTPUT_FILE=clientOutput
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
docker build -t $IMAGE $LOCAL_DIR

#########################################################################
# Start running of 2 containers
#########################################################################
echo "-----------------------------------------------------------"
echo "Start running of 2 containers"
echo "-----------------------------------------------------------"


#docker run -itd --name $SERVER_CONTAINER -v $LOCAL_DIR:$WORK_DIR $IMAGE bash $WORK_DIR/compile_and_runServer.sh

#SERVER_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' $SERVER_CONTAINER)

#docker run -itd --name $CLIENT_CONTAINER -v $LOCAL_DIR:$WORK_DIR $IMAGE bash $WORK_DIR/compile_and_runClient.sh $SERVER_IP $ID_NUM

#docker logs $CLIENT_CONTAINER
#docker stop $CLIENT_CONTAINER $SERVER_CONTAINER
#docker rm $CLIENT_CONTAINER $SERVER_CONTAINER
