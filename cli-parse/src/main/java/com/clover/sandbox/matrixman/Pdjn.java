package com.clover.sandbox.matrixman;

import io.airlift.airline.*;
import io.airlift.airline.Cli.*;

import java.util.List;
import java.util.Scanner;

public class Pdjn {
    public static void main(String[] args) {

        CliBuilder<Runnable> builder = Cli.<Runnable>builder("pdjn")
                .withDescription("A java test runner for the Pidgin framework")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, Init.class);

        Cli<Runnable> pdjnParser = builder.build();

        pdjnParser.parse(args).run();

    }

    public abstract class TwoStageCommand {
        void preCommand(){ /* if not overridden, do nothing */ }

        abstract void command();
    }

    public class PdjnCommand extends TwoStageCommand implements Runnable {

        @Option(type = OptionType.GLOBAL, name = {"-n", "--no-files"}, description = "test")
        boolean quiet;

        @Option(type = OptionType.GLOBAL, name = {"-q", "--quiet"})
        boolean noFile;

        public void run() {

            if (noFile) {
                System.out.println("No files will be written.");
            }

            if (quiet & noFile) {
                System.out.println("Both quiet and noFile are set.  Why even bother?");
                System.exit(1);
            }

            preCommand();
            command();
        }

        public void command() {
            System.out.println("Default command is specified, this should never be hit");
        }


        }
    }

    @Command(name = "init", description = "Make the current directory a test context")
    public class Init extends PdjnCommand
    {
        @Option(name = {"-i", "--ip"}, description = "Specify the device IP")
        String ip;

        @Option(name = {"-p", "--port"}, description = "Specify the device port")
        short port = 0;

        @Option(name = {"-s", "--secure"}, description = "Use wss:// instead of ws://")
        boolean secure;

        @Option(name = {"-f", "--force"}, description = "Overwrite existing context")
        boolean force;


        String url;

        protected void finalizeCommand(){

            Scanner s = new Scanner(System.in);

            if (ip == null) {
                System.out.printf("Device IP: ");
                ip = s.nextLine();

            if (port == 0) {
                System.out.printf("Device Port: ");
                port = s.nextShort();
            }

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
            url = sb.toString();



        }


    }
}
