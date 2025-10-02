#!/bin/bash
# Activar Java 17 para este proyecto
export JAVA_VERSION=17.0.12-tem
export JAVA_HOME="$HOME/.sdkman/candidates/java/$JAVA_VERSION"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Java activado para universidad: $(java -version 2>&1 | head -n 1)"
