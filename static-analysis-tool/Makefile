JC = javac
JVM = java
RM = rm
SRC = src

JFLAGS =-cp .:$(SRC)/:./lib/*

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	$(SRC)/BasicTests.java\
	$(SRC)/Result.java\
	$(SRC)/Main.java\
	$(SRC)/ReadFunction.java\
	$(SRC)/FunctionResult.java\
	$(SRC)/Graph.java\
	$(SRC)/ExportGraph.java\
	$(SRC)/Database.java\
	$(SRC)/Table.java\

MAIN = Main

default: classes

classes : $(CLASSES:.java=.class)

run: $(SRC)/$(MAIN).class
		$(JVM) $(JFLAGS) $(MAIN) $(filter-out $@,$(MAKECMDGOALS))

clean:
	$(RM) $(SRC)/*.class

%:
	@:
