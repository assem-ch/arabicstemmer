#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Test for issue: المهيمن should stem to مهيمن, not مهيم

This test verifies that words ending with noon (ن) that is part of the root
pattern are not incorrectly removed by the stemmer.
"""

import subprocess
import sys

def test_word(word, expected):
    """Test a single word with the stemmer"""
    result = subprocess.run(
        ['./bin/stemwords', '-l', 'ar'],
        input=word + '\n',
        capture_output=True,
        text=True,
        encoding='utf-8'
    )
    stemmed = result.stdout.strip()
    
    if stemmed == expected:
        print(f"✓ {word} → {stemmed} (expected: {expected})")
        return True
    else:
        print(f"✗ {word} → {stemmed} (expected: {expected})")
        return False

def main():
    """Run tests for the المهيمن issue"""
    print("Testing issue: المهيمن should stem to مهيمن")
    print("=" * 60)
    
    tests = [
        # The main reported issue
        ("المهيمن", "مهيمن"),  # al-Muhaymin (the Dominant, one of Allah's names)
        ("مهيمن", "مهيمن"),    # Muhaymin without article
        
        # Similar patterns with noon that should be preserved
        ("المؤمن", "مومن"),    # al-Mu'min (the Believer)
        ("مؤمن", "مومن"),      # Mu'min without article
        
        # Words where noon is actually a suffix and should be removed
        ("المعلمون", "معلم"),  # al-Mu'allimun (the teachers - masculine plural)
        ("معلمون", "معلم"),    # Mu'allimun without article
    ]
    
    passed = 0
    failed = 0
    
    for word, expected in tests:
        if test_word(word, expected):
            passed += 1
        else:
            failed += 1
    
    print("=" * 60)
    print(f"Results: {passed} passed, {failed} failed")
    
    return 0 if failed == 0 else 1

if __name__ == '__main__':
    sys.exit(main())
