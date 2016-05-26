#!/bin/bash

$HADOOP_INSTALL/sbin/start-dfs.sh

echo -e "\n"
$HADOOP_INSTALL/sbin/start-yarn.sh

# create input directory on HDFS
$HADOOP_INSTALL/bin/hadoop fs -mkdir -p input

# put input files to HDFS
$HADOOP_INSTALL/bin/hdfs dfs -put ./input/* input

cp /root/codes/mapper.py $(pwd)
cp /root/codes/reducer.py $(pwd)

# run wordcount
$HADOOP_INSTALL/bin/hadoop jar $HADOOP_INSTALL/share/hadoop/mapreduce/sources/hadoop-mapreduce-examples-2.3.0-sources.jar org.apache.hadoop.examples.WordCount input output1

# Bigram Wordcount
$HADOOP_INSTALL/bin/hadoop jar $HADOOP_INSTALL/share/hadoop/tools/lib/hadoop-streaming-2.3.0.jar -input input -output output2 -file mapper.py -file reducer.py -mapper mapper.py -reducer reducer.py

# print the input files
echo -e "\ninput file1.txt:"
$HADOOP_INSTALL/bin/hdfs dfs -cat input/file1.txt

echo -e "\ninput file2.txt:"
$HADOOP_INSTALL/bin/hdfs dfs -cat input/file2.txt

# print the output of wordcount
echo -e "\nwordcount output:"
$HADOOP_INSTALL/bin/hdfs dfs -cat output1/part-r-00000
echo -e "\nwordcount bigram output:"
$HADOOP_INSTALL/bin/hdfs dfs -cat output2/part-00000

# Copy the bigram result to local machine
$HADOOP_INSTALL/bin/hdfs dfs -cat output2/part-00000 > bigramResult
