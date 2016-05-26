#!/usr/bin/env python

import sys

for line in sys.stdin:
    words = line.split()
    for i in range(len(words) - 1):
        print '%s\t%s' % ((words[i],words[i+1]), 1)
