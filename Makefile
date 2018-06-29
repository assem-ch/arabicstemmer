SNOWBALL="./snowball/"
ARABIC_STEMMER="algorithm/stemmer.sbl"
ARABIC_ROOT_BASED_STEMMER = "algorithm/root_based_stemmer.sbl"
VOCFILE="test_data/voc.txt"
OUTPUTFILE="test_data/output.txt"
GROUPINGFILE="test_data/grouping.txt"
CORPUS_WORDS="golden_corpus/core/words.txt"
CORPUS_STEMS="test_golden_corpus/stems_output.txt"
CORPUS_ROOTS="test_golden_corpus/roots_output.txt"
GOLDEN_CORPUS="./golden_corpus/"

default: build

download: download_snowball download_data download_golden_corpus
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
	@echo "Creating test_data folder ....."
	@mkdir test_data
	@echo "Move voc.txt to test_data ....."
	@mv voc.txt test_data

download_golden_corpus:
	@echo "waiting for download golden_corpus_arabic ..... "
	@curl -LOk https://github.com/LBenzahia/golden-corpus-arabic/archive/master.zip
	@echo "Unziping master.zip"
	@unzip master.zip
	@echo "Rename golden-corpus-arabic folder to golden_corpus ....."
	@mv golden-corpus-arabic-master golden_corpus
	@echo " Delete master.zip ......"
	@rm master.zip
	@echo "building golden-corpus-arabic.json ..."
	@mkdir test_golden_corpus
	@cd $(GOLDEN_CORPUS); make

build:
	@echo "Copying the algorithm to snowball..."
	@cp $(ARABIC_STEMMER) $(SNOWBALL)"algorithms/arabic.sbl"
	@echo "Building light stemmer ..."
	@cd $(SNOWBALL); make

build_root_based_stemmer:
	@echo "Copying the algorithm of based-root stemmer to snowball..."
	@cp $(ARABIC_ROOT_BASED_STEMMER) $(SNOWBALL)"algorithms/arabic.sbl"
	@echo "Building based root stemmer ..."
	@cd $(SNOWBALL); make

run: build
	@echo "Put your words here:"
	@cd snowball; ./stemwords -l ar

run_root: build_root_based_stemmer
		@echo "Put your words here:"
		@cd snowball; ./stemwords -l ar

dist: build
	@echo "Compiling the light stemming algorithm to available programming languages"
	@cd $(SNOWBALL); make dist
	@mkdir -p  "dist/python/"; cp $(SNOWBALL)dist/snowballstemmer-*.tar.gz "dist/python/"
	@mkdir -p  "dist/java/";cp $(SNOWBALL)"dist/libstemmer_java.tgz" "dist/java/"
	@mkdir -p  "dist/c/";cp $(SNOWBALL)"dist/libstemmer_c.tgz" "dist/c/"
	@mkdir -p  "dist/jsx/";cp $(SNOWBALL)"dist/jsxstemmer.tgz" "dist/jsx/"

dist_rooter: build_root_based_stemmer
	@echo "Compiling the root-based stemming algorithm to available programming languages"
	@cd $(SNOWBALL); make dist
	@mkdir -p  "dist_rooter/python/"; cp $(SNOWBALL)dist/snowballstemmer-*.tar.gz "dist/python/"
	@mkdir -p  "dist_rooter/java/";cp $(SNOWBALL)"dist/libstemmer_java.tgz" "dist/java/"
	@mkdir -p  "dist_rooter/c/";cp $(SNOWBALL)"dist/libstemmer_c.tgz" "dist/c/"
	@mkdir -p  "dist_rooter/jsx/";cp $(SNOWBALL)"dist/jsxstemmer.tgz" "dist/jsx/"

time:
	@echo "Stemming sample timing..."
	@time $(SNOWBALL)stemwords -l ar -i $(VOCFILE) -o $(OUTPUTFILE)

grouping: time
	@echo "Stemming sample grouping effect..."
	@python algorithm/test/test_grouping.py  $(OUTPUTFILE) $(VOCFILE) $(GROUPINGFILE)

test: time grouping

get_roots: build_root_based_stemmer
	@echo "getting roots from words.txt and put it in roots.txt ......"
	@time $(SNOWBALL)stemwords -l ar -i $(CORPUS_WORDS) -o $(CORPUS_ROOTS)

get_stems: build
	@echo "getting stems from words.txt and put it in stems.txt ......"
	@time $(SNOWBALL)stemwords -l ar -i $(CORPUS_WORDS) -o $(CORPUS_STEMS)

get_all : get_stems get_roots

test_arabicstemmer: get_all
	@echo "test arabicstemmer using golden_corpus_arabic......"
	@python algorithm/test/test_stemmer.py

test_isri:
	@echo "Testing ISRI Arabic Stemmer using golden_corpus_arabic ..."
	@python algorithm/test/test_isri.py

clean:
	@rm  -rf  golden_corpus
	@rm  -rf  test_golden_corpus

update: clean download_golden_corpus
