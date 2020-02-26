JVM = java
JC = javac
.SUFFIXES : .java .class
%.class: 
	  $(JC) $(MODULES) $*.java
	  $(JVM) $(MODULES) $(PACKAGE).$(MAIN)
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
checkJava: 
      ifeq ($(shell java -version 2>&1|grep 14 >/dev/null; printf $$?),0)
		@make clean
		@ls sample/
		@make compile
		@make run
      else
           $(error "Could not find correct java version, please install 14")
      endif
all : checkJavaclean
compile :	
	$(JC) $(MODULES) $(PACKAGE)/*.java
run :
	$(JVM) $(MODULES) $(PACKAGE).$(MAIN)
clean :
	$(RM) $(PACKAGE)/*.class
