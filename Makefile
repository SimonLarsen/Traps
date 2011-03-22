main: Game.java
	javac *.java
	#javac -cp .:jl1.0.1.jar *.java

run:
	appletviewer Traps.html

clean:
	rm -rf *.class
