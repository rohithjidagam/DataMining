Run The Apriori using following commands:

1)javac *.java
2)java -Xmx4g -XX:-UseGCOverheadLimit Apriori s k InputfileName

Example : java -Xmx4g -XX:-UseGCOverheadLimit Apriori 5 3 "transactionDB.txt"

Input file should be placed in the same directory the code file resides.

Output file was given the format mentioned in the mail as
out_s=10_k=2+.txt

Output File will be generated in the same directory the code file resides.

Use jdk 1.8 to run the code.

Note: Make sure that the input file and the program are in the same folder.