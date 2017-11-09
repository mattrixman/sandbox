package com.clover.sandbox.matrixman;

import io.airlift.airline.*;
import io.airlift.airline.Cli.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;


enum TestSource {File, Directory, String;}

public class PidJ {

    public static void main(String[] args) {

        CliBuilder<Runnable> builder = Cli.<Runnable>builder("pidj")
                .withDescription("A java test runner for the Pidgin framework")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, Go.class);

        builder.withGroup("device")
                .withDescription("Manage devices in the test context")
                .withDefaultCommand(DeviceList.class)
                .withCommands(DeviceAdd.class, DeviceList.class, DeviceRemove.class);

        builder.withGroup("test")
                .withDescription("Manage devices in the test context")
                .withDefaultCommand(DeviceList.class)
                .withCommands(DeviceAdd.class, DeviceList.class, DeviceRemove.class);

        Cli<Runnable> pidjParser = builder.build();

        pidjParser.parse(args).run();

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
        sb.append("/remote_pay");
        return sb.toString();

    }


    interface Subcommand {
        void check();

        void go();
    }

    public static class CommandBase implements Runnable, Subcommand {

        // gets run every time
        final public void run() {
            globalCheck();
            check();
            go();
        }

        @Option(type = OptionType.GLOBAL, name = {"-n", "--no-files"}, description = "Don't write results to files")
        boolean quiet;

        @Option(type = OptionType.GLOBAL, name = {"-q", "--quiet"}, description = "Don't write to STDOUT")
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
    public static class Go extends CommandBase {

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
        String testFiles;

        ArrayList<File> tests;
        File config;

        public void check() {

            tests = new ArrayList<>();

            // get test files, fail if they don't exist
            Arrays.asList(StringUtils.split(testFiles, ","))
                    .forEach(fileString -> {
                        File f = FileManip.GetFileOrNull(fileString);
                        if (f == null) {
                            System.out.printf("%s does not exist\n", fileString);
                            System.exit(1);
                        } else {
                            tests.add(f);
                        }
                    });

            // get config file, fail if it doesn't exist
            config = FileManip.GetFileOrNull(configFile);
            if (config == null) {
                System.out.printf("%s does not exist\n", configFile);
                System.exit(1);
            }
        }

        public void go() {
            System.out.print("GO ");
            System.out.printf("%s ", config.getAbsolutePath().toString());
            tests.forEach(x -> System.out.printf(" %s", x.getAbsolutePath().toString()));
            System.out.println("");
        }
    }

    @Command(name = "add", description = "Add a device to the test context")
    public static class DeviceAdd extends CommandBase {

        @Arguments(description = "the name of the device", required = true)
        List<String> names;
        String name;

        @Option(name = {"-i", "--ip"}, description = "Specify the device IP")
        String ip;

        @Option(name = {"-p", "--port"}, description = "Specify the device port")
        short port = 0;

        @Option(name = {"-s", "--secure"}, description = "Use wss:// instead of ws://")
        boolean secure;

        public void check() {

            // add only one device at a time
            if (names.size() > 1){
                System.out.println("Please specify only one device name");
                System.exit(1);
            }

            name = names.get(0);
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
            System.out.printf("Adding %s at %s\n", name, MakeUrl(ip, port, secure));
        }
    }

    @Command(name = "ls", description = "Show the devices in the test context")
    public static class DeviceList extends CommandBase {
        public void go() {
            System.out.println("Listing devices");
        }
    }

    @Command(name = "rm", description = "Remove a device from the test context")
    public static class DeviceRemove extends CommandBase {

        @Arguments(description = "the name of the device", required=true)
        List<String> names;

        public void check()
        {
            names.forEach(name ->
            {
                System.out.printf("TODO: check that %s is a device\n", name);
            });
        }

        public void go() {
            names.forEach(name -> {
                System.out.printf("Removing Device: %s\n", name);
            });
        }
    }
}
