package edu.gatech.xpert;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ErrorReporter {

	XBIResult results;

	public ErrorReporter(XBIResult results) {
		this.results = results;
	}

	public void save(String jobDir, String browser1, String browser2) {
		String dir = String.format("%s/issues/%s-%s", jobDir, browser1, browser2);

		new File(dir).mkdirs();

		// Writeout behavior issues
		StringBuffer behaviorIssues = new StringBuffer();
		for (String bi : results.behaviorIssues) {
			behaviorIssues.append(bi);
			behaviorIssues.append(System.getProperty("line.separator"));
		}
		try {
			FileUtils.writeStringToFile(new File(dir + "/behaviorIssues.csv"),
					behaviorIssues.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Layout issues
		for (String li : results.layoutIssues.keySet()) {
			try {
				StringBuffer layoutIssues = new StringBuffer();
				for (String i : results.layoutIssues.get(li)) {
					layoutIssues.append(i);
					layoutIssues.append(System.getProperty("line.separator"));
				}
				FileUtils.writeStringToFile(
						new File(String.format("%s/layoutIssues_%s.csv", dir, li)), layoutIssues.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Content issues
		for (String li : results.contentIssues.keySet()) {
			try {
				StringBuffer contentIssues = new StringBuffer();
				for (String i : results.contentIssues.get(li)) {
					contentIssues.append(i);
					contentIssues.append(System.getProperty("line.separator"));
				}
				FileUtils.writeStringToFile(
						new File(String.format("%s/contentIssues%s.csv", dir, li)), contentIssues.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
