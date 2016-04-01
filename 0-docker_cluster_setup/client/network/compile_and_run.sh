PORT=5000

g++ -o /root/network/client.out /root/network/client.cpp
/root/network/client.out /data/string.txt 172.17.0.3 $PORT
