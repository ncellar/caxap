- `mvn compile` to compile, using caxap to preprocess the sources. Caxap needs
   to have been compiled by running `mvn package` in the parent
   folder. Alternatively, copy the caxap jar from `prebuilt` to `target`.
- `mvn exec:java -Drun` to run the examples (after compiling).
- `mvn clean` to remove all generated artifacts.