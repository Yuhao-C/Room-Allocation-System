# Java Requirement
* Java JDK 9 (recommended) or 10
* The program is developed and fully tested with JDK 9
* Avoid using JDK 11 because the program requires JavaFX which is excluded from JDK 11

# How to run
Open `Room_Allocation_System.jar`

__Login details__:\
Username: MUWCI\
Password: muwci2018

Testingdata is provided in the `Room&Student Data` folder in `Product\Project` Folder

# Issues
If double clicking the jar file does not work, 
please first make sure that the Java requirement indicated above is met,
then open the command prompt and navigate to the product folder where `Room_Allocation_System.jar` is located,
and execute command `javaw -jar Room_Allocation_System.jar`

If still nothing happens, you can check your default JDK version by executing `java -version` in CMD.
If you have multiple JDKs and your default JDK version is not 9,
you can set JDK 9 default by modifying the environment variable PATH so that the bin folder of JDK 9 is on the top.
Then you can navigate to the product folder and execute `javaw -jar Room_Allocation_System.jar` in CMD.

Alternatively, without modifying the environment variable PATH,
you can find the path to JDK 9 and simply execute command:
`"<path_to_jdk9>\bin\javaw.exe" -jar Room_Allocation_System.jar`
For example, `"C:\Program Files\Java\jdk-9.0.4\bin\javaw.exe" -jar Room_Allocation_System.jar`

# Source File
The source file of the program is included in `Project Folder\src`.
To run the source code, please use IntelliJ IDEA to open the project folder,
and import all the jar files in the Dependencies folder.

__Thank you!__
