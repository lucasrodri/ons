/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

/**
 * The Main class takes care of the execution of the simulator,
 * which includes dealing with the arguments called (or not) on
 * the command line.
 * 
 * @author andred
 */
public class Main {
    
    protected static boolean header = true; //to print the header in table mode
    protected static String simConfigFile = "", raClass = "";
    protected static boolean table, json, verbose, trace;
    public static int seed, calls;
    public static double load, minload, maxload, step = 1.0;
    /**
     * Instantiates a Simulator object and takes the arguments from the command line.
     * Based on the number of arguments, can detect if there are too many or too few,
     * which prints a message teaching how to run WDMSim. If the number is correct,
     * detects which arguments were applied and makes sure they have the expected effect.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        Simulator ons;
        table = json = verbose = trace = false;
        ArgParsing.parse(args);
        for (double load = minload; load <= maxload; load += step) {
            ons = new Simulator();
            ons.Execute(simConfigFile, raClass, trace, verbose, table, json, load, seed);
        }
    }
}
