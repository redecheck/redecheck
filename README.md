Welcome to ReDeCheck!
===================

ReDeCheck is an automated tool designed to aid developers with the process of testing the layouts of responsive web sites. With the huge range of devices available nowadays, performing adequate quality assurance on a sufficient range of devices is extremely difficult, if not impossible.



Running ReDeCheck
-----------

ReDeCheck is run from the command line and takes five compulsory arguments which control various facets of its execution. These arguments are defined and described below:
-------	 	|	---------------
Argument 	|	Description
-------		|	---------------
oracle		|	URL of the oracle version of the webpage
test 		|	URL of the test version of the webpage
step		|	The step size to use during the sampling process. For example, a step size of 40 would result in 				 the webpage being sampled at 40px intervals (400px, 440px, 480px, ...)
start		|	The viewport width at which to start sampling
end			|	The viewport width at which to finish sampling