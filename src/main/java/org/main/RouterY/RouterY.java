package org.main.RouterY;

import org.main.util.PortMap;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

import static org.main.util.APSP.floydWarshall;
import static org.main.util.Dijkstra.dijkstra;

public class RouterY {
    public static char id = 'y';
    public static int inf = 10000;
    public static int port = PortMap.map.get(id); // 8002
    public static int broadcastNo = 1;
    public static int broadcastThreshold = 25; // After 5 broadcast, cost will change

    public static int[][] cost = {
            //        u    v    w    x    y    z
            /* u */ { 0 , inf, inf, inf, inf, inf},
            /* v */ {inf,  0 , inf, inf, inf, inf},
            /* w */ {inf, inf,  0 , inf,  1 , inf},
            /* x */ {inf, inf, inf,  0 ,  1 , inf},
            /* y */ {inf, inf,  1 ,  1 ,  0 ,  2 },
            /* z */ {inf, inf, inf, inf,  2 , inf}
    };

    static char ascii(char c, int i){
        return (char)((int)c + i);
    }
    public static void broadcast(DatagramSocket socket) throws IOException {
        int idIdx = (id - (int)'u');
        for(int i=0; i<cost[idIdx].length; i++){ // NEIGHBORS : Cost row of this router

            for(int j=0; j<cost[idIdx].length; j++){ // Cost row of this router

                // Packet // Format [u v cost]
                String message = id + " " + ascii('u', j) + " " + cost[idIdx][j];
                byte[] buffer = message.getBytes();

                // Sending Packet to a neighbor
                int port = PortMap.map.get(ascii('u', i));
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), port);
                socket.send(packet);
            }


            // Ending Signal Packet // Format [- - -]
            String message = "- - -";
            byte[] buffer = message.getBytes();
            // Sending Ending Packet to a neighbor
            int port = PortMap.map.get(ascii('u', i));
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), port);
            socket.send(packet);
        }
    }
    static void changeCost(){
        Random random = new Random();
        System.out.println("\nNetwork Changing\n");
        int i = (int)id - (int)'u';
        for (int j = 0; j < cost[i].length; j++) {
            if(cost[i][j] != inf){
                cost[i][j] = random.nextInt(10) + 1;
            }
        }
    }
    static void printCost() {
        System.out.println("     u    v    w    x    y    z");
        System.out.println("---------------------------------");
        for (int i = 0; i < cost.length; i++) {
            System.out.print((char)('u'+i) + " | ");
            for (int j = 0; j < cost[i].length; j++) {
                if(cost[i][j] == inf) System.out.print("inf, ");
                else System.out.print(" " + cost[i][j] + " , ");
            }
            System.out.println();
        }
    }
    static void printMyCost() {
            int i = (int)id - (int)'u';
            for (int j = 0; j < cost[i].length; j++) {
                if(cost[i][j] == inf) System.out.print("inf, ");
                else System.out.print(" " + cost[i][j] + " , ");
            }
            System.out.println();
    }
    public static void main(String[] args) throws Exception {

        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage initialMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long initialUsedMemory = initialMemoryUsage.getUsed();

        DatagramSocket socket = new DatagramSocket(port); // UDP SERVER
        System.out.print("Initial Cost: "); printMyCost();


        Boolean newBroadcast = true, improved = false;
        Character broadCastSrc = null;

        // Listening for any broadcast
        while(true){
            // Receive a response from the server
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String response = new String(packet.getData(), 0, packet.getLength());

            Character src = response.split(" ")[0].charAt(0);
            Character dst = response.split(" ")[1].charAt(0);

            if(src != '-' && dst != '-'){
                newBroadcast = false;
                int linkCost = Integer.parseInt(response.split(" ")[2]);
                System.out.println(src + " " + dst + " " + linkCost);

                // Compare with own information to update cost
                int idIdx = (id - (int)'u');
                for(int i=0; i < cost[idIdx].length; i++){



                    if(
                            linkCost < cost[(int)src - (int)'u'][(int)dst - (int)'u'] ||
                            linkCost < cost[(int)dst - (int)'u'][(int)src - (int)'u']
                    ){
                        cost[(int)src - (int)'u'][(int)dst - (int)'u'] = linkCost; cost[(int)dst - (int)'u'][(int)src - (int)'u'] = linkCost;
                        improved = true;
                    }
                }
                broadCastSrc = src;

            }

            // Packet Broadcast Reception Ended
            else {

//                printCost();
                System.out.println("Broadcast Reception Ended from: " + broadCastSrc);

                System.out.println("\nCalculating Single Source Shortest path");
                dijkstra(cost, id-'u');
                floydWarshall(cost);
                // Broadcast updated Information
                if (improved) {
                    System.out.println("IMPROVED => Broadcasting");
                    broadcast(socket);
                    System.out.println("Broadcast Ended\n");
                }

                // Dynamically Adopt Network
                if(broadcastNo % broadcastThreshold == 0){
                    changeCost();
                    System.out.println("Broadcasting New Cost");
                    broadcast(socket);
                }

                MemoryUsage finalMemoryUsage = memoryMXBean.getHeapMemoryUsage();
                long finalUsedMemory = finalMemoryUsage.getUsed();
                long memoryUsageDelta = finalUsedMemory - initialUsedMemory;
                System.out.println("Memory usage: " + memoryUsageDelta/1024 + " kB");
                System.out.println("------------------" + broadcastNo + "------------------------");
                newBroadcast = true; improved = false; broadcastNo += 1;

                if(broadcastNo == 15) break;
            }
        }

    }
}
