#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import sys
import json
import io

# Make it work for Python 2+3 and with Unicode
try:
    to_unicode = unicode
except NameError:
    to_unicode = str

STEMS, ROOTS, WORDS = sys.argv[1:]

# Read JSON file
with open('golden_corpus/build/golden_corpus_arabic.json') as data_file:
    golden_corpus = json.load(data_file)

s = open(STEMS)
r = open(ROOTS)
w = open(WORDS)

line1 = s.readline()
line2 = w.readline()
line3 = r.readline()

i = cpt_stems = cpt_roots = 0

while( line1 and line2 and line3 and i < len(golden_corpus)- 2):
    stem = line1[:-1].decode("utf-8")
    word = line2[:-1].decode("utf-8")
    root = line3[:-1].decode("utf-8")
    if word == golden_corpus[i]["word"]:
        if stem == golden_corpus[i]["stem"]:
            cpt_stems = cpt_stems + 1
        if root == golden_corpus[i]["root"]:
            cpt_roots = cpt_roots + 1
    line1 = s.readline()
    line2 = w.readline()
    line3 = r.readline()
    i = i + 1

stemsSuccessPercent = (cpt_stems*100)/float(len(golden_corpus))
rootssSuccessPercent = (cpt_roots*100)/float(len(golden_corpus))

print "======================================================"
print "================= Test arabic-stemmer ================"
print "================= with Golden_Corpus  ================"
print "======================================================"
print "success rate stems = {:0.2f} %".format(stemsSuccessPercent)
print "success rate roots = {:0.2f} %".format(rootssSuccessPercent)
print cpt_stems," stem cases are passed from: ",len(golden_corpus)
print cpt_roots," root cases are passed from: ",len(golden_corpus)
print "======================================================"
print "=================     End Test        ================"
print "======================================================"
