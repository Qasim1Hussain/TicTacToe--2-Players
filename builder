#!/bin/bash
echo "Cleaning.."
rm -rf build
rm -f Client/*.class Server/*.class

echo "Building the Tic Tac Toe"
mkdir -p ./build

echo "Building Server.."
cd Server
javac *.java
jar cvfe Server.jar ServerMain *.class
mv Server.jar ../build/
cd ..

echo "Building Client"
cd Client
javac *.java
jar cvfe Client.jar ClientMain *.class *.png
mv Client.jar ../build/
cd ..

echo "Building Complete"
