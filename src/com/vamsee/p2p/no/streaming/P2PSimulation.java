package com.vamsee.p2p.no.streaming;

import com.sun.org.apache.bcel.internal.generic.NOP;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/**
 * Created by Vamsee on 6/7/17.
 * This class is the main file for simulation.
 */
public class P2PSimulation {
    public double lambda_p; // Rate of contacting peer
    public double lambda; // Rate of arrival
    public double Us; // Rate of seed
    public int NoEvents; // Amount of time to run the simulaiton
    public int NoPeers; // Total peers in the system
    public ArrayList<Peer> listPeers; // List of all current peers
    public Random rand;
    //public String folderPath = "/Users/Vamsee/Work/GitHub/Projects/P2P-No-Streaming/output/";
    public P2PSimulation(double Us, double lambda, double lambda_p){
        this.lambda_p = lambda_p;
        this.Us = Us;
        this.lambda = lambda;
        this.NoPeers = 5; //Lets start with 5 peers
        listPeers = new ArrayList<Peer>();
        for(int i=0;i<this.NoPeers;i++){
            Peer newPeer = new Peer();
            listPeers.add(newPeer);
        }
        rand = new Random(); //initialize random
    }
    public int Run() throws IOException{
       // Generate soujourn times for each poisson random variable


        NoEvents = 1000000;
        // Run the simulation
        PrintWriter writer = new PrintWriter("no-peers.txt", "UTF-8");
        for(int i=0; i< NoEvents;i++){
            double peerContactTime = expRand(NoPeers*lambda_p);
            double seedContactTime= expRand(Us);
            double peerArrivalTime = expRand(lambda);

            if ( peerArrivalTime <= peerContactTime & peerArrivalTime <= seedContactTime){
                // Arrival Event
                Peer newPeer = new Peer();
                listPeers.add(newPeer);
                NoPeers = NoPeers +1;
                writer.println(NoPeers);
            }
            else if(peerContactTime<= peerArrivalTime && peerContactTime <= seedContactTime){
                // Peers Exchange packets
                Peer srcPeer = listPeers.get(rand.nextInt(NoPeers));
                int dstPeerIdx = rand.nextInt(NoPeers);
                Peer dstPeer = listPeers.get(dstPeerIdx);
                // transfer random useful packet from src to dst
                int pktIdx = transferPackets(dstPeer.Buffer, srcPeer.Buffer);
                // Check if dst Peer has recieved all the pacckets. If yes then remove the peer from system
                if(Arrays.equals(dstPeer.Buffer, Peer.FullBuffer)){
                    // remove
                    listPeers.remove(dstPeerIdx);
                    NoPeers = NoPeers -1;
                    writer.println(NoPeers);
                    if (NoPeers ==0){
                        break;
                    }
                }
            }
            else if ( seedContactTime<=peerArrivalTime && seedContactTime<=peerContactTime){
                // Seed gives a packet
                int dstPeerIdx = rand.nextInt(NoPeers);
                Peer dstPeer = listPeers.get( dstPeerIdx );
                // Transfer a random useful packet from seed to dst
                int pktIdx = transferPackets(dstPeer.Buffer, Peer.FullBuffer);

                // Check if dst Peer has recieved all the pacckets. If yes then remove the peer from system
                if(Arrays.equals(dstPeer.Buffer, Peer.FullBuffer)){
                    // remove
                    listPeers.remove(dstPeerIdx);
                    NoPeers = NoPeers -1;
                    writer.println(NoPeers);

                    if (NoPeers ==0){
                        break;
                    }
                }

            }

//            System.out.println(NoPeers+", ");
        }
        writer.close();

        return 0;
    }
    private double expRand(double lambda) {
        return  Math.log(1-rand.nextDouble())/(-lambda);
    }
    private int transferPackets(int[] dst, int[] src){
        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i=0; i< dst.length; i++){
            if (src[i] > dst[i]){
                usefulPktIdx.add(i);
            }
        }
        // Select a random packet and transfer
        if (usefulPktIdx.size() >0){
            int randomIdx = rand.nextInt(usefulPktIdx.size());
            int pktIdx = usefulPktIdx.get(randomIdx);
            dst[pktIdx] = 1;
            return pktIdx;
        }
        return -1;

    }
}
