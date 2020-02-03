JVM = java
JC = javac
.SUFFIXES : .java .class
%.class: %.java
	  $(JC) $(MODULES) $<
PACKAGE = sample
CLASSES = \
	sample/Controller.java\
	sample/Parser.java\
	sample/Main.java\
	sample/Transaction.java\
	sample/Relation.java\
	sample/ElementVisuel.java\

MAIN = Main
MODULES = --module-path lib/ --add-modules=javafx.controls,javafx.fxml
default : classes
classes : $(CLASSES:.java=.class)

run :	
	$(JVM) $(MODULES) $(PACKAGE).$(MAIN)

clean :
	$(RM) *.class
