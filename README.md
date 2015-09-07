#Welcome to ReDeCheck!

ReDeCheck is an automated tool designed to aid developers with the process of testing the layouts of responsive web sites. With the huge range of devices available nowadays, performing adequate quality assurance on a sufficient range of devices is extremely difficult, if not impossible.

##Downloading and Installing

1. Clone the ReDeCheck project repository using either a VCS client or the following command
```
git clone https://github.com/redecheck/redecheck-tool.git
```
2. If you wish to use the .jar file included with the download, please skip to "Running ReDeCheck"
3. Otherwise, follow the instructions in the next section.

### Installing

If using the IntelliJ IDE for Java:
1. Select 'File' -> 'Open'
2. Navigate to the root directory of your copy of ReDeCheck
3. Select the 'pom.xml' file and click 'Finish'
4. Open the Maven Projects toolbar  using 'View' -> 'Tool Windows' -> 'Maven Projects'
5. Select the ReDeCheck project and click 'package'
6. A jar entitled redecheck-jar-with-dependencies.jar should have been created in the */target* directory of your ReDeCheck download.


## Running ReDeCheck

ReDeCheck is run from the command line and takes five compulsory arguments which control various facets of its execution. These arguments are defined and described below:

Argument 	|	Description
-------		|	---------------
oracle		|	URL of the oracle version of the webpage
test 		|	URL of the test version of the webpage
step		|	The step size to use during the sampling process. For example, a step size of 40 would result in 				 the webpage being sampled at 40px intervals (400px, 440px, 480px, ...)
start		|	The viewport width at which to start sampling
end			|	The viewport width at which to finish sampling

An example showing the command line input is presented below:

```
java -jar redecheck-jar-with-dependencies.jar --oracle live.mysite.com/home --test dev.mysite.com/home --step 40 --start 400 --end 1400
```

In the example above, the current live version of the webpage under test (*live.mysite.com/home*) is being used as the oracle to compare against the test version (*dev.mysite.com/home*).

The remaining three parameters are used to control the generation of layout model used to compare the two versions of the webpage. The parameters shown in the example above would result the initial sampling process examining the webpages at 40px intervals between the viewport widths of 400px and 1400px. Different combinations of values for these parameters can be used to conduct different types of testing. For instance, the values above would produce in a regular strength testing across a wide range of devices, from smartphones to tablets and up to laptops and desktops. However, if the tester only wishes to test the page's layout on smartphones, the parameters could be set to 10, 320 and 800 respectively, performing a more thorough test on a smaller range of device resolutions. 