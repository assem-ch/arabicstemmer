#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Example: Integrating Arabic Stoplist with Stemming

This example demonstrates how to use the Arabic stoplist module
in conjunction with the Arabic stemmer for text processing.
"""

import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from stopwords import ArabicStoplist


def process_arabic_text(text, use_stoplist=True, use_affixes=False, max_frequency_rank=None):
    """
    Process Arabic text by filtering stopwords and optionally stemming.
    
    Args:
        text: Arabic text to process
        use_stoplist: Whether to filter stopwords
        use_affixes: Whether to use affix patterns when filtering stopwords
        max_frequency_rank: Maximum frequency rank for stopwords (None = all)
    
    Returns:
        List of processed words
    """
    # Initialize stoplist
    stoplist = ArabicStoplist()
    
    # Simple tokenization (split by whitespace)
    # In production, use a proper Arabic tokenizer
    words = text.split()
    
    if use_stoplist:
        # Filter stopwords
        words = stoplist.filter_stopwords(words, use_affixes=use_affixes)
    
    # At this point, you would typically apply stemming
    # For example, using the Snowball stemmer:
    # stemmed_words = [stem(word) for word in words]
    
    return words


def analyze_text(text):
    """
    Analyze Arabic text and show stopword statistics.
    
    Args:
        text: Arabic text to analyze
    """
    stoplist = ArabicStoplist()
    words = text.split()
    
    print("Text Analysis")
    print("=" * 60)
    print(f"Total words: {len(words)}")
    
    # Count stopwords
    stopwords_found = [w for w in words if stoplist.is_stopword(w)]
    print(f"Stopwords found: {len(stopwords_found)} ({len(stopwords_found)/len(words)*100:.1f}%)")
    
    # Count content words
    content_words = [w for w in words if not stoplist.is_stopword(w)]
    print(f"Content words: {len(content_words)} ({len(content_words)/len(words)*100:.1f}%)")
    
    # Show stopwords by category
    print("\nStopwords by category:")
    categories = {}
    for word in stopwords_found:
        info = stoplist.get_word_info(word)
        if info:
            cat = info['category']
            if cat not in categories:
                categories[cat] = []
            categories[cat].append(word)
    
    for cat, words_in_cat in sorted(categories.items()):
        print(f"  {cat}: {', '.join(set(words_in_cat))}")
    
    print("\nContent words (non-stopwords):")
    print(f"  {' '.join(content_words)}")


def main():
    # Example 1: Basic stopword filtering
    print("Example 1: Basic Stopword Filtering")
    print("=" * 60)
    text1 = "الكتاب في المكتبة على الطاولة من الخشب"
    print(f"Original text: {text1}")
    
    filtered = process_arabic_text(text1, use_stoplist=True)
    print(f"After filtering: {' '.join(filtered)}")
    print()
    
    # Example 2: Using affix patterns
    print("Example 2: Filtering with Affix Support")
    print("=" * 60)
    text2 = "والطالب يدرس في المدرسة بالقلم والدفتر"
    print(f"Original text: {text2}")
    
    filtered_no_affixes = process_arabic_text(text2, use_stoplist=True, use_affixes=False)
    print(f"Without affixes: {' '.join(filtered_no_affixes)}")
    
    filtered_with_affixes = process_arabic_text(text2, use_stoplist=True, use_affixes=True)
    print(f"With affixes: {' '.join(filtered_with_affixes)}")
    print()
    
    # Example 3: Text analysis
    print("Example 3: Text Analysis")
    print("=" * 60)
    text3 = "الطالب يذهب إلى المدرسة في الصباح مع أصدقائه"
    print(f"Text: {text3}\n")
    analyze_text(text3)
    print()
    
    # Example 4: Category filtering
    print("Example 4: Stopwords by Category")
    print("=" * 60)
    stoplist = ArabicStoplist()
    
    print("Articles:", ', '.join(stoplist.get_stopwords_by_category('article')))
    print("Pronouns (sample):", ', '.join(stoplist.get_stopwords_by_category('pronoun')[:10]))
    print("Prepositions (sample):", ', '.join(stoplist.get_stopwords_by_category('preposition')[:10]))
    print("Conjunctions (sample):", ', '.join(stoplist.get_stopwords_by_category('conjunction')[:10]))
    print()
    
    # Example 5: High-frequency stopwords
    print("Example 5: High-Frequency Stopwords")
    print("=" * 60)
    top_20 = stoplist.get_stopwords(max_frequency_rank=20)
    print(f"Top 20 most frequent stopwords:")
    for i, word in enumerate(top_20, 1):
        info = stoplist.get_word_info(word)
        if info:
            print(f"  {i}. {word} - {info['translation']} ({info['category']})")
    print()


if __name__ == '__main__':
    main()
