import sys
if len(sys.argv) != 3:
    print "Please provide the output file name and the idNumber!"
    exit()

s_outputFile = sys.argv[1]
s_idNumber = sys.argv[2]

with open(s_outputFile) as f:
    sl_verifyData = map(lambda x: x.replace("\r\n", ''), f.readlines()[-4:])

i_failCount = 0

for i in range(4):
    if sl_verifyData[i] != "Pong "+s_idNumber:
        i_failCount += 1

print 4 - i_failCount, "Tests Completed,", i_failCount, "Tests Failed."


