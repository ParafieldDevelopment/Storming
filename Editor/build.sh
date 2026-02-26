#!/bin/bash

# Configuration
SRC_DIR="src"
LIB_DIR="lib"
BUILD_DIR="build/classes"
MAIN_CLASS="com.parafield.storming.EditorApp"

# Create build directory
mkdir -p $BUILD_DIR

# Find all Java files
JAVA_FILES=$(find $SRC_DIR -name "*.java")

# Build classpath
CP="."
if [ -d "$LIB_DIR" ]; then
    for jar in $LIB_DIR/*.jar; do
        CP="$CP:$jar"
    done
fi

# Compile
echo "Compiling..."
javac -d $BUILD_DIR -cp "$CP" $JAVA_FILES

# Run
if [ $? -eq 0 ]; then
    echo "Running..."
    java -cp "$BUILD_DIR:$CP" $MAIN_CLASS
else
    echo "Compilation failed."
fi
