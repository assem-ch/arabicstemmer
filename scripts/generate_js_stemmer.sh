#!/bin/bash
#
# Script to automate JS snowball stemmer generation for the website
# This script generates a JavaScript stemmer from the Arabic Snowball algorithm
# and bundles it for use in web browsers.
#
# Usage: ./scripts/generate_js_stemmer.sh [output_file]
#
# Arguments:
#   output_file - Optional. The output JavaScript file path.
#                 Default: dist/website/snowball.js
#

set -e  # Exit on error

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Configuration
SNOWBALL_DIR="$PROJECT_ROOT/modules/snowball"
ALGORITHM_DIR="$PROJECT_ROOT/algorithm"
ARABIC_STEMMER="$ALGORITHM_DIR/arabic.sbl"
OUTPUT_DIR="$PROJECT_ROOT/dist/website"
OUTPUT_FILE="${1:-$OUTPUT_DIR/snowball.js}"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Generating JavaScript Snowball Stemmer ===${NC}"
echo ""

# Step 1: Check if snowball compiler exists
echo "Step 1: Checking Snowball compiler..."
if [ ! -f "$SNOWBALL_DIR/snowball" ]; then
    echo -e "${YELLOW}Snowball compiler not found. Building it first...${NC}"
    cd "$SNOWBALL_DIR"
    make clean
    make
    cd "$PROJECT_ROOT"
fi
echo -e "${GREEN}✓ Snowball compiler ready${NC}"
echo ""

# Step 2: Copy the Arabic stemmer algorithm to Snowball's algorithms directory
echo "Step 2: Copying Arabic stemmer algorithm..."
cp "$ARABIC_STEMMER" "$SNOWBALL_DIR/algorithms/arabic.sbl"
echo -e "${GREEN}✓ Algorithm copied${NC}"
echo ""

# Step 3: Generate JavaScript stemmer
echo "Step 3: Generating JavaScript stemmer..."
cd "$SNOWBALL_DIR"
# Clean previous JS output
rm -rf js_out
mkdir -p js_out

# Generate the JS stemmer for Arabic
./snowball algorithms/arabic.sbl -js -o js_out/arabic-stemmer.js

# Copy base-stemmer.js
cp javascript/base-stemmer.js js_out/base-stemmer.js

echo -e "${GREEN}✓ JavaScript stemmer generated${NC}"
echo ""

# Step 4: Create bundled version for web
echo "Step 4: Creating web-compatible bundle..."
mkdir -p "$OUTPUT_DIR"

# Create a combined file with base-stemmer and arabic-stemmer
cat > "$OUTPUT_FILE" << 'HEADER'
/**
 * Arabic Stemmer - Snowball Generated JavaScript
 * 
 * This file is auto-generated. Do not edit manually.
 * To regenerate, run: ./scripts/generate_js_stemmer.sh
 * 
 * Generated from: algorithm/arabic.sbl
 * Generator: Snowball framework
 */

HEADER

# Append base-stemmer (remove export statement)
echo "// Base Stemmer" >> "$OUTPUT_FILE"
sed '/^export { BaseStemmer };$/d' js_out/base-stemmer.js >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Append Arabic stemmer (remove import statement and export)
echo "// Arabic Stemmer" >> "$OUTPUT_FILE"
sed '/^import { BaseStemmer } from/d; /^export { ArabicStemmer };$/d' js_out/arabic-stemmer.js >> "$OUTPUT_FILE"

# Add exports for browser/Node.js compatibility
cat >> "$OUTPUT_FILE" << 'FOOTER'

// Export for different module systems
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ArabicStemmer;
} else if (typeof define === 'function' && define.amd) {
    define(function() { return ArabicStemmer; });
} else {
    // Browser global - use globalThis for better compatibility
    (typeof globalThis !== 'undefined' ? globalThis : 
     typeof window !== 'undefined' ? window : 
     typeof global !== 'undefined' ? global : 
     this).ArabicStemmer = ArabicStemmer;
}
FOOTER

cd "$PROJECT_ROOT"

echo -e "${GREEN}✓ Web bundle created: $OUTPUT_FILE${NC}"
echo ""

# Step 5: Display usage information
echo -e "${GREEN}=== Generation Complete ===${NC}"
echo ""
echo "The JavaScript stemmer has been generated at:"
echo "  $OUTPUT_FILE"
echo ""
echo "To use it in a web page:"
echo '  <script src="dist/website/snowball.js"></script>'
echo '  <script>'
echo '    var stemmer = new ArabicStemmer();'
echo '    var stem = stemmer.stemWord("الطالب");'
echo '    console.log(stem); // Output: طالب'
echo '  </script>'
echo ""
echo "To use it in Node.js:"
echo "  const ArabicStemmer = require('./dist/website/snowball.js');"
echo "  const stemmer = new ArabicStemmer();"
echo "  console.log(stemmer.stemWord('الطالب'));"
echo ""

# Optional: Generate minified version if uglifyjs is available
if command -v uglifyjs &> /dev/null; then
    echo "Generating minified version..."
    # Create minified filename by replacing .js extension or adding .min.js
    if [[ "$OUTPUT_FILE" == *.js ]]; then
        MINIFIED_FILE="${OUTPUT_FILE%.js}.min.js"
    else
        MINIFIED_FILE="${OUTPUT_FILE}.min.js"
    fi
    uglifyjs "$OUTPUT_FILE" -c -m -o "$MINIFIED_FILE"
    echo -e "${GREEN}✓ Minified version created: $MINIFIED_FILE${NC}"
    echo ""
fi

echo -e "${GREEN}Done!${NC}"
