package com.clover.sandbox.matrixman;

import io.airlift.airline.*;
import io.airlift.airline.Cli.*;

import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;


enum TestSource {File, Directory, String;}

public class Pdjn {

    public static void main(String[] args) {

        CliBuilder<Runnable> builder = Cli.<Runnable>builder("pdjn")
                .withDescription("A java test runner for the Pidgin framework")
                .withDefaultCommand(Go.class)
                .withCommands(Help.class, Go.class, Device.class);

        Cli<Runnable> pdjnParser = builder.build();

        pdjnParser.parse(args).run();

    }

    private static String MakeUrl(String ip, short port, boolean secure) {
        // build "ws://1.2.3.4:12345/remote_pay" or similar
        StringBuilder sb = new StringBuilder();
        if (secure)
            sb.append("ws://");
        else
            sb.append("wss://");
        sb.append(ip);
        sb.append(":");
        sb.append(port);
        sb.append("remote_pay");
        return sb.toString();

    }


    interface Subcommand {
        void check();

        void go();
    }

    public class CommandBase implements Runnable, Subcommand {

        // gets run every time
        final public void run() {
            globalCheck();
            check();
            go();
        }

        @Option(type = OptionType.GLOBAL, name = {"-n", "--no-files"}, description = "test")
        boolean quiet;

        @Option(type = OptionType.GLOBAL, name = {"-q", "--quiet"})
        boolean noFile;

        // base command stuff
        private void globalCheck() {
            if (noFile) {
                System.out.println("No files will be written.");
            }

            if (quiet & noFile) {
                System.out.println("Both quiet and noFile are set.  Why even bother?");
                System.exit(1);
            }
        }

        public void check() { /* subcommands may override */ }

        public void go() {
            System.out.println("Default command is specified, this should never be hit");
        }
    }


    @Command(name = "go", description = "Ignore working directory, just run based on parameters")
    public class Go extends CommandBase {

        public Go() {
        }

        @Option(name = {"-j", "--json-output"}, description = "Instead of writing test results to a file, write them to STDOUT")
        boolean writeJson;

        boolean writeResultsFile(boolean val) {
            writeJson = !val;
            return writeResultsFile();
        }

        boolean writeResultsFile() {
            return !writeJson;
        }

        @Option(name = {"-c", "--config-file"}, description = "Read this file to find devices", required = true)
        String configFile;

        @Option(name = {"-t", "--test-files"}, description = "Specify test file(s) to run (e.g. foo.json,~/bar/baz.json)", required = true)
        String testFileList;

        List<String> testFiles;

        public void check() {
            StringUtils.split(testFileList, ",");

//            int testSources = 0;
//            if (fileGlob.isEmpty())
//                testSources++;
//            if (nameRegex.isEmpty())
//                testSources++;
//            if (tagFilter.isEmpty())
//                testSources++;
//
//            if (testSources != 1) {
//                System.out.println("Please specify at least one test filter");
//                System.exit(1);
//            }
        }

        public void go() {
            System.out.println("GO ");
//            System.out.println("GO " + configFile + StringUtils.join(testFiles, " "));
        }
    }

    @Command(name = "device", description = "Add a device to the run context")
    class Device extends CommandBase {
        @Arguments(description = "The name of the device to add" )

        @Option(name = {"-i", "--ip"}, description = "Specify the device IP")
        String ip;

        @Option(name = {"-p", "--port"}, description = "Specify the device port")
        short port = 0;

        @Option(name = {"-s", "--secure"}, description = "Use wss:// instead of ws://")
        boolean secure;

        public void check() {
            // if IP and Port were not provided, get them
            Scanner s = new Scanner(System.in);

            if (ip == null) {
                System.out.printf("Device IP: ");
                ip = s.nextLine();
            }

            if (port == 0) {
                System.out.printf("Device Port: ");
                port = s.nextShort();
            }
        }

        public void go() {
            System.out.println("Adding Device at" + MakeUrl(ip, port, secure));
        }
    }
}
