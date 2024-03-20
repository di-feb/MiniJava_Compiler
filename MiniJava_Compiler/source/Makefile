JFLAGS = -g
JC = javac
J = java

MAIN = Main.java
CLASSES = Main
INPUT = ../tests

compile:
	$(J) -jar ../jtb132di.jar -te minijava.jj
	$(J) -jar ../javacc5.jar minijava-jtb.jj
	$(JC) $(MAIN) 

semantic_check:
	$(J) $(CLASSES)  $(wildcard $(INPUT)/*/*.java)
	$(J) $(CLASSES)  $(wildcard $(INPUT)/*.java)
clean:
	rm -rf *.class *~ *Parse* *Token* *jtb* *CharStream* syntaxtree visitor