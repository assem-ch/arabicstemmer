SNOWBALL="../snowball/"
ARABIC_STEMMER="src/stemmer.sbl"

default: 
	cp $(ARABIC_STEMMER) $(SNOWBALL)"algorithms/arabic/stem_Unicode.sbl"
	cd $(SNOWBALL); make; make dist
	mkdir -p  "website/python/"; cp $(SNOWBALL)dist/snowballstemmer-*.tar.gz "website/python/"
	mkdir -p  "website/java/";cp $(SNOWBALL)"dist/libstemmer_java.tgz" "website/java/"
	mkdir -p  "website/c/";cp $(SNOWBALL)"dist/libstemmer_c.tgz" "website/c/"
	mkdir -p  "website/jsx/";cp $(SNOWBALL)"dist/jsxstemmer.tgz" "website/jsx/"
        
