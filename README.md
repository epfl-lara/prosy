# Quick start

To run the example of the paper:

    java -jar prosy.jar src/main/scala/example/sample-paper.scala

You may also try the other files of the form "sample-*.scala"

# To compile the project 

    sbt assembly
    mv target/scala-2.12/prosy-assembly-0.1-SNAPSHOT.jar prosy.jar

# To start the synthesis

    java -jar prosy.jar [NUMBER_CHOICE] SCALA_FILES
    NUMBER_CHOICE: the maximal number of propositions per page (default: 9).
    SCALA_FILES: a space-separated list of scala files, containing case class definitions

Alternatively, you can use the following command

    sbt "run [NUMBER_CHOICE] SCALA_FILES"

# To make an archive with the project:

    tar --exclude=prosy.tar.gz --exclude=project/project --exclude=target --exclude=project/target -czf prosy.tar.gz *
