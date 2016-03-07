javac *.java
java -Xmx4g -XX:-UseGCOverheadLimit Apriori 8 2 "path\\transactionDB.txt"
pause