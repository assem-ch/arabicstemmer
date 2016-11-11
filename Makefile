SNOWBALL="./snowball/"
ARABIC_STEMMER="algorithm/stemmer.sbl"

default: 
	cp $(ARABIC_STEMMER) $(SNOWBALL)"algorithms/arabic/stem_Unicode.sbl"
	cd $(SNOWBALL); make; make dist
	mkdir -p  "dist/python/"; cp $(SNOWBALL)dist/snowballstemmer-*.tar.gz "dist/python/"
	mkdir -p  "dist/java/";cp $(SNOWBALL)"dist/libstemmer_java.tgz" "dist/java/"
	mkdir -p  "dist/c/";cp $(SNOWBALL)"dist/libstemmer_c.tgz" "dist/c/"
	mkdir -p  "dist/jsx/";cp $(SNOWBALL)"dist/jsxstemmer.tgz" "dist/jsx/"

download:
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


