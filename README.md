# Welcome to ReDeCheck!

ReDeCheck is an automated tool designed to aid developers with the process of testing the layouts of responsive (i.e.,
"mobile ready") web sites. With the huge range of devices available nowadays, performing adequate quality assurance on a
sufficient range of devices is extremely difficult, if not impossible. Yet, since it is important to have a high-quality
mobile-ready web site, ReDeCheck makes responsive web testing both efficient and effective!

## Installing Maven

The ReDeCheck project has been implemented using Maven, a build automation tool for projects programmed in the Java
programming language. If you wish to build the ReDeCheck tool from its source code, then you will first need to install
Maven on your workstation. If you have already installed Maven, then please go directly to the next section. Otherwise,
follow the installation guidelines at https://maven.apache.org/install.html.

## Downloading and Installing

1. Clone the ReDeCheck project repository using either a graphical Git client or by running the following command at the
   prompt of your terminal window:

   `git clone https://github.com/redecheck/redecheck-tool.git`

2. If you wish to use the provided .jar file included with ReDeCheck, then please skip to "Running ReDeCheck" section.

3. Otherwise, if you want to build ReDeCheck from its source code, then follow the instructions in the next section.

### Installing

As ReDeCheck has been implemented as a Maven project using the Java programming language, the easiest method of
generating the executable tool involves importing the project into an integrated development environment (IDE) and
generating the Java archive (JAR) from inside the IDE. Instructions are presented for doing this using two common IDEs:
Eclipse (https://www.eclipse.org/downloads/) and IntelliJ (https://www.jetbrains.com/idea/download/). However, if you
would prefer to build the project using the command line in an appropriate terminal emulator, then instructions to do so
are also provided.

#### Dependencies

##### Java Version

ReDeCheck has been implemented to run using Java Development Kit (JDK) 7 or 8, which can be downloaded from
http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html and
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html, respectively. Instead of
downloading the JDK from one of the aforementioned web sites, you can also use your operating system's package manager
to install it correctly. After downloading and installing the JDK, you are also likely to have to set Java 1.7 (or, Java
1.8) as the chosen Java Development Kit for the ReDeCheck project. Please follow the instructions provided by either
your operating system or your integrated development environments to accomplish this task.

##### PhantomJS

PhantomJS is a "headless web stack" that allows ReDeCheck to interact with the responsive web site subject to testing;
to learn more about this tool, please visit http://phantomjs.org/.  ReDeCheck requires that you place a PhantomJS
executable inside the `resources/` directory where Maven will store all of the objects that it build during the
compilation phase.  You may have to create this folder manually through your file explorer or terminal window.

ReDeCheck has been developed and tested using PhantomJS 1.9.8, which can be downloaded for all major operating systems
from https://bitbucket.org/ariya/phantomjs/downloads. Once the archive is downloaded, decompress it using a tool
provided by your operating system. Now, move the `phantomjs` executable &mdash; found in the `bin/` directory of your
platform-specific directory for PhantomJS &mdash; to the `resources/` directory where ReDeCheck can access it correctly.

You can test if `phantomjs` is installed correctly by using your terminal window to navigate to the `resources/`
directory and typing `./phantomjs --version`. If this command does not output `1.9.8` or it outputs an error message,
then you need to repair your installation of PhantomJS before continuing to install and use ReDeCheck.

#### Use of X-PERT

Where appropriate to avoid duplicating effort, ReDeCheck re-uses methods from the alignment graph code of the X-PERT
tool that can automatically detect cross-browser incompatibilities. Since X-PERT is available under the MIT license from
https://github.com/gatech/xpert, the developers of ReDeCheck have bundled X-PERT's source code in the ReDeCheck
repository.  X-PERT's source code is contained within the `edu/` directory of the `src/` directory. ReDeCheck contains a
class called `AlignmentGraphFactory` that operates as a "bridge" to the X-PERT classes, thus allowing ReDeCheck to
seamlessly access them.

More details about the X-PERT system are available in the following paper:

- S. Roy Choudhary, M. R. Prasad, and A. Orso, “X-PERT: Accurate identification of cross-browser issues in Web
applications,” in Proc. of the 35th ICSE, 2013

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
6. A JAR file called `redecheck-jar-with-dependencies.jar` should have been created in the `target` directory of ReDeCheck's main directory; if this JAR file does not exist, then the installation with the command line failed and you will not yet be able to use ReDeCheck. Please try these steps again or, alternatively, try one of the methods that uses an IDE.

## Running ReDeCheck

Once you have ReDeCheck correctly packaged and ready to run, then there are two points that you must understand in order
to effectively use the tool:

1. The configuration parameters and how they affect the way the tool works.
2. The reports produced by the tool and the way in which they support the analysis of the differences between the two versions of a web page under test.

### Configuration Parameters

The remainder of this tutorial assumes that ReDeCheck is run from the `target/` directory that contains the
`redecheck-jar-with-dependencies.jar` file. ReDeCheck is run from the command line and takes five compulsory arguments
which control various facets of its execution. The *preamble* parameter is optional and is mainly used when testing web
pages that are stored locally on the file system. ReDeCheck's arguments are defined and described in the following
table:

Argument | Description
-------  | ---------------
oracle   | URL of the oracle version of the web page
test     | URL of the test version of the web page
step     | The step size to use during the sampling process. For example, a step size of 40 would result in the web page being sampled at 40px intervals (e.g., 400px, 440px, 480px, ...)
start    | The viewport width at which to start sampling
end      | The viewport width at which to finish sampling
preamble | Preamble for navigating to the local versions of the web page in the file system

An example showing the command line input is presented below:

```
java -jar redecheck-jar-with-dependencies.jar --oracle demo.com/index --test demo.com/0 --step 20 --start 400 --end 1400
```

In the previous example, the current live version of the web page under test (*demo.com/index*) is being used as the oracle
to compare against the test version (*demo.com/0*).

The remaining three parameters (i.e., step, start, and end) are used to control the generation of the layout model used
to compare the two versions of the web page. The parameters shown in the example above would result in the initial
sampling process examining the web pages at 40px intervals between the viewport widths of 400px and 1400px.

Different combinations of values for these parameters can be used to conduct different types of testing. For instance,
the values chosen for the previous example would support "regular strength" testing across a wide range of devices, from
mobile phones to tablets up to laptops and desktops. However, if the tester only wishes to test the page's layout on
mobile phones, the parameters could be set to 10, 320, and 800, respectively, performing a more thorough test on a
smaller range of device resolutions.

#### Running the Shield Example

Provided with the download of ReDeCheck is directory containing an oracle version (index) and twenty mutants (numbered 0
to 19) of an open-source web site, currently available at http://www.blacktie.co/demo/shield/. As the web pages
themselves are local, rather than live on the web, the *preamble* parameter is required to run the tool:

```
java -jar redecheck-jar-with-dependencies.jar --oracle shield.com/index --test shield.com/2 --step 40 --start 400 --end 1400 --preamble $PATH_TO_REDECHECK_DIRECTORY/testing/
```

Running the command above will allow you to see the tool in action with some different versions of a single web page
being used as input.

### Interpreting and Understanding the Reports

After the tool has finishing comparing the two versions of the web site, a report is produced and should open
automatically on the screen. To get the most out of ReDeCheck, it is important to learn how to interpret these reports,
which should make it as easy as possible to locate and fix any detected problems.

The report is split into three sections, corresponding to the three main layout features of responsive web design:
visibility, alignment and width. Here we'll present an example of each category of error, with a guide on how to
understand them:

#### Visibility Errors

```
HTML/BODY/NAV/BUTTON

Oracle:
    400 -> 767
Test:
    400 -> 775
```

In this example, the report shows that the `button` element contained within the `nav` element is visible at a different
range of viewport widths in the test version compared to the oracle. This could potentially have further impacts, such
as changing the intended alignment of other nearby elements, so it is important to check.

#### Alignment Errors

```
/HTML/BODY/DIV[2]/DIV[4]/DIV/H4/IMG[2] -> /HTML/BODY/DIV[2]/DIV[4]/DIV/H4/IMG
 Oracle:
    400 -> 1300     rightOf,topAlign,bottomAlign

 Test:
    400 -> 440      below
    441 -> 1300     rightOf,topAlign,bottomAlign
```

The example shows that in the oracle version of the web page the two images (`IMG` and `IMG[2]`) are always side by
side, with the `IMG[2]` element always being to the right of `IMG`. However, in the test version, the second image wraps
onto a different line at a small range of narrow viewport widths, which may make the overall layout of the web site look
unprofessional and in some severe cases, difficult to use. It is therefore beneficial to all involved if this issue can
be detected and fixed before the new version of the site goes live.

#### Width Errors
```
/HTML/BODY/DIV[2]/DIV/DIV[6]
 Oracle:
    400 --> 767 : 50.0% of /HTML/BODY/DIV[2]/DIV + 0.0
    768 --> 940 : 0.0% of /HTML/BODY/DIV[2]/DIV + 250.0
 Test:
    400 --> 775 : 50.0% of /HTML/BODY/DIV[2]/DIV + 0.0
    776 --> 940 : 0.0% of /HTML/BODY/DIV[2]/DIV + 250.0
```

The example above shows that element `DIV[6]` exhibits different width behaviour in the two different versions, in this
particular case, from viewport widths 768px to 775px. This can cause unwanted layout issues, especially if other
elements nearby have changed widths while `DIV[6]` has not, or vice versa. It is therefore very useful to know the exact
behaviour each element exhibits and whether they are different in the two versions of the web page.

## Building and Execution Environment

All of the previous instructions for building, installing, and using ReDeCheck have been tested on Mac OS X 10.11 "El
Capitan" and Ubuntu Linux 15.04 "Vivid Vervet". All of the development and testing on the Mac OS X workstations was done
with Java Standard Edition 1.7, while the same work was done with Java Standard Edition 1.8 on the Ubuntu workstation. While
ReDeCheck is very likely to work on other Unix-based development environments, we cannot guarantee correct results for
systems different than the ones mentioned previously. Currently, we do not provide full support for the building,
installation, and use of ReDeCheck on Windows; forks and pull requests that provide this functionality are welcomed.

## Problems or Praise?

If you have any problems with building, installing, or executing ReDeCheck, then please feel free to create an issue
associated with this Git repository using the "Issues" link at the top of this site. The contributors to the
redecheck-tool repository will do all that they can to resolve your issue and ensure that the entire tool works well in
your situation. If you find that ReDeCheck works well, then we also encourage you to "star" and "watch" the project!
Finally, thank you for your interest in responsive web testing and the ReDeCheck tool!
