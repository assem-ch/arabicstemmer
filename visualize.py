#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Interactive visualization for Arabic stemmer algorithm phases

This module creates an interactive HTML visualization showing the different
phases of the Arabic stemming algorithm as an automaton with state transitions.
"""

import json
import os
import sys
import subprocess
import tempfile
from datetime import datetime


class ArabicStemmerVisualizer:
    """
    Visualizes the Arabic stemming algorithm phases as an interactive automaton
    """
    
    def __init__(self, word):
        self.word = word
        self.phases = []
        self.current_state = word
        
    def add_phase(self, phase_name, input_text, output_text, description=""):
        """Add a phase to the visualization"""
        self.phases.append({
            'name': phase_name,
            'input': input_text,
            'output': output_text,
            'description': description,
            'timestamp': len(self.phases)
        })
        self.current_state = output_text
        
    def trace_stemming(self, stemmer_bin='./bin/stemwords'):
        """
        Trace the stemming process by analyzing the algorithm phases
        This is a simulation based on the algorithm structure in arabic.sbl
        """
        # Initial state
        current = self.word
        self.add_phase('Initial', '', current, 'Original word input')
        
        # Phase 1: Normalize pre-processing
        # Simulated normalization (in real implementation, would call actual stemmer)
        normalized = current
        self.add_phase('Normalize_pre', current, normalized, 
                      'Strip vocalization, normalize shaped forms, remove punctuation')
        current = normalized
        
        # Phase 2: Type checking
        self.add_phase('Checks', current, current, 
                      'Determine if word is noun/verb and if defined (has article)')
        
        # Phase 3: Suffix processing
        # Run the actual stemmer to get the result
        try:
            result = subprocess.run(
                [stemmer_bin, '-l', 'ar'],
                input=self.word.encode('utf-8'),
                capture_output=True
            )
            stemmed = result.stdout.decode('utf-8').strip()
        except Exception as e:
            stemmed = current
            
        # Simulate suffix removal
        if len(stemmed) < len(current):
            self.add_phase('Suffix_Processing', current, stemmed,
                          'Remove noun/verb suffixes (pronouns, gender, number markers)')
            current = stemmed
        else:
            self.add_phase('Suffix_Processing', current, current,
                          'No suffix removal needed')
            
        # Phase 4: Prefix processing
        self.add_phase('Prefix_Processing', current, current,
                      'Remove prefixes (articles, conjunctions, prepositions)')
        
        # Phase 5: Post-normalization
        self.add_phase('Normalize_post', current, current,
                      'Normalize remaining hamza forms')
        
        # Final state
        self.add_phase('Final', current, current, 'Final stemmed form')
        
        return stemmed
    
    def generate_html(self, output_file='visualization.html'):
        """Generate an interactive HTML visualization"""
        
        html_template = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Arabic Stemmer - Algorithm Phases Visualization</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
            direction: rtl;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .header .word {
            font-size: 3em;
            font-weight: bold;
            margin: 20px 0;
            font-family: 'Traditional Arabic', 'Arabic Typesetting', serif;
        }
        
        .controls {
            padding: 20px;
            background: #f8f9fa;
            display: flex;
            justify-content: center;
            gap: 10px;
            flex-wrap: wrap;
        }
        
        button {
            background: #667eea;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 8px;
            font-size: 16px;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        button:hover {
            background: #764ba2;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.2);
        }
        
        button:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }
        
        .automaton {
            padding: 40px;
            min-height: 400px;
        }
        
        .phase {
            opacity: 0;
            transform: translateY(20px);
            transition: all 0.5s ease;
            margin-bottom: 30px;
            padding: 20px;
            border-radius: 12px;
            background: #f8f9fa;
            border-left: 5px solid #667eea;
        }
        
        .phase.active {
            opacity: 1;
            transform: translateY(0);
            background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
            border-left: 5px solid #764ba2;
        }
        
        .phase-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        
        .phase-name {
            font-size: 1.5em;
            font-weight: bold;
            color: #667eea;
        }
        
        .phase-number {
            background: #667eea;
            color: white;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
        }
        
        .phase.active .phase-number {
            background: #764ba2;
            animation: pulse 1s infinite;
        }
        
        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.1); }
        }
        
        .phase-description {
            color: #666;
            margin-bottom: 15px;
            font-size: 1.1em;
        }
        
        .transition {
            display: flex;
            align-items: center;
            gap: 20px;
            margin-top: 15px;
            font-family: 'Traditional Arabic', 'Arabic Typesetting', serif;
        }
        
        .state {
            flex: 1;
            padding: 15px;
            background: white;
            border-radius: 8px;
            text-align: center;
            font-size: 2em;
            border: 2px solid #e0e0e0;
            direction: rtl;
        }
        
        .state.highlight {
            background: #fff3cd;
            border-color: #ffc107;
            animation: highlight 1s ease;
        }
        
        @keyframes highlight {
            0%, 100% { background: white; }
            50% { background: #fff3cd; }
        }
        
        .arrow {
            font-size: 2em;
            color: #667eea;
        }
        
        .summary {
            padding: 30px;
            background: #f8f9fa;
            border-top: 2px solid #e0e0e0;
        }
        
        .summary h2 {
            color: #667eea;
            margin-bottom: 15px;
        }
        
        .summary-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        
        .summary-item {
            background: white;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
        }
        
        .summary-label {
            color: #666;
            font-size: 0.9em;
            margin-bottom: 5px;
        }
        
        .summary-value {
            font-size: 1.8em;
            font-weight: bold;
            color: #667eea;
            font-family: 'Traditional Arabic', 'Arabic Typesetting', serif;
        }
        
        .timeline {
            position: relative;
            padding: 20px 0;
        }
        
        .timeline::before {
            content: '';
            position: absolute;
            right: 20px;
            top: 0;
            bottom: 0;
            width: 2px;
            background: #e0e0e0;
        }
        
        .progress-bar {
            position: fixed;
            top: 0;
            left: 0;
            height: 4px;
            background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
            transition: width 0.3s;
            z-index: 1000;
        }
    </style>
</head>
<body>
    <div class="progress-bar" id="progressBar"></div>
    
    <div class="container">
        <div class="header">
            <h1>🔍 Arabic Stemmer Algorithm Visualization</h1>
            <div class="word" id="originalWord">{{ word }}</div>
            <p>Interactive visualization of stemming phases</p>
        </div>
        
        <div class="controls">
            <button onclick="playAll()" id="playBtn">▶️ Play All</button>
            <button onclick="previousPhase()" id="prevBtn" disabled>⏮️ Previous</button>
            <button onclick="nextPhase()" id="nextBtn">⏭️ Next</button>
            <button onclick="reset()" id="resetBtn">🔄 Reset</button>
        </div>
        
        <div class="automaton" id="automaton">
            <div class="timeline">
                {% for phase in phases %}
                <div class="phase" data-phase="{{ loop.index - 1 }}">
                    <div class="phase-header">
                        <div class="phase-name">{{ phase.name }}</div>
                        <div class="phase-number">{{ loop.index }}</div>
                    </div>
                    <div class="phase-description">{{ phase.description }}</div>
                    {% if phase.input or phase.output %}
                    <div class="transition">
                        {% if phase.input %}
                        <div class="state">{{ phase.input }}</div>
                        <div class="arrow">⟵</div>
                        {% endif %}
                        <div class="state highlight">{{ phase.output }}</div>
                    </div>
                    {% endif %}
                </div>
                {% endfor %}
            </div>
        </div>
        
        <div class="summary">
            <h2>Summary</h2>
            <div class="summary-grid">
                <div class="summary-item">
                    <div class="summary-label">Original Word</div>
                    <div class="summary-value">{{ word }}</div>
                </div>
                <div class="summary-item">
                    <div class="summary-label">Stemmed Result</div>
                    <div class="summary-value">{{ result }}</div>
                </div>
                <div class="summary-item">
                    <div class="summary-label">Total Phases</div>
                    <div class="summary-value">{{ phases|length }}</div>
                </div>
                <div class="summary-item">
                    <div class="summary-label">Characters Removed</div>
                    <div class="summary-value">{{ word|length - result|length }}</div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        let currentPhase = 0;
        const phases = document.querySelectorAll('.phase');
        const totalPhases = phases.length;
        let isPlaying = false;
        
        function updateButtons() {
            document.getElementById('prevBtn').disabled = currentPhase === 0;
            document.getElementById('nextBtn').disabled = currentPhase >= totalPhases - 1;
            document.getElementById('playBtn').disabled = isPlaying || currentPhase >= totalPhases - 1;
        }
        
        function updateProgressBar() {
            const progress = ((currentPhase + 1) / totalPhases) * 100;
            document.getElementById('progressBar').style.width = progress + '%';
        }
        
        function showPhase(index) {
            phases.forEach((phase, i) => {
                if (i <= index) {
                    phase.classList.add('active');
                } else {
                    phase.classList.remove('active');
                }
            });
            
            // Scroll to current phase
            if (phases[index]) {
                phases[index].scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
            
            updateButtons();
            updateProgressBar();
        }
        
        function nextPhase() {
            if (currentPhase < totalPhases - 1) {
                currentPhase++;
                showPhase(currentPhase);
            }
        }
        
        function previousPhase() {
            if (currentPhase > 0) {
                currentPhase--;
                showPhase(currentPhase);
            }
        }
        
        function reset() {
            currentPhase = 0;
            isPlaying = false;
            showPhase(-1);
            setTimeout(() => {
                showPhase(0);
            }, 100);
        }
        
        async function playAll() {
            isPlaying = true;
            updateButtons();
            
            for (let i = currentPhase; i < totalPhases; i++) {
                if (!isPlaying) break;
                currentPhase = i;
                showPhase(currentPhase);
                await new Promise(resolve => setTimeout(resolve, 1500));
            }
            
            isPlaying = false;
            updateButtons();
        }
        
        // Initialize
        showPhase(0);
        
        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowRight' || e.key === 'ArrowLeft') {
                previousPhase();
            } else if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
                nextPhase();
            } else if (e.key === ' ') {
                e.preventDefault();
                if (!isPlaying) {
                    playAll();
                }
            }
        });
    </script>
</body>
</html>
"""
        
        # Use simple string replacement since we don't have jinja2
        html = html_template
        html = html.replace('{{ word }}', self.word)
        html = html.replace('{{ result }}', self.phases[-1]['output'] if self.phases else self.word)
        
        # Build phases HTML
        phases_html = ''
        for i, phase in enumerate(self.phases):
            phase_html = f'''
                <div class="phase" data-phase="{i}">
                    <div class="phase-header">
                        <div class="phase-name">{phase['name']}</div>
                        <div class="phase-number">{i + 1}</div>
                    </div>
                    <div class="phase-description">{phase['description']}</div>
            '''
            
            if phase['input'] or phase['output']:
                phase_html += '<div class="transition">'
                if phase['input']:
                    phase_html += f'''
                        <div class="state">{phase['input']}</div>
                        <div class="arrow">⟵</div>
                    '''
                phase_html += f'<div class="state highlight">{phase["output"]}</div>'
                phase_html += '</div>'
            
            phase_html += '</div>'
            phases_html += phase_html
        
        html = html.replace('{% for phase in phases %}', '').replace('{% endfor %}', '')
        html = html.replace('{{ loop.index - 1 }}', '').replace('{{ loop.index }}', '')
        html = html.replace('{{ phase.name }}', '').replace('{{ phase.description }}', '')
        html = html.replace('{{ phase.input }}', '').replace('{{ phase.output }}', '')
        html = html.replace('{% if phase.input or phase.output %}', '').replace('{% endif %}', '')
        html = html.replace('{% if phase.input %}', '')
        
        # Find the timeline div and insert phases
        timeline_start = html.find('<div class="timeline">')
        timeline_end = html.find('</div>', timeline_start + 100)
        html = html[:timeline_start + 22] + phases_html + html[timeline_end:]
        
        # Replace length calculations
        html = html.replace('{{ phases|length }}', str(len(self.phases)))
        html = html.replace('{{ word|length - result|length }}', 
                          str(len(self.word) - len(self.phases[-1]['output'] if self.phases else self.word)))
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(html)
        
        return output_file


