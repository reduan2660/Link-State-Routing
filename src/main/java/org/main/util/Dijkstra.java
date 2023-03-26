package org.main.util;

public class Dijkstra {

    private static final int V = 6; // Number of vertices
    // private static int[][] graph = new int[V][V]; // Graph represented as a 2D array

    public static void dijkstra(int[][] graph, int source) {
        int[] dist = new int[V]; // Array to store shortest distances
        boolean[] visited = new boolean[V]; // Array to keep track of visited vertices
        int[] parent = new int[V]; // Array to store parent of each vertex in the shortest path

        // Initialize arrays
        for (int i = 0; i < V; i++) {
            dist[i] = Integer.MAX_VALUE;
            visited[i] = false;
            parent[i] = -1;
        }

        // Distance to source vertex is 0
        dist[source] = 0;

        // Loop through all vertices
        for (int i = 0; i < V - 1; i++) {
            // Find the vertex with minimum distance
            int u = minDistance(dist, visited);
            visited[u] = true;

            // Update distances of adjacent vertices
            for (int v = 0; v < V; v++) {
                if (!visited[v] && graph[u][v] != 0 && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    parent[v] = u;
                }
            }
        }

        // Print shortest distances and paths
        for (int i = 0; i < V; i++) {
            System.out.print("Router " + (char)(i+'u') + ": ");
            if (dist[i] == Integer.MAX_VALUE) {
                System.out.println("No path");
            } else {
                System.out.print(dist[i] + " [");
                printPath(i, parent);
                System.out.println("]");
            }
        }
    }

    private static int minDistance(int[] dist, boolean[] visited) {
        int min = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int i = 0; i < V; i++) {
            if (!visited[i] && dist[i] <= min) {
                min = dist[i];
                minIndex = i;
            }
        }

        return minIndex;
    }

    private static void printPath(int current, int[] parent) {
        if (current == -1) {
            return;
        }

        printPath(parent[current], parent);
        System.out.print(current + " ");
    }
}