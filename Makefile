SNOWBALL="modules/snowball/"
VOCFILE="tests/test_data/snowball-data/voc.txt"


ARABIC_STEMMER="algorithm/arabic.sbl"
ARABIC_ROOT_BASED_STEMMER = "algorithm/arabic_root.sbl"

OUTPUT="tests/output_data/"

build:
	cp $(ARABIC_STEMMER) $(SNOWBALL)"algorithms/arabic.sbl"
	cd $(SNOWBALL); make
	cp $(SNOWBALL)stemwords bin/stemwords; chmod +x bin/stemwords
	cp $(ARABIC_ROOT_BASED_STEMMER) $(SNOWBALL)"algorithms/arabic.sbl"
	cd $(SNOWBALL); make
	cp $(SNOWBALL)stemwords bin/rootwords; chmod +x bin/rootwords

run:
	@echo "Put your words here:"
	@./bin/stemwords -l ar

run_root:
	@echo "Put your words here:"
	@./bin/rootwords -l ar

test:
	@echo "Stemming sample timing..."
	@time ./bin/stemwords -l ar -i $(VOCFILE) -o $(OUTPUT)output_stem.txt
	@echo "Rooting sample timing..."
	@time ./bin/rootwords -l ar -i $(VOCFILE) -o $(OUTPUT)output_root.txt
	@echo "Stemming sample grouping effect..."
	@python tests/test_grouping.py  $(OUTPUT)output_stem.txt $(VOCFILE) $(OUTPUT)grouping_stem.txt
	@echo "Rooting sample grouping effect..."
	@python tests/test_grouping.py  $(OUTPUT)output_root.txt $(VOCFILE) $(OUTPUT)grouping_root.txt



test_against_gc:
	@echo "getting stems from words.txt and put it in stems.txt ......"
	@time ./bim/stemwords -l ar -i $(CORPUS_WORDS) -o $(CORPUS_STEMS)
	@echo "getting roots from words.txt and put it in roots.txt ......"
	@time ./bin/rootwords -l ar -i $(CORPUS_WORDS) -o $(CORPUS_ROOTS)
	@echo "test arabicstemmer against golden_corpus_arabic......"
	@python algorithm/test/test_stemmer.py


dist: build
	@cd $(SNOWBALL); make dist
	@mkdir -p  "dist/python/"; cp $(SNOWBALL)dist/snowballstemmer-*.tar.gz "dist/python/"
	@mkdir -p  "dist/java/";cp $(SNOWBALL)"dist/libstemmer_java.tgz" "dist/java/"
	@mkdir -p  "dist/c/";cp $(SNOWBALL)"dist/libstemmer_c.tgz" "dist/c/"
	@mkdir -p  "dist/jsx/";cp $(SNOWBALL)"dist/jsstemmer.tgz" "dist/jsx/"
