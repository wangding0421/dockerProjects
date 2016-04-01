#include <stdio.h>
#include <fstream>
#include <stdlib.h>
#include <string.h>
#include <algorithm>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#define BUFFER_SIZE 256

void error(const char *msg){
  perror(msg);
  exit(1);
}

int main(int argc, char *argv[]){
  int sockfd, newsockfd, portno;
  socklen_t clilen;
  char buffer[BUFFER_SIZE];
  struct sockaddr_in serv_addr, cli_addr;
  int n;

  if (argc != 3) {
    fprintf(stderr,"ERROR, either file path or port is not provided\n");
    exit(1);
  }

  std::string fileline;
  std::ifstream myfile (argv[1]);

  if (!myfile.is_open()){
    error("ERROR on opening file!");
    exit(1);
  }

  sockfd = socket(AF_INET, SOCK_STREAM, 0);
  if (sockfd < 0)
    error("ERROR opening socket");

  bzero((char *) &serv_addr, sizeof(serv_addr));
  portno = atoi(argv[2]);
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = INADDR_ANY;
  serv_addr.sin_port = htons(portno);

  if (bind(sockfd, (struct sockaddr *) &serv_addr,
           sizeof(serv_addr)) < 0)
    error("ERROR on binding");

  listen(sockfd,5);

/*
  Starting connecting...
 */

  clilen = sizeof(cli_addr);
  newsockfd = accept(sockfd,
                     (struct sockaddr *) &cli_addr,
                     &clilen);

  if (newsockfd < 0)
    error("ERROR on accept");

  bzero(buffer,256);

  /*
    While loop for receiving
   */
  while ((n = read(newsockfd,buffer,255)) >= 0){
    if (strcmp(buffer, "LINE\n") == 0){
      if (!getline(myfile,fileline)) {
        myfile.clear();
        myfile.seekg(0, std::ios::beg);
        getline(myfile, fileline);
      }
      std::transform(fileline.begin(), fileline.end(),fileline.begin(), ::toupper);
      fileline += '\n';
      n = write(newsockfd, fileline.c_str() , fileline.length());
    //printf("Message received!\n");
      if (n < 0) error("ERROR writing to socket");
    }
  }

  close(newsockfd);
  close(sockfd);
  return 0;
}

