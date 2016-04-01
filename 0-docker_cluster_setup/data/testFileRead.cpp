#include <fstream>
#include <string>
#include <algorithm>
#include <iostream>

void error(const char *msg){
  perror(msg);
  exit(1);
}

int main(int argc, char *argv[])
{
  if (argc != 2) {
    fprintf(stderr,"ERROR, either file path or port is not provided\n");
    exit(1);
  }

  std::string fileline;
  std::ifstream myfile (argv[1]);

  if (!myfile.is_open()){
    error("ERROR on opening file!");
    exit(1);
  }

  while (true){
    if (!getline(myfile,fileline)) {
      myfile.clear();
      myfile.seekg(0, std::ios::beg);
      getline(myfile, fileline);
    }
    std::transform(fileline.begin(), fileline.end(),fileline.begin(), ::toupper);
    fileline += '\n';
    std::cout << fileline;
  }
  return 0;
}
