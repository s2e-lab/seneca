#!/bin/bash

# Check if the correct number of arguments is provided
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <jar1> <jar2> <output_jar>"
    exit 1
fi

# Assign arguments to variables
jar1="$1"
jar2="$2"
output_jar="$3"

# Create temporary directories
tmp_dir1=$(mktemp -d)
tmp_dir2=$(mktemp -d)

# Unpack the jar files
unzip -q "$jar1" -d "$tmp_dir1"
unzip -q "$jar2" -d "$tmp_dir2"

# Merge them together
cp -r "$tmp_dir2"/* "$tmp_dir1"

# Repack into a single jar
jar cf "$output_jar" -C "$tmp_dir1" .

# Clean up the temporary directories
rm -rf "$tmp_dir1" "$tmp_dir2"

#echo "Merged $jar1 and $jar2 into $output_jar"
