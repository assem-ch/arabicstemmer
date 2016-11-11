#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
script for comparing arabic stemmer output
"""
import os


os.system("make -C ../../snowball/")
os.system("time ../../snowball/stemwords -l ar -i voc.txt -o output.txt")

f = open("output.txt")
g = open("voc.txt")
h = open("grouping.txt", "w")

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
