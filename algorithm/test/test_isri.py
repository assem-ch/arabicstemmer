#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import sys
import json
import io
from nltk.stem.isri import ISRIStemmer

# Make it work for Python 2+3 and with Unicode
try:
    to_unicode = unicode
except NameError:
    to_unicode = str
# Read JSON file
with open('golden_corpus/build/golden_corpus_arabic.json') as data_file:
    golden_corpus = json.load(data_file)

stemmer = ISRIStemmer()
i = cpt_roots = 0
stemmed = ''
while(i < len(golden_corpus)- 2):
    r = stemmer.stem(golden_corpus[i]["word"])
    if r == golden_corpus[i]["root"]:
        cpt_roots = cpt_roots + 1
    i = i + 1

rootssSuccessPercent = (cpt_roots*100)/float(len(golden_corpus))

print "======================================================"
print "================= Test ISRI-stemmer =================="
print "================= with Golden_Corpus  ================"
print "======================================================"
print "success rate roots = {:0.2f} %".format(rootssSuccessPercent)
print cpt_roots," root cases are passed from: ",len(golden_corpus)
print "======================================================"
print "=================     End Test        ================"
print "======================================================"
