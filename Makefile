SNOWBALL="../snowball/"
ARABIC_STEMMER="src/stemmer.sbl"

default: 
	cp $(ARABIC_STEMMER) $(SNOWBALL)algorithms/arabic/stem_Unicode.sbl
	cd $(SNOWBALL); make; make dist
        
