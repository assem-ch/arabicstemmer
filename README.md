# Assem's Arabic Stemmer [![DOI](https://zenodo.org/badge/49428595.svg)](https://zenodo.org/badge/latestdoi/49428595)
This is an algorithm for Arabic stemming written on Snowball framework language. If offers light stemming and text normalization. 

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
## Distributions:
- dist light stemmer to available languages:
```sh
$ make dist
```


