# Welcome to snowball-arabic-stemmer doc
If you would like to visit official website [arabicstemmer.com](http://arabicstemmer.com).

## Snowball Arabic Stemmer
This is an algorithm for Arabic stemming written on Snowball framework language. If offers
- light stemming.
- root-based stemming.
- text normalization.

## Doc Content
- [Quickstart](/)
    - [Requirements](#requirements)
    - [Build](#build)
    - [Run](#run)
    - [Test](#test)
    - [Distributions](#distributions)
    - [Usage with languages](#usage-with-languages)
    - [Usage with NLP libs](#usage-from-known-libs)
    - [Contribute](#contribute)
- [Algorithms](/algorithms/)
    - [Light Stemmer Algorithm](algorithms/#light-algorithm)
    - [Root-Based Stemmer Algorithm](algorithm/#root-based-algorithm)
- [Credits](credits/)
- [Corpuses](corpuses/)


## Code Usage

## Requirements
- [Snowball framework](https://github.com/snowballstem/snowball)
- [Snowball-data](https://github.com/snowballstem/snowball-data)
- [Golden-Arabic-Corpus](https://github.com/LBenzahia/golden-corpus-arabic/archive/master.zip)

- You can download it automatically using
```sh
    $ make download
```

- Install python requirements
```sh
    $ sudo pip install -r requirements.txt
```
or manually by:

- extracting snowball into the root folder `{Root}/snowball`
- extracting snowball-data/arabic/voc.txt.gz into `{Root}/test_data/voc.txt`

## Build

- light stemming
```sh
      $ make build
```
- root-based stemming
```sh
      $ make build_root_based_stemmer
```

## Run

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

## Test
We configured tests to run against snowball-data arabic sample.

- time
```sh
      $ make time
```

- grouping effect
```sh
      $ make grouping
```
- all
```sh
      $ make test
```
- Test SAS with golden arabic corpus
```sh
      $ make test_arabicstemmer
```
## Distributions

- dist light stemmer to available languages
```sh
    $ make dist
```
- dist root-based stemmer to available languages
```sh
    $ make dist_rooter
```
## Usage with languages

Programming language | Author/Affiliation | How to use | Links | Notes
----------- | ------------- | ---------- | ---------- | -----
Python |  |  |  |
C |  |  |  |
C++ |  |  |  |
Java |  |  |  |
Jsx |  |  |  |
Nodejs |  |  |  |
Go|  |  |  |

## Usage from known Libs

NLB Library | Affiliation | How to use | Links | Notes
----------- | ------------- | ---------- | ---------- | -----
node_snowball |  |  |  |
nltk |  | |  |
spark.apache |  |  |  |
lucene.apache |  |  |  |
Elastic Search |  |  |  |
Whoosh |  |  |  |
snowballstemmer|  |  |  |
Xapian|  |  |  |

## Some Results
Snowball Arabic (Stemmer & root-based) Results

Word | Stem | root
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

## Contribute
If you have a bug or suggestion, please consider to submit a report [here](https://github.com/assem-ch/arabicstemmer/issues/new). Open issues are listed [here](https://github.com/assem-ch/arabicstemmer/issues).

## Authors
* Assem Chelli `assem.ch[at]gmail[dot]com` Founder .

## Contributers
* Lakhdar Benzahia `lakhdar.benzahia[at]gmail[dot]com` Algorithm/website/Documentation .
