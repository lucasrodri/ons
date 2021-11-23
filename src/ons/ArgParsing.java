/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

/**
 *
 * @author lucasrc
 */
public final class ArgParsing {

    private static final String usage = "Usage: EONSim -f <simulation_file> -s <seed> [-trace] [-ra <raClass>] [-c <calls>] [ [-l <load>] or [-L <minload maxload step>] ] [-verbose | -table | -json]";

    protected static void printUsage() {
        System.out.println(usage);
        System.exit(0);
    }
    
    private static void printHelp() {
        System.out.println(usage);
        System.out.println("Arguments:\n"
                + "\t-h                              Print the help (this message) and exit\n"
                + "\t-f <simulation_file>            XML simulation file\n"
                + "\t-s <seed>                       The simulation seed (1-25)\n"
                + "\t-ra <raClass>                   Routing Algorithm Class\n"
                + "\t-c <calls>                      Number of Calls\n"
                + "\t-l <load>                       The load in Erlang\n"
                + "\t-L <minload maxload step>       For various load sequences, minload maxload step in Erlang\n"
                + "\t-trace                          To generate the trace file\n"
                + "\t-verbose                        To view detailed output\n"
                + "\t-table                          To display the output in tabular format\n"
                + "\t-json                           To display output in JSON format\n");
        System.exit(0);
    }

    static void parse(String[] args) {
        boolean flag = false;
        if(args.length == 1 && args[0].equals("-h")) {
            printHelp();
        }
        String comand = "";
        for (String arg : args) {
            comand += arg + " ";
        }
        int flags = 0, loads = 0, outputs = 0, seeds = 0, files = 0, ras = 0, traces = 0, calls = 0;
        files += comand.split("-f ", -1).length-1;
        seeds += comand.split("-s ", -1).length-1;
        calls += comand.split("-c ", -1).length-1;
        ras += comand.split("-ra ", -1).length-1;
        loads += comand.split("-l ", -1).length-1;
        loads += comand.split("-L ", -1).length-1;
        traces += comand.split("-trace", -1).length-1;
        outputs += comand.split("-verbose", -1).length-1;
        outputs += comand.split("-table", -1).length-1;
        outputs += comand.split("-json", -1).length-1;
        flags += loads + outputs + seeds + calls + files + ras + traces;
        if (flags < 2 || flags > 7) {
            printUsage();
        } else {
            if (loads > 1) {
                System.out.println("Flags '-l' and '-L' can not be declared together");
                printUsage();
            }
            if (outputs > 1) {
                System.out.println("Flags '-verbose' and '-table' and '-json' can not be declared together");
                printUsage();
            }
            if (files > 1) {
                System.out.println("Were declared two flags '-f'");
                printUsage();
            }
            if (seeds > 1) {
                System.out.println("Were declared two flags '-s'");
                printUsage();
            }
            if (calls > 1) {
                System.out.println("Were declared two flags '-c'");
                printUsage();
            }
            if (ras > 1) {
                System.out.println("Were declared two flags '-ra'");
                printUsage();
            }
            if (traces > 1) {
                System.out.println("Were declared two flags '-trace'");
                printUsage();
            }

        }
        if(comand.split("-verbose").length-1 == 1){
            Main.verbose = true;
        } else {
            if(comand.split("-table").length-1 == 1){
                Main.table = true;
            } else {
                if(comand.split("-json").length-1 == 1){
                    Main.json = true;
                }
            }
        }
        if(comand.split("-trace").length-1 == 1){
            Main.trace = true;
        }
        try {
            Main.seed = Integer.parseInt(comand.split("-s ")[1].split(" ")[0]);
            if(Main.seed < 1 || Main.seed > 25) {
                System.out.println("The seed should be between 1 and 25");
                printUsage();
            }
            Main.simConfigFile = comand.split("-f ")[1].split(" ")[0];
        } catch (Exception e) {
            printUsage();
        }
        try {
            if(comand.split("-ra ", -1).length-1 == 1) {
                Main.raClass = comand.split("-ra ")[1].split(" ")[0];
            }
            if(comand.split("-c ", -1).length-1 == 1) {
                Main.calls = Integer.parseInt(comand.split("-c ")[1].split(" ")[0]);
            }
            if(comand.split("-l ", -1).length-1 == 1) {
                Main.minload = Double.parseDouble(comand.split("-l ")[1].split(" ")[0]);
                Main.maxload = Main.minload;
            }
            if(comand.split("-L ", -1).length-1 == 1) {
                Main.minload = Double.parseDouble(comand.split("-L ")[1].split(" ")[0]);
                Main.maxload = Double.parseDouble(comand.split("-L ")[1].split(" ")[1]);
                Main.step = Double.parseDouble(comand.split("-L ")[1].split(" ")[2]);
            }
        } catch (Exception e) {
            printUsage();
        }
    }
}
