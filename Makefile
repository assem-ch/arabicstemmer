SNOWBALL="./snowball/"
ARABIC_STEMMER="algorithm/stemmer.sbl"
VOCFILE="test_data/voc.txt"
OUTPUTFILE="test_data/output.txt"
GROUPINGFILE="test_data/grouping.txt"

default: build

download:
	@echo "TO BE DONE!"

build: 
	@echo "Copying the algorithm to snowball..."
	@cp $(ARABIC_STEMMER) $(SNOWBALL)"algorithms/arabic/stem_Unicode.sbl"
	@echo "Building..."
	@cd $(SNOWBALL); make

run: build
	@echo "Put your words here:"
	@cd snowball; @./stemwords -l ar 

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
	@python algorithm/test/test_grouping.py $(VOCFILE) $(OUTPUTFILE) $(GROUPINGFILE)


test: time grouping
