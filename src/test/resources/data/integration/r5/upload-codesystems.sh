#!/bin/bash

FHIR_ENDPOINT="http://localhost:8080/fhir/r4/CodeSystem"
CODE_SYSTEMS_DIR="package"

echo "Starting upload of CodeSystems to $FHIR_ENDPOINT"
echo ""

count=0
success=0
failed=0
skipped=0

for file in "$CODE_SYSTEMS_DIR"/CodeSystem*.json; do
    if [ -f "$file" ]; then
        count=$((count + 1))
        filename=$(basename "$file")

        echo "[$count] Checking $filename"

        # Check for required fields
        title=$(jq -r '.title // ""' "$file" 2>/dev/null)
        publisher=$(jq -r '.publisher // ""' "$file" 2>/dev/null)
        version=$(jq -r '.version // ""' "$file" 2>/dev/null)

        # Build list of missing fields
        missing=()
        [ -z "$title" ] && missing+=("title")
        [ -z "$publisher" ] && missing+=("publisher")
        [ -z "$version" ] && missing+=("version")

        # If any required fields are missing, skip the file
        if [ ${#missing[@]} -gt 0 ]; then
            echo "  Skipped! Missing: ${missing[*]}"
            skipped=$((skipped + 1))
            echo ""
            continue
        fi

        # Upload the file
        echo "[$count] Uploading $filename"

        http_code=$(curl -s -w "%{http_code}" -o /dev/null \
            -X POST \
            -H "Content-Type: application/fhir+json" \
            -d @"$file" \
            "$FHIR_ENDPOINT")

        if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
            echo "  Success! HTTP $http_code"
            success=$((success + 1))
        else
            echo "  Failed! HTTP $http_code"
            failed=$((failed + 1))
        fi

        echo ""

        sleep 1
    fi
done

echo "Upload complete!"
echo "Total: $count"
echo "Successful: $success"
echo "Failed: $failed"
echo "Skipped (missing attributes): $skipped"

