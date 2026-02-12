# Assem's Arabic Stemmer [![DOI](https://zenodo.org/badge/49428595.svg)](https://zenodo.org/badge/latestdoi/49428595)
This is an algorithm for Arabic stemming written in Snowball framework language. It offers light stemming, text normalization, and comprehensive stopword filtering. 

```bibtex
@article{Chelli2018,
author = "Assem Chelli",
title = "{Assem's Arabic Stemmer}",
year = "2018",
month = "11",
url = "https://figshare.com/articles/Assem_s_Arabic_Stemmer/7295690",
doi = "10.6084/m9.figshare.7295690.v1"
}
```

This is a sample of results:

Word | Light Stemmer | Root-Based Stemmer
------------ | ------------- | ------------
طفل | طفل  | طفل
اطفال | اطفال  | طفل
الاطفال | اطفال  | طفل
اطفالكم | اطفال  | طفل
فأطفالكم | اطفال  | طفل
اطفالهم | اطفال  | طفل
والاطفال | اطفال| طفل
فاطفالهم | اطفال  | طفل
وطفل | طفل  | طفل
الطفولة | طفول  | طفل
  والطفلتين | طفل |طفل
طفلتان | طفل | طفل



## Requirements:

- [Snowball framework](https://github.com/snowballstem/snowball)
- [Snowball-data](https://github.com/snowballstem/snowball-data)
- [Golden-Arabic-Corpus](https://github.com/LBenzahia/golden-corpus-arabic/archive/master.zip)

They are already attached as git submodules so just run:
```sh
$ git submodule update --init --recursive
```
## Build:
```sh
$ make build
```

## Run:
- Light Stemmer
```sh
$ make run
الطالب
طالب
```
- Root-Based Stemmer
```sh    
$ make run_root
الطالب
طلب
```

## Test:
We configured tests to run against snowball-data arabic sample to test speed, grouping factor and precision.
```sh
$ make test
```

## Arabic Stoplist:
A comprehensive Arabic stopword list is included with the following features:
- **156+ high-frequency Arabic stopwords** organized into 8 categories
- **English translations** for each stopword
- **Category classification**: articles, pronouns, prepositions, conjunctions, particles, verbs, demonstratives, interrogatives
- **Frequency-based filtering**: stopwords ranked by usage frequency
- **Affix support**: Regular expression patterns for common Arabic prefixes (و، ف، ب، ك، ل، ال) and suffixes (possessive pronouns, plural markers)

### Usage:
```python
from stopwords import ArabicStoplist

# Initialize stoplist
stoplist = ArabicStoplist()

# Check if a word is a stopword
print(stoplist.is_stopword('في'))  # True
print(stoplist.is_stopword('كتاب'))  # False

# Check with affix support (matches والذي = و + الذي)
print(stoplist.is_stopword('والذي', use_affixes=True))  # True

# Get stopwords by category
prepositions = stoplist.get_stopwords_by_category('preposition')
pronouns = stoplist.get_stopwords_by_category('pronoun')

# Get high-frequency stopwords only (rank <= 50)
top_stopwords = stoplist.get_stopwords(max_frequency_rank=50)

# Filter stopwords from text
words = ['الكتاب', 'في', 'المكتبة', 'على', 'الطاولة']
filtered = stoplist.filter_stopwords(words)
print(filtered)  # ['الكتاب', 'المكتبة', 'الطاولة']
```

See [stopwords/README.md](stopwords/README.md) for detailed documentation.

## Distributions:
- dist light stemmer to available languages:
```sh
$ make dist
```


