#! /bin/sh

mkdir -p test_inputs
aspell -l en dump master | aspell expand | sort | uniq >| test_inputs/en.txt
sort -R test_inputs/en.txt >| test_inputs/en_random.txt
cat << EOF | python >| test_inputs/dense_dict.txt
chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
chrs = []
for c1 in chars:
  for c2 in chars:
    for c3 in chars:
      print "".join([c1, c2, c3])
EOF
sort -R test_inputs/dense_dict.txt >| test_inputs/dense_dict_random.txt
