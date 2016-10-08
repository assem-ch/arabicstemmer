# Assem's Arabic Stemmer

This is the algorithm written on Snowball framework language:



## Requirements

- [Snowball framework](https://github.com/snowballstem/snowball)



## Compile 

Download Snowball to the folder `snowball` and run:

```sh
    $ make
```


## Test grouping effect
- Download test data `voc.txt.gz` from [here](https://github.com/snowballstem/snowball-data/tree/master/arabic). 
- Update paths inside the script `algorithm/test/test_grouping.py`
  ```python
      os.system("make -C ../../../snowball/")
      os.system("time ../../../snowball/stemwords -l ar -i voc.txt -o output.txt")
  ```
- Run the script. 