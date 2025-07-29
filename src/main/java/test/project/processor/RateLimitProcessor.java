package test.project.processor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class RateLimitProcessor {
    
    Map<String, Map<String, Queue<Long>>> connections = new HashMap<>();
    long TIME_WINDOW_IN_MILLIS = 100; //time gap is 1s
    int MAX_CONNECTIONS = 10;

    public RateLimitProcessor() {}

    public RateLimitProcessor(int maxConnections, long timeWindowInMillis) {
        MAX_CONNECTIONS = maxConnections;
        TIME_WINDOW_IN_MILLIS = timeWindowInMillis;
    }

    public boolean allow(String account, String requestUrl) {
        
        long now = System.currentTimeMillis();

        if (!connections.containsKey(account)) {
            Queue<Long> newRequest = new LinkedList<Long>();
            newRequest.add(now);
            
            Map<String, Queue<Long>> newConnections = new HashMap<String, Queue<Long>>();
            newConnections.put(requestUrl, newRequest);

            connections.put(account, newConnections);
            return true;
        }

        Map<String, Queue<Long>> allRequests = connections.get(account);

        if (!allRequests.containsKey(requestUrl)) {
            Queue<Long> newRequest = new LinkedList<Long>();
            newRequest.add(now);

            allRequests.put(requestUrl, newRequest);
            return true;
        }

        Queue<Long> requestsPerUrl = allRequests.get(requestUrl);

        while(!requestsPerUrl.isEmpty() && now - requestsPerUrl.peek() > TIME_WINDOW_IN_MILLIS) {
            requestsPerUrl.poll();
        }

        if (requestsPerUrl.size() >= MAX_CONNECTIONS) {
            return false;
        }

        requestsPerUrl.add(now);
        return true;
    }
}
