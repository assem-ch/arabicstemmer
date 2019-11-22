#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
script for comparing arabic stemmer output
"""
import os
import sys

OUTPUT, VOC, GROUPING = sys.argv[1:]

f = open(OUTPUT)
g = open(VOC)
h = open(GROUPING, "w")

line1 = f.readline()
line2 = g.readline()
word_family={}
cpt=1
while (line1 and line2):
    if cpt%1000000==0:
        print "milestone", cpt
    if  line1[:-1] in word_family:
        word_family[line1[:-1]].append(line2[:-1])
    else:
        word_family[line1[:-1]]=[line2[:-1]]
    line1 = f.readline()
    line2 = g.readline()
    cpt+=1

for k in sorted(word_family):
    h.write(k+" ==> \t"+ " ".join(word_family[k])+"\n")

print "Number of words =", cpt
print "Number of groups =", len(word_family)
print "Grouping percentage =", (len(word_family))/float(cpt)
