package edu.gatech.xpert;

import java.io.File;

public class XpertMain {
	
	static void printUsage() {
		System.err.println("Usage: XpertMain <jobid> <browser1> <browser2>");
		System.exit(1);
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			printUsage();
		}

		String cwd = System.getProperty("user.dir");
		String jobId = args[0];
		String jobDir = cwd + File.separator + jobId + File.separator;
		String path1 = jobDir + args[1] + File.separator + "output";
		String path2 = jobDir + args[2] + File.separator + "output";

		X.debug("* Populating Browser Models");
		BrowserModel model1 = new BrowserModel(path1);
		BrowserModel model2 = new BrowserModel(path2);

		X.debug("* Performing Graph Isomorphism Check");
		IsoChecker ic = new IsoChecker(model1, model2);
		ic.check();

		ErrorReporter er = new ErrorReporter(ic.getResults());
		er.save(jobDir, args[1], args[2]);
	}

}
