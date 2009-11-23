
CLASSPATH = import/jdom.jar:.

all:
	javac -classpath $(CLASSPATH) *.java

jar: all
	rm -rf jardir
	mkdir jardir
	mkdir jardir/parts
	mkdir jardir/import
	cp *.class jardir
	cp parts/*.xml jardir/parts
	cp import/*.jar jardir/import
	(cd jardir && jar cvfm ../Capture.jar ../manifest.txt *.class import parts)

run:
	java -classpath $(CLASSPATH) Capture

clean:
	rm -f *.class

