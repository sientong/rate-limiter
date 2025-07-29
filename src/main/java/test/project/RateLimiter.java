package test.project;

import java.util.Scanner;

import test.project.processor.RateLimitProcessor;

public class RateLimiter 
{
    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\tThis is a rate limiting program. You need to insert using following commands: ");
        System.out.println("\t1. init <time_window_in_millis> <max_request>: \n\t\tInitialize the variable, otherwise it is set to default: TIME_WINDOW_IN_MILLIS = 100 and MAX_CONNECTION = 10");
        System.out.println("\t2. request <account> <request_url>: \n\t\tConnection performed by individual account to single request url");

        boolean exit = false;

        do{

            String input = scanner.nextLine().trim();
            String[] arguments = input.split(" ");

            String command = arguments[0];
            RateLimitProcessor limiter = new RateLimitProcessor();

            switch (command) {
                case "init":
                    if (arguments.length == 3) {
                        try {
                            int timeWindowInMillis = Integer.parseInt(arguments[1]);
                            int maxConnections = Integer.parseInt(arguments[2]);
                            System.out.println("\tInitializing..");
                            limiter = new RateLimitProcessor(maxConnections, timeWindowInMillis);
                        }
                        catch(Exception e) {}
                    }
                    break;
                case "request":
                    if (arguments.length == 3) {
                        try {
                            String account = arguments[1];
                            String requestUrl = arguments[2];
                            System.out.println("\tRequesting..");
                            limiter.allow(account, requestUrl);
                        }
                        catch(Exception e) {}
                    }
                default:
                    break;
            
            }
            System.out.println("\tDo you want to exit?");
            exit = scanner.nextLine().trim().equalsIgnoreCase("yes") || scanner.nextLine().trim().equalsIgnoreCase("y");

        } while(exit);
        
    }
}
