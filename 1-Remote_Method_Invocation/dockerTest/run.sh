#########################################################################
# Config vars
#########################################################################
# Set to the name of the Docker machine you want to use
DOCKER_MACHINE_NAME=default

# Set to the names of the Docker images you want to use
SERVER_IMAGE=dingchao/javaserver
CLIENT_IMAGE=dingchao/javaclient

# Set the names of the Docker containers for corresponding images
SERVER_CONTAINER=server
CLIENT_CONTAINER=client

# Set the local directories to the server and the client
LOCAL_SERVER_DIR=$(pwd)'/server'
LOCAL_CLIENT_DIR=$(pwd)'/client'

# Set the image directories
WORK_DIR='/root/RMI'

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