def visualize_word(word, output_file=None, stemmer_bin='./bin/stemwords'):
    """
    Visualize the stemming process for a given Arabic word
    
    Args:
        word: The Arabic word to stem and visualize
        output_file: Optional output HTML file path
        stemmer_bin: Path to the stemmer binary
    
    Returns:
        Path to the generated HTML visualization file
    """
    if output_file is None:
        output_file = f'visualization_{datetime.now().strftime("%Y%m%d_%H%M%S")}.html'
    
    visualizer = ArabicStemmerVisualizer(word)
    stemmed = visualizer.trace_stemming(stemmer_bin)
    html_file = visualizer.generate_html(output_file)
    
    print(f"✓ Visualization generated: {html_file}")
    print(f"  Original: {word}")
    print(f"  Stemmed:  {stemmed}")
    print(f"\nOpen {html_file} in your browser to view the interactive visualization.")
    
    return html_file


def main():
    """Main entry point for command-line usage"""
    if len(sys.argv) < 2:
        print("Usage: python visualize.py <arabic_word> [output_file.html]")
        print("\nExample:")
        print("  python visualize.py الطالب")
        print("  python visualize.py الطالب my_visualization.html")
        sys.exit(1)
    
    word = sys.argv[1]
    output_file = sys.argv[2] if len(sys.argv) > 2 else None
    
    # Try to find stemmer binary
    stemmer_paths = [
        './bin/stemwords',
        '../bin/stemwords',
        'bin/stemwords'
    ]
    
    stemmer_bin = None
    for path in stemmer_paths:
        if os.path.exists(path):
            stemmer_bin = path
            break
    
    if not stemmer_bin:
        print("Warning: Stemmer binary not found. Using simulation mode.")
        stemmer_bin = './bin/stemwords'
    
    visualize_word(word, output_file, stemmer_bin)


if __name__ == '__main__':
    main()
