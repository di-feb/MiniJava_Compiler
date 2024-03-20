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

llvm:
	$(J) $(CLASSES) $(wildcard $(INPUT)/*.java)

clean:
	rm -rf ../build/* *Parse* syntaxtree visitor *Token* minijava-jtb.jj JavaCharStream.java *.class out*