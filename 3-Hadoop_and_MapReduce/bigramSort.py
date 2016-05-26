import sys
import numpy as np
if len(sys.argv) != 2:
    print "Please provide the output file name!"
    exit()

s_outputFile = sys.argv[1]

with open(s_outputFile) as f:
    sl_verifyData = (map(lambda x: x.split('\t'), f.readlines()))

sl_verifyData = sorted(sl_verifyData, key=lambda a_entry: a_entry[1], reverse=True) 

print "Sorted bigram result:"
for pair in sl_verifyData:
    print '%s\t%s' % (pair[0], pair[1])
