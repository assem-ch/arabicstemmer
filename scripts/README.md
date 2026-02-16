# JavaScript Stemmer Generation Script

This directory contains scripts to automate the generation of JavaScript stemmer files for use in web applications.

## Overview

The `generate_js_stemmer.sh` script automates the process of:
1. Compiling the Snowball compiler (if needed)
2. Copying the Arabic stemmer algorithm
3. Generating JavaScript code from the Snowball algorithm
4. Bundling the base stemmer and Arabic stemmer into a single browser-compatible file

## Usage

### Quick Start

Generate the JavaScript stemmer with default output location:

```bash
make js_stemmer
```

Or run the script directly:

```bash
./scripts/generate_js_stemmer.sh
```

### Custom Output Location

You can specify a custom output file path:

```bash
./scripts/generate_js_stemmer.sh path/to/custom/snowball.js
```

### Default Output

By default, the generated file is placed at:
```
dist/website/snowball.js
```

## Using the Generated Stemmer

### In a Web Browser

```html
<!DOCTYPE html>
<html>
<head>
    <title>Arabic Stemmer Demo</title>
    <script src="dist/website/snowball.js"></script>
</head>
<body>
    <script>
        // Create a new stemmer instance
        var stemmer = new ArabicStemmer();
        
        // Stem a word
        var word = "الطالب";
        var stem = stemmer.stemWord(word);
        console.log(word + " => " + stem); // Output: الطالب => طالب
        
        // Stem multiple words
        var words = ["الطلاب", "والمعلمين", "الحي"];
        words.forEach(function(word) {
            console.log(word + " => " + stemmer.stemWord(word));
        });
    </script>
</body>
</html>
```

### In Node.js

```javascript
const ArabicStemmer = require('./dist/website/snowball.js');

// Create a new stemmer instance
const stemmer = new ArabicStemmer();

// Stem words
console.log(stemmer.stemWord('الطالب'));    // Output: طالب
console.log(stemmer.stemWord('الطلاب'));    // Output: طلاب
console.log(stemmer.stemWord('والمعلمين')); // Output: معلم
```

## When to Regenerate

You should regenerate the JavaScript stemmer whenever:

1. **The light stemmer algorithm is updated**: When `algorithm/arabic.sbl` is modified
2. **Website deployment**: Before deploying updates to the website that use the stemmer

**Note**: This script generates the **light stemmer** (`arabic.sbl`), which removes prefixes and suffixes while preserving the stem form. The repository also includes a **root-based stemmer** (`arabic_root.sbl`) that extracts the linguistic root. To generate a root-based version, modify the `ARABIC_STEMMER` variable in the script to point to `arabic_root.sbl`.

## Output File Structure

The generated `snowball.js` file contains:

1. **Header comments**: Information about auto-generation and source
2. **BaseStemmer class**: The base functionality for stemming
3. **ArabicStemmer class**: The Arabic-specific stemming logic
4. **Module exports**: Compatibility layer for different JavaScript environments (CommonJS, AMD, browser global)

## Optional: Minification

If you have `uglifyjs` installed, the script will automatically generate a minified version:

```bash
# Install uglifyjs (optional)
npm install -g uglify-js

# Run the script (will create snowball.min.js if uglifyjs is available)
./scripts/generate_js_stemmer.sh
```

## Troubleshooting

### Snowball compiler not found

If you get an error about the Snowball compiler not being found:

```bash
# Initialize and update submodules
git submodule update --init --recursive

# Build the project
make build
```

### Permission denied

If you get a permission error:

```bash
chmod +x scripts/generate_js_stemmer.sh
```

## Technical Details

The script performs the following steps:

1. **Checks for Snowball compiler**: Ensures `modules/snowball/snowball` exists
2. **Copies algorithm**: Copies `algorithm/arabic.sbl` to `modules/snowball/algorithms/`
3. **Generates JS**: Runs the Snowball compiler with `-js` flag
4. **Bundles files**: Combines `base-stemmer.js` and `arabic-stemmer.js`
5. **Adds compatibility layer**: Adds module exports for different environments
6. **Optional minification**: Creates a minified version if uglifyjs is available

## Related Files

- `algorithm/arabic.sbl` - The source Snowball algorithm for Arabic stemming
- `modules/snowball/` - The Snowball compiler framework (git submodule)
- `Makefile` - Contains the `js_stemmer` target for easy generation

## See Also

- **Demo**: See `scripts/demo.html` for a complete browser-based demo
- [Snowball Framework Documentation](https://snowballstem.org/)
- [Arabic Stemmer Website](http://arabicstemmer.com)
- [Project README](../README.md)
