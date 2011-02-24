#! /bin/sh

mkdir -p test_inputs
aspell -l en dump master | aspell expand | sort | uniq >| test_inputs/en.txt
sort -R test_inputs/en.txt >| test_inputs/en_random.txt
