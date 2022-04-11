JFLAGS = -g
JC = javac
J = java

MAIN = Main.java
CLASSES = Main

compile:
	jflex lexer.flex
	$(J) -jar java-cup-11b.jar -interface -parser Parser parser.cup
	$(JC) -cp java-cup-11b-runtime.jar *.java

execute:
	$(J) -cp java-cup-11b-runtime.jar:. MyMain < Inputs/input_3.txt > $(MAIN)
	$(JC) $(MAIN)  
	$(J) $(CLASSES) $(ARGS)

clean:
	rm -f *.class *~