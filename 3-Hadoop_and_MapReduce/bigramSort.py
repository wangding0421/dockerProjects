import sys
import numpy as np
if len(sys.argv) != 2:
    print "Please provide the output file name!"
    exit()

s_outputFile = sys.argv[1]

with open(s_outputFile) as f:
    sl_verifyData = (map(lambda x: x.split('\t'), f.readlines()))

#Raw data, sorted by value
sl_verifyData = sorted(sl_verifyData, key=lambda a_entry: a_entry[1], reverse=True) 

#Total number of bigrams
i_numBigram = sum(np.array(map(int, np.array(sl_verifyData)[:,1])))

#The Max frequency bigram
l_maxBigram = sl_verifyData[0]

#The 10 percent number needed
i_10percent = (i_numBigram)/10

#The type of bigram needed for the 10 percent
i_10typeBigram = 0
print "1. The total number of bigrams: \t", i_numBigram
print "2. The most common bigram:"
for i in range(len(sl_verifyData)):
    if (sl_verifyData[i][1] == l_maxBigram[1]):
        print '%s\t%s' % (sl_verifyData[i][0], sl_verifyData[i][1])
    if (i_10percent > 0):
        i_10percent -= int(sl_verifyData[i][1])
        i_10typeBigram += 1
print "3. The number of bigrams required to add up to 10% of all bigrams: \t", i_10typeBigram

