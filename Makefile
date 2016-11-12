SNOWBALL="./snowball/"
ARABIC_STEMMER="algorithm/stemmer.sbl"
VOCFILE="test_data/voc.txt"
OUTPUTFILE="test_data/output.txt"
GROUPINGFILE="test_data/grouping.txt"


default: build

download: download_snowball download_data 

download_snowball:
	@echo "Wait for download snowball ......"
	@curl -LOk https://github.com/snowballstem/snowball/archive/master.zip
	@echo "Unziping the snowball file ......"
	@unzip master.zip 
	@echo "Rename snowball-master to snowball"
	@mv snowball-master snowball        
	@echo " Delete master.zip ......"
	@rm master.zip

download_data:
	@echo "waiting for download test data ..... "
	@curl -LOk https://github.com/snowballstem/snowball-data/raw/master/arabic/voc.txt.gz
	@echo "Unziping voc.txt.gz"
	@gzip -d voc.txt.gz


build: 
	@echo "Copying the algorithm to snowball..."
	@cp $(ARABIC_STEMMER) $(SNOWBALL)"algorithms/arabic/stem_Unicode.sbl"
	@echo "Building..."
	@cd $(SNOWBALL); make

run: build
	@echo "Put your words here:"
	@cd snowball; ./stemwords -l ar 

dist: build
	@echo "Compiling the algorithm to available programming languages"
	@cd $(SNOWBALL); make dist
	@mkdir -p  "dist/python/"; cp $(SNOWBALL)dist/snowballstemmer-*.tar.gz "dist/python/"
	@mkdir -p  "dist/java/";cp $(SNOWBALL)"dist/libstemmer_java.tgz" "dist/java/"
	@mkdir -p  "dist/c/";cp $(SNOWBALL)"dist/libstemmer_c.tgz" "dist/c/"
	@mkdir -p  "dist/jsx/";cp $(SNOWBALL)"dist/jsxstemmer.tgz" "dist/jsx/"

time:
	@echo "Stemming sample timing..."
	@time $(SNOWBALL)stemwords -l ar -i $(VOCFILE) -o $(OUTPUTFILE)

grouping: time
	@echo "Stemming sample grouping effect..."
	@python algorithm/test/test_grouping.py  $(OUTPUTFILE) $(VOCFILE) $(GROUPINGFILE)


test: time grouping

