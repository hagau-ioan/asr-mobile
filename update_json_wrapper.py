#!/usr/bin/env python3
"""Script to wrap all JSON files with _meta and data structure"""
import json
import os
from datetime import datetime

# Get current timestamp for _meta
timestamp = datetime.now().isoformat()

# Files to update
files = [
    'composeApp/src/commonMain/composeResources/files/asr_expenses_transactions.json',
    'composeApp/src/commonMain/composeResources/files/cg_donate_transactions.json',
    'composeApp/src/commonMain/composeResources/files/congregations.json',
    'composeApp/src/commonMain/composeResources/files/app_config.json',
    'composeApp/src/commonMain/composeResources/files/asr_expenses_last_12_months.json'
]

for file_path in files:
    if not os.path.exists(file_path):
        print(f'Skipping {file_path} - file not found')
        continue
    
    print(f'Processing {file_path}...')
    with open(file_path, 'r', encoding='utf-8') as f:
        content = json.load(f)
    
    # Wrap with _meta and data
    wrapped = {
        '_meta': {
            'version': timestamp,
            'generated_at': timestamp
        },
        'data': content
    }
    
    # Write back
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(wrapped, f, indent=2, ensure_ascii=False)
    
    print(f'✓ Updated {file_path}')

print('All files updated!')
