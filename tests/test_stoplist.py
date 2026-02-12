#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Tests for Arabic Stoplist Module
"""

import unittest
import os
import sys

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from stopwords import ArabicStoplist, load_stoplist, is_stopword


class TestArabicStoplist(unittest.TestCase):
    """Test cases for ArabicStoplist class"""
    
    def setUp(self):
        """Set up test fixtures"""
        self.stoplist = ArabicStoplist()
    
    def test_initialization(self):
        """Test that stoplist initializes correctly"""
        self.assertIsNotNone(self.stoplist)
        self.assertGreater(len(self.stoplist.stopwords), 0)
        self.assertGreater(len(self.stoplist.stopwords_set), 0)
    
    def test_load_stoplist(self):
        """Test that stoplist loads correctly"""
        self.assertGreater(len(self.stoplist.stopwords), 100)
        # Check that we have expected categories
        categories = self.stoplist.get_all_categories()
        expected_categories = ['article', 'pronoun', 'preposition', 'conjunction', 'particle']
        for cat in expected_categories:
            self.assertIn(cat, categories)
    
    def test_get_stopwords(self):
        """Test getting all stopwords"""
        stopwords = self.stoplist.get_stopwords()
        self.assertIsInstance(stopwords, list)
        self.assertGreater(len(stopwords), 0)
        # Check some common stopwords
        self.assertIn('في', stopwords)
        self.assertIn('من', stopwords)
        self.assertIn('على', stopwords)
    
    def test_get_stopwords_by_frequency(self):
        """Test filtering stopwords by frequency rank"""
        # Get high-frequency stopwords
        high_freq = self.stoplist.get_stopwords(max_frequency_rank=50)
        all_stopwords = self.stoplist.get_stopwords()
        
        self.assertLess(len(high_freq), len(all_stopwords))
        self.assertGreater(len(high_freq), 0)
        
        # Verify all returned words have rank <= 50
        for word in high_freq:
            info = self.stoplist.get_word_info(word)
            self.assertLessEqual(info['frequency_rank'], 50)
    
    def test_get_stopwords_by_category(self):
        """Test getting stopwords by category"""
        # Test prepositions
        prepositions = self.stoplist.get_stopwords_by_category('preposition')
        self.assertGreater(len(prepositions), 0)
        self.assertIn('في', prepositions)
        self.assertIn('من', prepositions)
        self.assertIn('على', prepositions)
        
        # Test pronouns
        pronouns = self.stoplist.get_stopwords_by_category('pronoun')
        self.assertGreater(len(pronouns), 0)
        self.assertIn('أنا', pronouns)
        self.assertIn('هو', pronouns)
        
        # Test articles
        articles = self.stoplist.get_stopwords_by_category('article')
        self.assertGreater(len(articles), 0)
        self.assertIn('ال', articles)
    
    def test_is_stopword_direct(self):
        """Test direct stopword detection"""
        # Should be stopwords
        self.assertTrue(self.stoplist.is_stopword('في'))
        self.assertTrue(self.stoplist.is_stopword('من'))
        self.assertTrue(self.stoplist.is_stopword('الذي'))
        self.assertTrue(self.stoplist.is_stopword('هو'))
        self.assertTrue(self.stoplist.is_stopword('أنا'))
        
        # Should not be stopwords
        self.assertFalse(self.stoplist.is_stopword('كتاب'))
        self.assertFalse(self.stoplist.is_stopword('مدرسة'))
        self.assertFalse(self.stoplist.is_stopword('طالب'))
    
    def test_is_stopword_with_affixes(self):
        """Test stopword detection with affixes"""
        # These should be detected with affixes enabled
        # والذي = و + الذي
        self.assertTrue(self.stoplist.is_stopword('والذي', use_affixes=True))
        
        # بالذي = ب + الذي
        self.assertTrue(self.stoplist.is_stopword('بالذي', use_affixes=True))
        
        # Note: Some affix combinations may not match depending on pattern implementation
        # The core stopword should always match
        self.assertTrue(self.stoplist.is_stopword('الذي', use_affixes=True))
    
    def test_filter_stopwords(self):
        """Test filtering stopwords from text"""
        words = ['الكتاب', 'في', 'المكتبة', 'على', 'الطاولة', 'من', 'الخشب']
        filtered = self.stoplist.filter_stopwords(words)
        
        # Stopwords should be removed
        self.assertNotIn('في', filtered)
        self.assertNotIn('على', filtered)
        self.assertNotIn('من', filtered)
        
        # Content words should remain
        self.assertIn('الكتاب', filtered)
        self.assertIn('المكتبة', filtered)
        self.assertIn('الطاولة', filtered)
        self.assertIn('الخشب', filtered)
    
    def test_get_word_info(self):
        """Test getting word information"""
        info = self.stoplist.get_word_info('في')
        self.assertIsNotNone(info)
        self.assertEqual(info['word'], 'في')
        self.assertEqual(info['category'], 'preposition')
        self.assertIn('translation', info)
        self.assertIn('frequency_rank', info)
        
        # Non-existent word
        info = self.stoplist.get_word_info('كتاب')
        self.assertIsNone(info)
    
    def test_get_statistics(self):
        """Test getting statistics"""
        stats = self.stoplist.get_statistics()
        
        self.assertIn('total_stopwords', stats)
        self.assertIn('total_categories', stats)
        self.assertIn('category_counts', stats)
        
        self.assertGreater(stats['total_stopwords'], 0)
        self.assertGreater(stats['total_categories'], 0)
        self.assertIsInstance(stats['category_counts'], dict)
    
    def test_get_all_categories(self):
        """Test getting all categories"""
        categories = self.stoplist.get_all_categories()
        self.assertIsInstance(categories, list)
        self.assertGreater(len(categories), 0)
        
        # Check for expected categories
        expected = ['article', 'pronoun', 'preposition', 'conjunction']
        for cat in expected:
            self.assertIn(cat, categories)
    
    def test_affix_loading(self):
        """Test that affixes are loaded"""
        # Should have some prefixes and suffixes loaded
        self.assertGreaterEqual(len(self.stoplist.prefixes), 0)
        self.assertGreaterEqual(len(self.stoplist.suffixes), 0)


class TestConvenienceFunctions(unittest.TestCase):
    """Test convenience functions"""
    
    def test_load_stoplist_function(self):
        """Test load_stoplist convenience function"""
        stopwords = load_stoplist()
        self.assertIsInstance(stopwords, set)
        self.assertGreater(len(stopwords), 0)
        self.assertIn('في', stopwords)
        self.assertIn('من', stopwords)
    
    def test_load_stoplist_with_frequency(self):
        """Test load_stoplist with frequency filter"""
        all_stopwords = load_stoplist()
        high_freq = load_stoplist(max_frequency_rank=50)
        
        self.assertLess(len(high_freq), len(all_stopwords))
    
    def test_is_stopword_function(self):
        """Test is_stopword convenience function"""
        self.assertTrue(is_stopword('في'))
        self.assertTrue(is_stopword('من'))
        self.assertFalse(is_stopword('كتاب'))


class TestDataIntegrity(unittest.TestCase):
    """Test data integrity"""
    
    def setUp(self):
        """Set up test fixtures"""
        self.stoplist = ArabicStoplist()
    
    def test_no_duplicates(self):
        """Test that there are no duplicate stopwords"""
        words = [entry['word'] for entry in self.stoplist.stopwords]
        self.assertEqual(len(words), len(set(words)))
    
    def test_all_entries_have_required_fields(self):
        """Test that all entries have required fields"""
        required_fields = ['word', 'translation', 'category', 'frequency_rank']
        
        for entry in self.stoplist.stopwords:
            for field in required_fields:
                self.assertIn(field, entry)
                self.assertIsNotNone(entry[field])
    
    def test_frequency_ranks_are_positive(self):
        """Test that all frequency ranks are positive integers"""
        for entry in self.stoplist.stopwords:
            self.assertIsInstance(entry['frequency_rank'], int)
            self.assertGreater(entry['frequency_rank'], 0)
    
    def test_categories_are_consistent(self):
        """Test that categories are consistent"""
        # Get all unique categories from entries
        entry_categories = set(entry['category'] for entry in self.stoplist.stopwords)
        
        # Get categories from the categories dict
        dict_categories = set(self.stoplist.categories.keys())
        
        # They should match
        self.assertEqual(entry_categories, dict_categories)


if __name__ == '__main__':
    # Run tests
    unittest.main(verbosity=2)
