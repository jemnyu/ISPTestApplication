/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication2;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.util.Pair;

/**
 *
 * @author Justin
 */
public class JavaApplication2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        int numberOfIterations = 250;
        // TODO code application logic here
        ArrayList<Pair<Double, Double>> results = new ArrayList<>();
        results.add(runTests(numberOfIterations, 1));
        results.add(runTests(numberOfIterations, 2));
        results.add(runTests(numberOfIterations, 3));
        results.add(runTests(numberOfIterations, 4));
        results.add(runTests(numberOfIterations, 5));
        results.add(runTests(numberOfIterations, 6));
        results.add(runTests(numberOfIterations, 7));
        results.add(runTests(numberOfIterations, 8));
        results.add(runTests(numberOfIterations, 9));
        results.add(runTests(numberOfIterations, 10));
        for(int i = 1; i <= results.size(); i++)
        {
            Pair result = results.get(i-1);
            System.out.println("Test " + i + ": Old-" + result.getKey() + ";New-" + result.getValue());
        }
        
    }
    
    private static Pair<Double, Double> runTests(int numberOfIterations, int numberOfConcurrentCalls) throws Exception
    {
        System.out.println("TESTS: Iterations: " + numberOfIterations + "; Calls: " + numberOfConcurrentCalls);
        Double oldTime = oldTest(numberOfIterations, numberOfConcurrentCalls);
        Double newTime = newTest(numberOfIterations, numberOfConcurrentCalls);
        return new Pair<Double, Double>(oldTime, newTime);
    }
    
    private static double oldTest(int numberOfIterations, int numberOfConcurrentCalls) throws Exception
    {
        ArrayList<Long> times = new ArrayList<>();
        for(int i = 0; i < numberOfIterations; i++)
        {
            //initially request the auth token
            long startTime = System.nanoTime();
            HttpURLConnection asd = (HttpURLConnection) new URL("http://localhost:8080/old/authenticate?password=Password").openConnection();
            asd.setRequestMethod("POST");
            String authToken;
            try(InputStream response = asd.getInputStream())
            {
                Scanner scanner = new Scanner(response);
                authToken = scanner.useDelimiter("\\A").next();
                System.out.println("Auth: " + authToken);
            }
            //for each concurrent call, use the auth token to request user
            for(int j = 0; j < numberOfConcurrentCalls; j++)
            {
                HttpURLConnection second = (HttpURLConnection) new URL("http://localhost:8080/old/user?authenticationToken=" + authToken).openConnection();
                second.setRequestMethod("POST");
                try(InputStream response = second.getInputStream())
                {
                    Scanner scanner = new Scanner(response);
                    String output = scanner.useDelimiter("\\A").next();
                    System.out.println("Output: " + output);
                }
            }
            //log the total time
            long endTime = System.nanoTime();
            long totalTime = (endTime - startTime);
            times.add(totalTime);
        }
        StringBuilder str = new StringBuilder();
        for(Long time : times)
        {
            str.append(time).append(",");
        }
        System.out.println(str.toString());
        return times.stream().mapToLong(x -> x).average().orElse(-1)/1000000;
    }
    
    private static double newTest(int numberOfIterations, int numberOfConcurrentCalls) throws Exception
    {
        ArrayList<Long> times = new ArrayList<>();
        for(int i = 0; i < numberOfIterations; i++)
        {
            long startTime = System.nanoTime();
            String authToken = null;
            for(int j = 0; j < numberOfConcurrentCalls; j++)
            {
                String urlString;
                if(authToken == null) urlString = "http://localhost:8080/new/user?password=NewPassword";
                else urlString = "http://localhost:8080/new/user?authenticationToken=" + authToken;
                HttpURLConnection asd = (HttpURLConnection) new URL(urlString).openConnection();
                asd.setRequestMethod("POST");
                try(InputStream response = asd.getInputStream())
                {
                    Scanner scanner = new Scanner(response);
                    //on the first call, get both the user and the auth token
                    if(j == 0)
                    {
                        String fullOutput = scanner.useDelimiter("\\A").next();
                        String output = fullOutput.split(";")[0];
                        authToken = fullOutput.split(";")[1];
                        System.out.println("Auth Output: " + output);
                        System.out.println("Auth: " + authToken);
                    }
                    else {
                        String output = scanner.useDelimiter("\\A").next();
                        System.out.println("Output: " + output);
                    }
                }
            }
            long endTime = System.nanoTime();
            long totalTime = (endTime - startTime);
            times.add(totalTime);
        }
        StringBuilder str = new StringBuilder();
        for(Long time : times)
        {
            str.append(time).append(",");
        }
        System.out.println(str.toString());
        return (times.stream().mapToLong(x -> x).average().orElse(-1)/1000000);
    }
}
