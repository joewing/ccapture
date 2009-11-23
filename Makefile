
CLASSPATH = .

all:
	javac -classpath $(CLASSPATH) *.java

jar: all
	rm -rf jardir
	mkdir jardir
	mkdir jardir/parts
	cp *.class jardir
	cp parts/*.xml jardir/parts
	(cd jardir && jar cvfm ../Capture.jar ../manifest.txt *.class parts)

run:
	java -classpath $(CLASSPATH) Capture

clean:
	rm -f *.class

