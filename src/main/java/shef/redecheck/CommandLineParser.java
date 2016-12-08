package shef.redecheck;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomaswalsh on 27/08/15.
 */
public class CommandLineParser {
    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = {"--oracle"}, description = "URL of oracle webpage")
    public String oracle;

    @Parameter(names = {"--test"}, description = "URL of test webpage")
    public String test;

    @Parameter(names = "--step", description = "Step size")
    public int ss = 60;

    @Parameter(names = "--start", description = "Start width for sampling")
    public int startWidth = 400;

    @Parameter(names = "--end", description = "End width for sampling")
    public int endWidth = 1400;

    @Parameter(names = "--sampling", description = "Sampling technique")
    public String sampling = "uniformBP";

    @Parameter(names = "--binary", description = "Use binary search or not")
    public boolean binary = true;

    @Parameter(names = "--preamble", description = "Preamble for the two URL parameters")
    public String preamble;
    
    @Parameter(names = "--mutantID", description = "Unique mutant ID (for experiments)")
    public String mutantID;

    @Parameter(names = "--screenshot", description = "take screenshots (for experiments)")
    public boolean screenshot;

    @Parameter(names = "--tool", description = "run regular tool")
    public boolean tool;

    @Parameter(names = "--xpert", description = "run x-pert")
    public boolean xpert;

    @Parameter(names = "--timing", description = "writes timing data to special directory")
    public boolean timing;

    @Parameter(names = "--timingID", description = "iteration number for timing data")
    public int timingID;

    @Parameter(names = "--browser", description = "The browser to use for the test run")
    public String browser = "phantom";

    @Parameter(names = "--baselines", description = "Whether to run the baseline approaches")
    public boolean baselines;

    @Parameter(names = "--results", description = "Whether to process experimental results")
    public boolean results;

    @Parameter(names = "--url", description = "The URL to check for faults.")
    public String url;

}
