# Assem's Arabic Stemmer

This is an algorithm for Arabic stemming written on Snowball framework language. If offers light stemming and text normalization. voc



## Requirements

- [Snowball framework](https://github.com/snowballstem/snowball)
- [Snowball-data](https://github.com/snowballstem/snowball-data)

You can download it automatically using:

```sh
    $ make download
```
or manually by:
- extracting snowball into the root folder `{Root}/snowball`
- extracting snowball-data/arabic/voc.txt.gz into `{Root}/test_snowball/voc.txt`


## Build

```sh
    $ make build
```

## Run

```sh
	$ make run
	الطالب
	طالب

```

## Test 
We configured tests to run against snowball-data arabic sample. 

- time:
	```sh
	    $ make time
	```

- grouping effect:
    ```sh
    $ make grouping
	```
- all:
	```sh
    $ make test
	```

# Distributions
```sh
    $ make dist
```
