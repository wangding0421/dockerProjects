#########################################################################
# Config vars
#########################################################################
# Set to the name of the Docker machine you want to use
DOCKER_MACHINE_NAME=default

# Set to the names of the Docker images you want to use
SERVER_IMAGE=dingwang/ubuntuserver
CLIENT_IMAGE=dingwang/ubuntuclient
DATA_IMAGE=dingwang/ubuntudata

# Set the names of the Docker containers for corresponding images
SERVER_CONTAINER=server
CLIENT_CONTAINER=client
DATA_CONTAINER=data

# Set the local directories to the server and the client
LOCAL_SERVER_DIR=$(pwd)'/server'
LOCAL_CLIENT_DIR=$(pwd)'/client'
LOCAL_DATA_DIR=$(pwd)'/data'

# Set the image directories
WORK_DIR='/root/network'
DATA='/data/string.txt'

# Set the port
PORT=5000

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
docker build -t $SERVER_IMAGE $LOCAL_SERVER_DIR
docker build -t $CLIENT_IMAGE $LOCAL_CLIENT_DIR
# Build the data volume container Image
docker build -t $DATA_IMAGE $LOCAL_DATA_DIR


#########################################################################
# Start running of 3 containers
#########################################################################
echo "-----------------------------------------------------------"
echo "Start running of 3 containers"
echo "-----------------------------------------------------------"

docker run -d --name $DATA_CONTAINER $DATA_IMAGE

docker run -itd --name $SERVER_CONTAINER -v $LOCAL_SERVER_DIR'/network':$WORK_DIR --volumes-from $DATA_CONTAINER $SERVER_IMAGE bash $WORK_DIR/compile_and_run.sh $WORK_DIR $DATA $PORT

SERVER_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' $SERVER_CONTAINER)

docker run -itd --name $CLIENT_CONTAINER -v $LOCAL_CLIENT_DIR'/network':$WORK_DIR --volumes-from $DATA_CONTAINER $CLIENT_IMAGE bash $WORK_DIR/compile_and_run.sh $WORK_DIR $DATA $SERVER_IP $PORT


#########################################################################
# Log out the client result
#########################################################################
echo "-----------------------------------------------------------"
echo "Log out the client result every 3 seconds: "
echo "-----------------------------------------------------------"
for i in {1..10}; do
    echo "Updated result: "
    docker logs $CLIENT_CONTAINER
    sleep 3
done

docker stop $DATA_CONTAINER $CLIENT_CONTAINER $SERVER_CONTAINER
docker rm $DATA_CONTAINER $CLIENT_CONTAINER $SERVER_CONTAINER
