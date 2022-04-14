# define compiler and compiler flag variables
JFLAGS = -g
JC = javac
J = java

MAIN = Calculator.java
CLASSES = Calculator
ARGS = 

# RM is a predefined macro in make (RM = rm -f)
clean: 
	$(RM) *.class

# Run
run: 
	$(JC) $(MAIN)  
	$(J) $(CLASSES) $(ARGS)