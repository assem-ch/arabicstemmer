SNOWBALL="./snowball/"
ARABIC_STEMMER="algorithm/stemmer.sbl"

default: 
	cp $(ARABIC_STEMMER) $(SNOWBALL)"algorithms/arabic/stem_Unicode.sbl"
	cd $(SNOWBALL); make; make dist
	mkdir -p  "dist/python/"; cp $(SNOWBALL)dist/snowballstemmer-*.tar.gz "dist/python/"
	mkdir -p  "dist/java/";cp $(SNOWBALL)"dist/libstemmer_java.tgz" "dist/java/"
	mkdir -p  "dist/c/";cp $(SNOWBALL)"dist/libstemmer_c.tgz" "dist/c/"
	mkdir -p  "dist/jsx/";cp $(SNOWBALL)"dist/jsxstemmer.tgz" "dist/jsx/"
        
