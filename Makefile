
CLASSPATH = import/jdom.jar:.

all:
	javac -classpath $(CLASSPATH) *.java

run:
	java -classpath $(CLASSPATH) Capture

clean:
	rm -f *.class

