#Welcome to ReDeCheck!

ReDeCheck is an automated tool designed to aid developers with the process of testing the layouts of responsive web sites. With the huge range of devices available nowadays, performing adequate quality assurance on a sufficient range of devices is extremely difficult, if not impossible.

##Maven

The project has been implemented using Maven, a build automation tool for Java projects. If you wish to build the tool from scratch, you will first need to install on your workstation. If you already have Maven installed, please skip to the next section. Otherwise, follow the installation guidelines here https://maven.apache.org/install.html.

##Downloading and Installing

1. Clone the ReDeCheck project repository using either a VCS client or the following command
`
git clone https://github.com/redecheck/redecheck-tool.git
`
2. If you wish to use the .jar file included with the download, please skip to "Running ReDeCheck"
3. Otherwise, follow the instructions in the next section.

### Installing

As ReDeCheck has been implemented as a Maven project using Java, the easiest method of generating the executable tool involves importing the project into an Integrated Development Environment (IDE) and generating the .jar from inside the IDE. Instructions are presented for doing this using two common IDEs; Eclipse (https://www.eclipse.org/downloads/) and IntelliJ (https://www.jetbrains.com/idea/download/). However, if you would prefer to build the project using the command line, instructions to do so are also provided.

#### Dependencies

##### Java Version

ReDeCheck has been implemented to run using Java Development Kit 7, which can be downloaded from `http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html`. After downloading and installing, you are also likely to have set Java 1.7 as the chosen JDK for the ReDeCheck project.

##### PhantomJS

ReDeCheck requires a PhantomJS executable be placed inside the `resources` directory, where Maven stored all of its built objects. You may have to create this folder manually through your file explorer. This tool has been developed and tested using PhantomJS 1.9.8, which can be downloaded for all major operating systems from `https://bitbucket.org/ariya/phantomjs/downloads`. Once downloaded, move the executable phantomjs file from `bin` to `resources` where ReDeCheck can access it correctly.

#### Use of X-PERT

ReDeCheck took inspiration from X-PERT, a software solution for automatically detecting cross browser incompatibilities, and reuses small amounts of code written by the developers of X-PERT. The licensing of the released version of X-PERT, available at https://github.com/gatech/xpert allows it to be bundled with ReDeCheck. The X-PERT code is contained within the `edu` directory of the `src` folder. A class 'AlignmentGraphFactory.java' is stored as a "bridge class" with the X-PERT class files to allow ReDeCheck to access them.

#### If using Eclipse:

1. Select 'File' -> 'Import'.
2. From the project options, select 'Maven' -> 'Existing Maven Projects'.
3. Select the root directory of your downloaded copy of ReDeCheck.
4. Click 'Finish' to complete the import.
5. To generate the jar file, select 'Run' -> 'Run As' -> 'maven install'.
6. A jar entitled redecheck-jar-with-dependencies.jar should have been created in the */target* directory of your ReDeCheck download.

#### If using IntelliJ:

1. Select 'File' -> 'Open'.
2. Navigate to the root directory of your copy of ReDeCheck.
3. Select the 'pom.xml' file and click 'Finish'.
4. Open the Maven Projects toolbar  using 'View' -> 'Tool Windows' -> 'Maven Projects'.
5. Select the ReDeCheck project and click 'package'.
6. A jar entitled redecheck-jar-with-dependencies.jar should have been created in the */target* directory of your ReDeCheck download.

#### If using command line:

1. Navigate to the root directory containing your copy of ReDeCheck. (Use the cd command as shown below)
2. Type the following command to build the tool: `mvn package`
3. Maven will build the project from scratch, downloading all the required dependencies for the project automatically.
4. A jar entitled redecheck-jar-with-dependencies.jar should have been created in the */target* directory of your ReDeCheck download.

## Running ReDeCheck

Once you have ReDeCheck correctly packaged and ready to run, there are two main requirements for effective use of the tool:

1. Understanding the configuration parameters and how they affect the way the tool works
2. Understanding the reports produced by the tool to effectively analyse the differences between the two versions of the webpage under test.

### Configuration Parameters

ReDeCheck is run from the command line and takes five compulsory arguments which control various facets of its execution. The *preamble* parameter is optional, and is mainly used when testing local web pages. These arguments are defined and described below:

Argument     |	Description
-------		|	---------------
oracle		|	URL of the oracle version of the webpage
test 		|	URL of the test version of the webpage
step		|	The step size to use during the sampling process. For example, a step size of 40 would result in the webpage being sampled at 40px intervals (400px, 440px, 480px, ...)
start		|	The viewport width at which to start sampling
end			|	The viewport width at which to finish sampling
preamble  |	Preamble for navigating to local versions of the webpage in the file system.

An example showing the command line input is presented below:

```
java -jar redecheck-jar-with-dependencies.jar --oracle demo.com/index --test demo.com/0 --step 20 --start 400 --end 1400
```

In the example above, the current live version of the webpage under test (*demo.com/index*) is being used as the oracle to compare against the test version (*demo.com/0*).

The remaining three parameters (step, start, end) are used to control the generation of layout model used to compare the two versions of the webpage. The parameters shown in the example above would result the initial sampling process examining the webpages at 40px intervals between the viewport widths of 400px and 1400px.

Different combinations of values for these parameters can be used to conduct different types of testing. For instance, the values above would produce in a regular strength testing across a wide range of devices, from smartphones to tablets and up to laptops and desktops. However, if the tester only wishes to test the page's layout on smartphones, the parameters could be set to 10, 320 and 800 respectively, performing a more thorough test on a smaller range of device resolutions.

#### Running the Shield Example

Provided with the download of ReDeCheck is directory containing an oracle version (index) and twenty mutants (numbered 0 to 19) of an open-source website, currently available at http://www.blacktie.co/demo/shield/. As the webpages themselves are local, rather than live on the web, the *preamble* parameter is required to run the tool:

```
java -jar redecheck-jar-with-dependencies.jar --oracle shield.com/index --test shield.com/2 --step 40 --start 400 --end 1400 --preamble $PATH_TO_REDECHECK_DIRECTORY/testing/
```

Running the command above will allow you to see the tool in action with some different versions of a single webpage being used as input.

### Interpreting and Understanding the Reports

After the tool has finishing comparing the two versions of the website, a report is produced and should open automatically on the screen. To get the most out of ReDeCheck, it is important to learn how to interpret these reports, which should make it as easy as possible to locate and fix any detected problems.

The report is split into three sections, corresponding to the three main layout features of responsive web design: visibility, alignment and width. Here we'll present an example of each category of error, with a guide on how to understand them:

#### Visibility Errors

```
HTML/BODY/NAV/BUTTON

Oracle:
	400 -> 767
Test:
	400 -> 775
```

In this example, the report shows that the `button` element contained within the `nav` element is visible at a different range of viewport widths in the test version compared to the oracle. This could potentially have further impacts, such as changing the intended alignment of other nearby elements, so it is important to check.

#### Alignment Errors

```
/HTML/BODY/DIV[2]/DIV[4]/DIV/H4/IMG[2] -> /HTML/BODY/DIV[2]/DIV[4]/DIV/H4/IMG
 Oracle:
	400 -> 1300     rightOf,topAlign,bottomAlign

 Test:
	400 -> 440     	below
	441 -> 1300     rightOf,topAlign,bottomAlign
```

The example shows that in the oracle version of the webpage the two images (`IMG` and `IMG[2]`) are always side by side, with the `IMG[2]` element always being to the right of `IMG`. However, in the test version, the second image wraps onto a different line at a small range of narrow viewport widths, which may make the overall layout of the website look unprofessional and in some severe cases, difficult to use. It is therefore beneficial to all involved if this issue can be detected and fixed before the new version of the site goes live.

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
The example above shows that element `DIV[6]` exhibits different width behaviour in the two different versions, in this particular case, from viewport widths 768px to 775px. This can cause unwanted layout issues, especially if other elements nearby have changed widths while `DIV[6]` has not, or vice versa. It is therefore very useful to know the exact behaviour each element exhibits and whether they are different in the two versions of the webpage.

## Troubleshooting

If you have any problems with either the installation or execution of ReDeCheck, please feel free to create an issues on the repository using the link above, and we'll do all we can to resolve your issue. Alternatively, if you'd like to get in touch with us directly, you can email us at redechecktool@gmail.com.
