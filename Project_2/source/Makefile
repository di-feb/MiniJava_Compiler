JFLAGS = -g
JC = javac
J = java

MAIN = Main.java
CLASSES = Main
INPUT = ../tests/minijava-extra/ManyClasses.java

compile:
	$(J) -jar ../jtb132di.jar -te minijava.jj
	$(J) -jar ../javacc5.jar minijava-jtb.jj
	$(JC) $(MAIN) 

semantic_check:
	$(J) $(CLASSES) $(INPUT)

clean:
	rm -rf *.class *~ *Parse* *Token* *jtb* *CharStream* syntaxtree visitor