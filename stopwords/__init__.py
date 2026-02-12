#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Arabic Stoplist Module

This module provides functionality to load and filter Arabic stopwords.
It supports:
- Loading categorized stopwords with translations
- Filtering based on frequency ranks
- Regular expression patterns with prefix/suffix support
- Categorization of stopwords
"""

import os
import re
from typing import Dict, List, Set, Optional, Tuple


class ArabicStoplist:
    """
    Arabic Stoplist handler with support for categories, translations,
    and regular expression patterns with affixes.
    """
    
    def __init__(self, stoplist_path: Optional[str] = None, 
                 affix_patterns_path: Optional[str] = None):
        """
        Initialize the Arabic Stoplist.
        
        Args:
            stoplist_path: Path to the stoplist file. If None, uses default.
            affix_patterns_path: Path to the affix patterns file. If None, uses default.
        """
        # Set default paths if not provided
        if stoplist_path is None:
            base_dir = os.path.dirname(os.path.abspath(__file__))
            stoplist_path = os.path.join(base_dir, 'arabic_stoplist.txt')
        
        if affix_patterns_path is None:
            base_dir = os.path.dirname(os.path.abspath(__file__))
            affix_patterns_path = os.path.join(base_dir, 'affix_patterns.txt')
        
        self.stoplist_path = stoplist_path
        self.affix_patterns_path = affix_patterns_path
        
        # Storage for loaded data
        self.stopwords: List[Dict[str, str]] = []
        self.stopwords_set: Set[str] = set()
        self.categories: Dict[str, List[str]] = {}
        self.prefixes: List[str] = []
        self.suffixes: List[str] = []
        self.regex_patterns: List[str] = []
        
        # Load data
        self._load_stoplist()
        self._load_affix_patterns()
        self._compile_regex_patterns()
    
    def _load_stoplist(self):
        """Load the stoplist from file."""
        if not os.path.exists(self.stoplist_path):
            raise FileNotFoundError(f"Stoplist file not found: {self.stoplist_path}")
        
        with open(self.stoplist_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                
                # Skip comments and empty lines
                if not line or line.startswith('#'):
                    continue
                
                # Parse line: word | translation | category | frequency_rank
                parts = [p.strip() for p in line.split('|')]
                if len(parts) == 4:
                    word, translation, category, freq_rank = parts
                    
                    entry = {
                        'word': word,
                        'translation': translation,
                        'category': category,
                        'frequency_rank': int(freq_rank)
                    }
                    
                    self.stopwords.append(entry)
                    self.stopwords_set.add(word)
                    
                    # Group by category
                    if category not in self.categories:
                        self.categories[category] = []
                    self.categories[category].append(word)
    
    def _load_affix_patterns(self):
        """Load prefix and suffix patterns from file."""
        if not os.path.exists(self.affix_patterns_path):
            return  # Affix patterns are optional
        
        current_section = None
        
        with open(self.affix_patterns_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                
                # Skip comments and empty lines
                if not line or line.startswith('#'):
                    continue
                
                # Check for section headers
                if line.startswith('[') and line.endswith(']'):
                    current_section = line[1:-1]
                    continue
                
                # Parse based on current section
                if current_section == 'prefixes':
                    parts = line.split('|')
                    if parts:
                        self.prefixes.append(parts[0])
                
                elif current_section == 'suffixes':
                    parts = line.split('|')
                    if parts:
                        self.suffixes.append(parts[0])
                
                elif current_section == 'regex_patterns':
                    if line.startswith('^'):
                        self.regex_patterns.append(line)
    
    def _compile_regex_patterns(self):
        """Compile regex patterns for stopword matching with affixes."""
        self.compiled_patterns = []
        
        # Build prefix and suffix patterns
        prefix_pattern = '|'.join(re.escape(p) for p in self.prefixes) if self.prefixes else ''
        suffix_pattern = '|'.join(re.escape(s) for s in self.suffixes) if self.suffixes else ''
        
        # For each stopword, create a pattern that matches it with optional affixes
        for entry in self.stopwords:
            word = entry['word']
            escaped_word = re.escape(word)
            
            if prefix_pattern and suffix_pattern:
                # Both prefix and suffix optional
                pattern = f'^({prefix_pattern})?{escaped_word}({suffix_pattern})?$'
            elif prefix_pattern:
                # Only prefix optional
                pattern = f'^({prefix_pattern})?{escaped_word}$'
            elif suffix_pattern:
                # Only suffix optional
                pattern = f'^{escaped_word}({suffix_pattern})?$'
            else:
                # No affixes
                pattern = f'^{escaped_word}$'
            
            self.compiled_patterns.append(re.compile(pattern))
    
    def get_stopwords(self, max_frequency_rank: Optional[int] = None) -> List[str]:
        """
        Get list of stopwords, optionally filtered by frequency rank.
        
        Args:
            max_frequency_rank: Maximum frequency rank to include.
                               Lower ranks = higher frequency.
                               If None, returns all stopwords.
        
        Returns:
            List of stopword strings.
        """
        if max_frequency_rank is None:
            return [entry['word'] for entry in self.stopwords]
        
        return [entry['word'] for entry in self.stopwords 
                if entry['frequency_rank'] <= max_frequency_rank]
    
    def get_stopwords_by_category(self, category: str) -> List[str]:
        """
        Get stopwords filtered by category.
        
        Args:
            category: Category name (e.g., 'pronoun', 'preposition', 'conjunction')
        
        Returns:
            List of stopwords in the specified category.
        """
        return self.categories.get(category, [])
    
    def get_all_categories(self) -> List[str]:
        """Get list of all available categories."""
        return list(self.categories.keys())
    
    def is_stopword(self, word: str, use_affixes: bool = False) -> bool:
        """
        Check if a word is a stopword.
        
        Args:
            word: The word to check.
            use_affixes: If True, also matches words with common prefixes/suffixes.
        
        Returns:
            True if the word is a stopword, False otherwise.
        """
        # Direct match
        if word in self.stopwords_set:
            return True
        
        # Check with affixes if requested
        if use_affixes and self.compiled_patterns:
            for pattern in self.compiled_patterns:
                if pattern.match(word):
                    return True
        
        return False
    
    def filter_stopwords(self, words: List[str], use_affixes: bool = False) -> List[str]:
        """
        Filter out stopwords from a list of words.
        
        Args:
            words: List of words to filter.
            use_affixes: If True, also filters words with common prefixes/suffixes.
        
        Returns:
            List of words with stopwords removed.
        """
        return [word for word in words if not self.is_stopword(word, use_affixes)]
    
    def get_word_info(self, word: str) -> Optional[Dict[str, str]]:
        """
        Get detailed information about a stopword.
        
        Args:
            word: The stopword to look up.
        
        Returns:
            Dictionary with word information, or None if not found.
        """
        for entry in self.stopwords:
            if entry['word'] == word:
                return entry
        return None
    
    def get_statistics(self) -> Dict[str, int]:
        """
        Get statistics about the stoplist.
        
        Returns:
            Dictionary with statistics.
        """
        return {
            'total_stopwords': len(self.stopwords),
            'total_categories': len(self.categories),
            'total_prefixes': len(self.prefixes),
            'total_suffixes': len(self.suffixes),
            'category_counts': {cat: len(words) for cat, words in self.categories.items()}
        }


def load_stoplist(max_frequency_rank: Optional[int] = None) -> Set[str]:
    """
    Convenience function to load stopwords as a set.
    
    Args:
        max_frequency_rank: Maximum frequency rank to include.
    
    Returns:
        Set of stopwords.
    """
    stoplist = ArabicStoplist()
    return set(stoplist.get_stopwords(max_frequency_rank))


def is_stopword(word: str, use_affixes: bool = False) -> bool:
    """
    Convenience function to check if a word is a stopword.
    
    Args:
        word: The word to check.
        use_affixes: If True, also matches words with common prefixes/suffixes.
    
    Returns:
        True if the word is a stopword, False otherwise.
    """
    stoplist = ArabicStoplist()
    return stoplist.is_stopword(word, use_affixes)


if __name__ == '__main__':
    # Example usage and testing
    stoplist = ArabicStoplist()
    
    print("Arabic Stoplist Statistics:")
    print("=" * 50)
    stats = stoplist.get_statistics()
    print(f"Total stopwords: {stats['total_stopwords']}")
    print(f"Total categories: {stats['total_categories']}")
    print(f"Total prefixes: {stats['total_prefixes']}")
    print(f"Total suffixes: {stats['total_suffixes']}")
    print("\nCategory counts:")
    for category, count in sorted(stats['category_counts'].items()):
        print(f"  {category}: {count}")
    
    print("\n" + "=" * 50)
    print("Sample stopwords by category:")
    print("=" * 50)
    for category in ['article', 'pronoun', 'preposition', 'conjunction']:
        words = stoplist.get_stopwords_by_category(category)
        print(f"\n{category.upper()}: {', '.join(words[:5])}")
    
    print("\n" + "=" * 50)
    print("Testing stopword detection:")
    print("=" * 50)
    test_words = ['الذي', 'في', 'من', 'كتاب', 'والذي', 'بالذي']
    for word in test_words:
        is_stop = stoplist.is_stopword(word, use_affixes=False)
        is_stop_with_affixes = stoplist.is_stopword(word, use_affixes=True)
        print(f"  {word}: direct={is_stop}, with_affixes={is_stop_with_affixes}")
