#! /bin/sh

mkdir -p test_inputs
aspell -l en dump master | aspell expand | sort | uniq >| test_inputs/en.txt
sort -R test_inputs/en.txt >| test_inputs/en_random.txt
cat << EOF | python >| test_inputs/dense_dict.txt
chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
for c1 in chars:
  print c1
  for c2 in chars:
    print "".join([c1, c2])
    for c3 in chars:
      print "".join([c1, c2, c3])
EOF
sort -R test_inputs/dense_dict.txt >| test_inputs/dense_dict_random.txt
cat << EOF | python >| test_inputs/shared_prefixes.txt
words = ["the", "brown", "fox", "jumps", "over", "the", "lazy", "dog"]
for w1 in words:
  print w1
  for w2 in words:
    print " ".join([w1, w2])
    for w3 in words:
      print " ".join([w1, w2, w3])
      for w4 in words:
        print " ".join([w1, w2, w3, w4])
        for w5 in words:
          print " ".join([w1, w2, w3, w4, w5])
          for w6 in words:
            print " ".join([w1, w2, w3, w4, w5, w6])
EOF
sort -R test_inputs/shared_prefixes.txt >| test_inputs/shared_prefixes_random.txt

