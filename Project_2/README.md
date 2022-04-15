## MiniJava Static Checking (Semantic Analysis)

For this proj we are going to build a compiler for MiniJava, a subset of Java.  
MiniJava is designed so that its programs can be compiled by a full Java compiler    
like javac.  

Θα χρησιμοποιησουμε δυο visitors.  
Και θα διασχισουμε δυο φορες το parse tree.  
Μια φορα για να μαζεψουμε ολες τις δηλωσεις.  
Και μια φορα για να ελενξουμε ολες τις αναφορες στις δηλωμενες αναφορες.  