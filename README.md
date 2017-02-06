# Welcome to ReDeCheck!

ReDeCheck (REsponsive DEsign CHECKer, pronounced “Ready Check”) is an automated tool designed to aid developers during
the process of testing the layouts of responsive (i.e., "mobile ready") web sites. With the huge range of devices
currently available, performing adequate quality assurance on a sufficient range of devices is extremely difficult, if
not impossible. Yet, since it is important to have a high-quality mobile-ready web site, ReDeCheck makes responsive web
testing both efficient and effective!

## Installing Maven

The ReDeCheck project has been implemented using Maven, a build automation tool for projects programmed in the Java
programming language. In order to build the ReDeCheck tool from its source code, then you will first need to install
Maven on your workstation. If you have already installed Maven, then please go directly to the next section. Otherwise,
follow the installation guidelines at https://maven.apache.org/install.html.

## Downloading ReDeCheck

Clone the ReDeCheck project repository using either a graphical Git client or by running the following command at the
   prompt of your terminal window:

   `git clone https://github.com/redecheck/redecheck-tool.git`
   
## Installing ReDeCheck

As ReDeCheck has been implemented as a Maven project using the Java programming language, the easiest method of
generating the executable tool involves importing the project into an integrated development environment (IDE) and
generating the Java archive (JAR) from inside the IDE. Instructions are presented for doing this using two common IDEs:
Eclipse (https://www.eclipse.org/downloads/) and IntelliJ (https://www.jetbrains.com/idea/download/). However, if you
would prefer to build the project using the command line in an appropriate terminal emulator, then instructions to do so
are also provided.

#### Dependencies

The `pom.xml` file provided in the repository will handle the vast majority of dependencies needed for ReDeCheck to install and run correctly. However, before attempting to run ReDeCheck to test your websites, please ensure your setup is complete with the following requirements:

##### Java Version

ReDeCheck has been implemented to run using Java Development Kit (JDK) 7 or 8, which can be downloaded from
http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html and
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html, respectively. Instead of
downloading the JDK from one of the aforementioned web sites, you can also use your operating system's package manager
to install it correctly. After downloading and installing the JDK, you are also likely to have to set Java 1.7 (or, Java
1.8) as the chosen Java Development Kit for the ReDeCheck project. Please follow the instructions provided by either
your operating system or your integrated development environments to accomplish this task.

##### Firefox

ReDeCheck uses the popular Mozilla Firefox web browser to render and analyse the webpage under test. Currently, it relies upon version 46, which is available at 
https://ftp.mozilla.org/pub/firefox/releases/46.0/, for all major operating systems. Simply download and install the relevant version for your setup and ReDeCheck will handle the rest.

#### Installation with Eclipse

1. Select 'File' &rarr; 'Import'.
2. From the project options, select 'Maven' &rarr; 'Existing Maven Projects'.
3. Select the root directory of your downloaded copy of ReDeCheck.
4. Click 'Finish' to complete the import.
5. To generate the JAR file, select 'Run' &rarr; 'Run As' &rarr; 'maven install'.
6. A JAR file called `redecheck-jar-with-dependencies.jar` should have been created in the `target` directory of ReDeCheck's main directory; if this JAR file does not exist, then the installation with Eclipse failed and you will not yet be able to use ReDeCheck. Please try these steps again or, alternatively, try another IDE or the command-line-based approach.

#### Installation with IntelliJ

1. Select 'File' &rarr; 'Open'.
2. Navigate to the root directory of your installation of ReDeCheck.
3. Select the 'pom.xml' file and click 'Finish'.
4. Open the Maven Projects toolbar using 'View' &rarr; 'Tool Windows' &rarr; 'Maven Projects'.
5. Select the ReDeCheck project and click 'package'.
6. A JAR file called `redecheck-jar-with-dependencies.jar` should have been created in the `target` directory of ReDeCheck's main directory; if this JAR file does not exist, then the installation with IntelliJ failed and you will not yet be able to use ReDeCheck. Please try these steps again or, alternatively, try another IDE or the command-line-based approach.

#### Installation at the Command Line

1. Navigate to the root directory containing of your installation of ReDeCheck.
2. Type the following command to build the tool: `mvn package`
3. Maven will build the project from scratch, downloading all the required dependencies for the project automatically.
4. A JAR file called `redecheck-jar-with-dependencies.jar` should have been created in the `target` directory of ReDeCheck's main directory; if this JAR file does not exist, then the installation with the command line failed and you will not yet be able to use ReDeCheck. Please try these steps again or, alternatively, try one of the methods that uses an IDE.

## Running ReDeCheck

Once you have ReDeCheck correctly packaged and ready to run on your workstation, you have two options regarding the running of the tool:

1) Run ReDeCheck on a fully live site on the World Wide Web.
2) Run ReDeCheck on a local web page file.

Examples of how to run ReDeCheck in each configuration are shown below:

#### Running on a Live Site


