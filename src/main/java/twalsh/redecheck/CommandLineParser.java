package twalsh.redecheck;

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
    public int ss;

    @Parameter(names = "--start", description = "Start width for sampling")
    public int startWidth;

    @Parameter(names = "--end", description = "End width for sampling")
    public int endWidth;
    
//    @Parameter(names = {"--checkwidths"}, description = "Set of widths to filter results with")
//    public String checkWidths;

    @Parameter(names = "--preamble", description = "Preamble for the two URL parameters")
    public String preamble;
}
