# Arabic Stoplist Module

This module provides a comprehensive Arabic stoplist with advanced features for Arabic NLP applications.

## Features

- **Categorized Stopwords**: Stopwords are organized into categories (articles, pronouns, prepositions, conjunctions, particles, verbs, demonstratives, interrogatives)
- **Translation Comments**: Each stopword includes English translation for better understanding
- **Frequency-Based Filtering**: Stopwords are ranked by frequency, allowing you to filter by importance
- **Affix Support**: Regular expression patterns support common Arabic prefixes and suffixes
- **Easy Integration**: Simple API for checking and filtering stopwords

## Stoplist Structure

The stoplist file (`arabic_stoplist.txt`) contains 165+ high-frequency Arabic stopwords. Each entry includes:
- **Word**: The Arabic stopword
- **Translation**: English translation
- **Category**: Grammatical category
- **Frequency Rank**: Lower numbers indicate higher frequency

## Categories

- **article**: Definite articles (ال, آل)
- **pronoun**: Personal, possessive, and relative pronouns (أنا, هو, الذي, etc.)
- **preposition**: Common prepositions (في, من, على, etc.)
- **conjunction**: Conjunctions (و, أو, لكن, etc.)
- **particle**: Particles and auxiliary words (ما, لا, قد, etc.)
- **verb**: High-frequency verbs (كان, يكون, etc.)
- **demonstrative**: Demonstrative pronouns (هذا, هذه, هنا, etc.)
- **interrogative**: Question words (من, ما, متى, etc.)

## Usage

### Basic Usage

```python
from stopwords import ArabicStoplist

# Initialize the stoplist
stoplist = ArabicStoplist()

# Check if a word is a stopword
print(stoplist.is_stopword('في'))  # True
print(stoplist.is_stopword('كتاب'))  # False

# Get all stopwords
all_stopwords = stoplist.get_stopwords()

# Get high-frequency stopwords only (rank <= 50)
high_freq_stopwords = stoplist.get_stopwords(max_frequency_rank=50)
```

### Filtering with Affixes

```python
# Check stopword with prefix/suffix support
print(stoplist.is_stopword('والذي', use_affixes=True))  # True (و + الذي)
print(stoplist.is_stopword('بالذي', use_affixes=True))  # True (ب + الذي)

# Filter stopwords from a list of words
words = ['الكتاب', 'في', 'المكتبة', 'على', 'الطاولة']
filtered = stoplist.filter_stopwords(words, use_affixes=True)
print(filtered)  # ['الكتاب', 'المكتبة', 'الطاولة']
```

### Category-Based Filtering

```python
# Get stopwords by category
pronouns = stoplist.get_stopwords_by_category('pronoun')
prepositions = stoplist.get_stopwords_by_category('preposition')
conjunctions = stoplist.get_stopwords_by_category('conjunction')

# Get all available categories
categories = stoplist.get_all_categories()
print(categories)  # ['article', 'pronoun', 'preposition', ...]
```

### Word Information

```python
# Get detailed information about a stopword
info = stoplist.get_word_info('في')
print(info)
# {'word': 'في', 'translation': 'in/at', 'category': 'preposition', 'frequency_rank': 29}
```

### Statistics

```python
# Get stoplist statistics
stats = stoplist.get_statistics()
print(f"Total stopwords: {stats['total_stopwords']}")
print(f"Categories: {stats['total_categories']}")
print("Category counts:", stats['category_counts'])
```

### Convenience Functions

```python
from stopwords import load_stoplist, is_stopword

# Quick load as a set
stopwords_set = load_stoplist(max_frequency_rank=100)

# Quick check
if is_stopword('من'):
    print("'من' is a stopword")
```

## Affix Patterns

The module supports common Arabic prefixes and suffixes through the `affix_patterns.txt` file:

### Prefixes
- Conjunctions: و (and), ف (so/then)
- Prepositions: ب (with/by), ك (like), ل (to/for)
- Articles: ال (the)
- Combinations: وال, فال, بال, كال, لل, etc.

### Suffixes
- Possessive pronouns: ي (my), ك (your), ه (his), ها (her), نا (our), etc.
- Plural/Dual markers: ان, ين, تان, تين, ون
- Verb markers: ت, وا, ن

## Integration with Arabic Stemmer

The stoplist can be used in conjunction with the Arabic stemmer for better text processing:

```python
from stopwords import ArabicStoplist

stoplist = ArabicStoplist()

def process_text(text):
    # Tokenize text (example)
    words = text.split()
    
    # Filter stopwords
    filtered_words = stoplist.filter_stopwords(words, use_affixes=True)
    
    # Apply stemming to remaining words
    # ... stemming code here ...
    
    return filtered_words
```

## File Structure

```
stopwords/
├── __init__.py              # Main Python module
├── arabic_stoplist.txt      # Stopwords with translations and categories
├── affix_patterns.txt       # Prefix and suffix patterns
└── README.md               # This file
```

## Customization

You can customize the stoplist by:

1. **Adding new stopwords**: Edit `arabic_stoplist.txt` following the format:
   ```
   word|translation|category|frequency_rank
   ```

2. **Modifying affix patterns**: Edit `affix_patterns.txt` to add or remove prefixes/suffixes

3. **Creating custom stoplists**: Initialize with custom file paths:
   ```python
   stoplist = ArabicStoplist(
       stoplist_path='/path/to/custom_stoplist.txt',
       affix_patterns_path='/path/to/custom_affixes.txt'
   )
   ```

## Testing

Run the module directly to see example output and statistics:

```bash
python stopwords/__init__.py
```

## References

The stoplist is based on common Arabic linguistic resources and high-frequency word lists used in Arabic NLP applications.

## License

This module is part of the Assem's Arabic Stemmer project and is distributed under the same BSD license.
