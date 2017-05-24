## Arabic Language :
Arabic is one of the most spoken languages in the world, with more than 400 million speakers. Arabic is right to left, with 28 letters shaped based on the position in the word and  a special letter Hamza that can be held on other letters. Arabic uses diacritics for short vowels. Arabic is based on the structure of patterns and roots. Most Arabic words are generated from a finite set of roots (about 7000 roots) transformed using one or more patterns (about 400-500). Theoretically, a single Arabic root can generate hundreds of words (noun, verb, ...). An Arabic word can exist in about a hundred of forms in a normal text by adding certain suffixes and prefixes (mainly considered as stop-words in English).
ref:[Proposal of an Advanced Retrieval System for Noble Qur’an](https://www.slideshare.net/AssemCHELLI/main-30182032)
## Stemming :
The Arabic word has many different level of derivations. Those levels are the word, the word without affixes (Lemma), the stem, or the root. Stemming we either bring it back into stem or root origin. The roots in Arabic are the basic unit. There are about 10000 roots in Modern Arabic. When pairing with patterns, the root can generate more than 1000 variant words. Generated words could have similar, independent or opposite meanings. In the contrary, the stem usually generate a small set of words that have a similar meaning. Thus, we recommend use the stem as the landmark for this phase. ref:[Proposal of an Advanced Retrieval System for Noble Qur’an ](https://www.slideshare.net/AssemCHELLI/main-30182032)

## Snowball :
Snowball is a small string processing language designed for creating stemming algorithms for use in Information Retrieval. This site describes Snowball, and presents several useful stemmers which have been implemented using it.

The Snowball compiler translates a Snowball script into another language - currently ISO C, Java and Python are supported.

"""
Since it effectively provides a ‘suffix STRIPPER GRAMmar’, I had toyed with the idea of calling it ‘strippergram’, but good sense has prevailed, and so it is ‘Snowball’ named as a tribute to SNOBOL, the excellent string handling language of Messrs Farber, Griswold, Poage and Polonsky from the 1960s.
""" [Martin Porter]()
## Snowball Arabic Stemmer :
SAS :  An algorithm for Arabic stemming written on Snowball framework language. If offers light stemming and root-based stemming and text normalization. voc
