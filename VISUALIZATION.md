# Interactive Algorithm Visualization

This document explains how to use the interactive visualization feature to understand the Arabic stemmer algorithm phases.

## Overview

The visualization tool creates an interactive HTML page that shows the step-by-step process of how the stemming algorithm processes an Arabic word. It displays the algorithm as an automaton with clear state transitions through different phases.

## Algorithm Phases

The Arabic stemmer processes words through the following phases:

1. **Initial**: The original input word
2. **Normalize_pre**: Pre-processing normalization
   - Strip vocalization marks (diacritics)
   - Normalize shaped letter forms to their base forms
   - Remove punctuation
   - Convert Hindu-Arabic numerals
   - Expand ligatures (e.g., لا → ل + ا)

3. **Checks**: Word type detection
   - Determine if the word is a noun or verb
   - Check if the word is defined (has definite article)

4. **Suffix_Processing**: Remove suffixes
   - For nouns: Remove pronouns, gender markers, plural markers
   - For verbs: Remove pronoun suffixes, tense markers
   
5. **Prefix_Processing**: Remove prefixes
   - Remove articles (ال, بال, كال, etc.)
   - Remove conjunctions (و, ف)
   - Remove prepositions (ب, ك, ل)
   - Remove verb prefixes (س, ت, ن, etc.)

6. **Normalize_post**: Post-processing normalization
   - Normalize hamza forms

7. **Final**: The final stemmed result

## Usage

### Command Line

Basic usage:
```bash
python visualize.py <arabic_word> [output_file.html]
```

Examples:
```bash
# Visualize a word (output to auto-generated filename)
python visualize.py الطالب

# Specify output filename
python visualize.py الطالب student_stem.html

# Visualize different words
python visualize.py والاطفال children_stem.html
python visualize.py فأطفالكم complex_stem.html
```

### As a Python Module

You can also use it programmatically:

```python
from visualize import visualize_word, ArabicStemmerVisualizer

# Simple usage
html_file = visualize_word('الطالب', 'output.html')

# Advanced usage
visualizer = ArabicStemmerVisualizer('الطالب')
stemmed = visualizer.trace_stemming('./bin/stemwords')
html_file = visualizer.generate_html('custom_output.html')
```

## Interactive Features

The generated HTML visualization includes:

### Controls
- **▶️ Play All**: Automatically plays through all phases with animation
- **⏮️ Previous**: Go to the previous phase
- **⏭️ Next**: Go to the next phase
- **🔄 Reset**: Reset to the initial state

### Keyboard Shortcuts
- **Arrow Left/Right**: Navigate between phases
- **Space**: Play all phases automatically

### Visual Elements
- **Progress Bar**: Shows current progress through all phases (top of page)
- **Phase Cards**: Each phase is displayed as a card showing:
  - Phase name and number
  - Description of what happens in this phase
  - Input and output states with visual transition arrow
  - Highlighting for active phase
- **Summary Section**: Shows overall statistics:
  - Original word
  - Final stemmed result
  - Total number of phases
  - Number of characters removed

## Technical Details

### Requirements
- Python 3.x
- The stemmer binary (`./bin/stemwords`) must be built
- Modern web browser for viewing the visualization

### File Structure
```
arabicstemmer/
├── visualize.py          # Visualization generator script
├── VISUALIZATION.md      # This documentation
└── bin/
    └── stemwords        # Stemmer binary (built from algorithm)
```

### How It Works

1. The Python script takes an Arabic word as input
2. It runs the word through the actual stemmer binary to get the result
3. It traces the algorithm phases based on the structure defined in `algorithm/arabic.sbl`
4. It generates a self-contained HTML file with embedded CSS and JavaScript
5. The HTML file can be opened in any modern browser without requiring a server

### Customization

The visualization template includes:
- RTL (right-to-left) support for Arabic text
- Responsive design that works on different screen sizes
- Color-coded phases with smooth animations
- Clean, modern UI with gradient backgrounds

## Examples

### Example 1: Simple Word
```bash
python visualize.py الطالب
```
- Input: الطالب (the student)
- Output: طالب (student)
- Phases: Removes the definite article "ال"

### Example 2: Complex Word with Affixes
```bash
python visualize.py فأطفالكم
```
- Input: فأطفالكم (so your children)
- Output: اطفال (children)
- Phases: Removes conjunction "ف", possessive suffix "كم"

### Example 3: Plural Feminine
```bash
python visualize.py الطفولة
```
- Input: الطفولة (the childhood)
- Output: طفول (childhood stem)
- Phases: Removes article and feminine marker

## Tips

1. **Testing Multiple Words**: Create visualizations for multiple related words to compare stemming results
2. **Educational Use**: Great for teaching or understanding Arabic morphology
3. **Debugging**: Useful for understanding why certain words are stemmed a particular way
4. **Browser Choice**: Works best in Chrome, Firefox, Safari, or Edge

## Troubleshooting

### Stemmer binary not found
If you get a warning about the stemmer binary not being found:
```bash
cd /path/to/arabicstemmer
make build
```

### Python encoding issues
Make sure your terminal supports UTF-8:
```bash
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

### Visualization not displaying properly
- Ensure you're using a modern web browser
- Check that JavaScript is enabled
- Try a different browser if issues persist

## Contributing

To improve the visualization:
1. Edit `visualize.py` to add more detailed phase tracing
2. Modify the HTML template to change styling or layout
3. Add additional interactive features or controls
4. Improve phase detection for more accurate state tracking

## License

This visualization tool is part of the Assem's Arabic Stemmer project and follows the same BSD license.
