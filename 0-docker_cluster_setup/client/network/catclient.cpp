#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <algorithm>
#include <fstream>
#include <iostream>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#define TIME_PERIOD 30
#define TIME_GAP 3

void error(const char *msg)
{
    perror(msg);
    exit(0);
}

int main(int argc, char *argv[])
{
    int sockfd, portno, n;
    struct sockaddr_in serv_addr;
    struct hostent *server;

    char buffer[256] = "LINE\n";
    char receive_buffer[256];
    if (argc != 4) {
       fprintf(stderr,"usage %s hostname port\n", argv[0]);
       exit(0);
    }

    std::string fileline;
    std::ifstream myfile (argv[1]);

    if (!myfile.is_open()){
      error("ERROR on opening file!");
      exit(1);
    }

    portno = atoi(argv[3]);
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) 
        error("ERROR opening socket");
    server = gethostbyname(argv[2]);
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, 
         (char *)&serv_addr.sin_addr.s_addr,
         server->h_length);
    serv_addr.sin_port = htons(portno);
    if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) 
        error("ERROR connecting");

    for (int i = 0; i <= TIME_PERIOD; i += TIME_GAP) {
      bool hit = false;
      n = write(sockfd,buffer,strlen(buffer));
      if (n < 0)
        error("ERROR writing to socket");
      bzero(receive_buffer,256);
      n = read(sockfd,receive_buffer,255);
      if (n < 0) 
        error("ERROR reading from socket");
      myfile.clear();
      myfile.seekg(0,std::ios::beg);
      while (getline(myfile, fileline)){
        std::transform(fileline.begin(), fileline.end(),fileline.begin(), ::toupper);
        fileline += '\n';
        if (strcmp(fileline.c_str(), receive_buffer) == 0){
          std::cout << "OK\n";
          hit = true;
          break;
        }
      }
      if (!hit)
        std::cout << "MISSING\n";
      usleep(TIME_GAP * 1000000);
    }

    close(sockfd);
    return 0;
}
