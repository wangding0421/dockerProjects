#########################################################################
# Config vars
#########################################################################
# Set to the name of the Docker machine you want to use
DOCKER_MACHINE_NAME=default

# Set to the name of the Docker image you want to use
SERVER_IMAGE_NAME=dingwang/ubuntuserver
CLIENT_IMAGE_NAME=dingwang/ubuntuclient

# Set the directory to the server and the client
SERVER_DIR=$(pwd)'/server'
CLIENT_DIR=$(pwd)'/client'
DATA_DIR=$(pwd)'/data'

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
docker build -t dingwang/ubuntuserver $SERVER_DIR
docker build -t dingwang/ubuntuclient $CLIENT_DIR
# Build the data volume container Image
docker build -t dingwang/ubuntudata $DATA_DIR


#########################################################################
# Start running of 3 containers
#########################################################################
echo "-----------------------------------------------------------"
echo "Start running of 3 containers"
echo "-----------------------------------------------------------"

docker run -d --name data dingwang/ubuntudata

docker run -itd --name server -v $SERVER_DIR'/network':/root/network --volumes-from data dingwang/ubuntuserver bash /root/network/compile_and_run.sh
docker run -itd --name client -v $CLIENT_DIR'/network':/root/network --volumes-from data dingwang/ubuntuclient bash /root/network/compile_and_run.sh


#########################################################################
# Log out the client result
#########################################################################
echo "-----------------------------------------------------------"
echo "Log out the client result every 3 seconds: "
echo "-----------------------------------------------------------"
for i in {1..10}; do
    echo "Updated result: "
    docker logs client
    sleep 3
done

docker stop data client server
docker rm data client server
