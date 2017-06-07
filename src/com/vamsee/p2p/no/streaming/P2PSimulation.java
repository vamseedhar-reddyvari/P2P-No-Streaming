package com.vamsee.p2p.no.streaming;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.pow;


/**
 * Created by Vamsee on 6/7/17.
 * This class is the main file for simulation.
 */
public class P2PSimulation {
    private double lambda_p; // Rate of contacting peer
    private double lambda; // Rate of arrival
    private double Us; // Rate of seed
    private int NoEvents; // Amount of time to run the simulaiton
    private int NoPeers; // Total peers in the system
    private ArrayList<Peer> listPeers; // List of all current peers
    private Random rand; // Object for generating random numbers
    private int[] marginalPktDistributionCount; // pdf of marginal
    private int[] bufferDistributionCount; // pdf of joint state
    private int NoPackets;


    //public String folderPath = "/Users/Vamsee/Work/GitHub/Projects/P2P-No-Streaming/output/";
    public P2PSimulation(double Us, double lambda, double lambda_p){

        // System Variables
        this.NoPackets = Peer.NoPackets;
        this.lambda_p = lambda_p;
        this.Us = Us;
        this.lambda = lambda;

        //Initialize Peers
        this.NoPeers = 5; //Lets start with 5 peers
        listPeers = new ArrayList<Peer>();
        for(int i=0;i<this.NoPeers;i++){
            Peer newPeer = new Peer();
            listPeers.add(newPeer);
        }

        // Initialize Distribution
        marginalPktDistributionCount = new int[NoPackets];
        bufferDistributionCount =  new int[(int)pow(2,NoPackets)];
        Arrays.fill(marginalPktDistributionCount,0); // As all peers begin with 0 packets margianl distribution will be 0
        Arrays.fill(bufferDistributionCount,0); // As all peers begin with 0 packets distribution will be 0
        bufferDistributionCount[0] = NoPeers; // All peers have 0  packets

        // Auxilary Objects
        rand = new Random(); //initialize random
    }

    public int Run() throws IOException{
        // Run the simulation
        // Generate sojourn times for each poisson random variable and execute the event with least sojourn time


        NoEvents = 1000000;
        PrintWriter writer = new PrintWriter("group-suppression-US-2-lambda-2.5.txt", "UTF-8");
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
                //update Distribution
                bufferDistributionCount[0] += 1;
            }
            else if(peerContactTime<= peerArrivalTime && peerContactTime <= seedContactTime){
                // Peers Exchange packets
                Peer srcPeer = listPeers.get(rand.nextInt(NoPeers));
                int dstPeerIdx = rand.nextInt(NoPeers);
                Peer dstPeer = listPeers.get(dstPeerIdx);
                // transfer random useful packet from src to dst
                int pktIdx = transferPackets(dstPeer.Buffer, srcPeer.Buffer);
                // Check if dst Peer has recieved all the pacckets. If yes then remove the peer from system
                boolean removed = removePeer(dstPeerIdx, pktIdx);
                if (removed){ writer.println(NoPeers); }
                if (NoPeers==0) {break;}
            }
            else if ( seedContactTime<=peerArrivalTime && seedContactTime<=peerContactTime){
                // Seed gives a packet
                int dstPeerIdx = rand.nextInt(NoPeers);
                Peer dstPeer = listPeers.get( dstPeerIdx );
                // Transfer a random useful packet from seed to dst
                int pktIdx = transferPackets(dstPeer.Buffer, Peer.FullBuffer);

                // Check if dst Peer has recieved all the pacckets. If yes then remove the peer from system
                boolean removed = removePeer(dstPeerIdx, pktIdx);
                if (removed){ writer.println(NoPeers); }
                if (NoPeers==0) {break;}
            }

        }
        writer.close();

        return 0;
    }
    private boolean removePeer(int peerIdx, int addedPktIdx){
        Peer dstPeer = listPeers.get(peerIdx);
        if(Arrays.equals(dstPeer.Buffer, Peer.FullBuffer)){
            // remove
            listPeers.remove(peerIdx);
            NoPeers = NoPeers -1;
            // update distributions
            for(int i=0;i < NoPackets; i++){
                marginalPktDistributionCount[i] = marginalPktDistributionCount[i] -1;
            }
            bufferDistributionCount[toDecimal(Peer.FullBuffer)] = bufferDistributionCount[toDecimal(Peer.FullBuffer)] -1;
            return true;
        }

        return false;

    }
    private double expRand(double lambda) {
        // Return an exponential random varible with parameter lambda
        return  Math.log(1-rand.nextDouble())/(-lambda);
    }
    private int transferPackets(int[] dst, int[] src){
        /*
        - Transfers a useful packet from src to destination
        - Returns index of the packet transmitted
        - If no packet is transmitted returns -1
        - Also update marginal and buffer distributions
        */
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
            // update distribution
            marginalPktDistributionCount[pktIdx] = (marginalPktDistributionCount[pktIdx] +1);
            int dstDecimalValue = toDecimal(dst);
            bufferDistributionCount[dstDecimalValue] = (bufferDistributionCount[dstDecimalValue] +1 );
            dstDecimalValue = dstDecimalValue - (int)pow(2,pktIdx);
            bufferDistributionCount[dstDecimalValue] = (bufferDistributionCount[dstDecimalValue] -1 );
            return pktIdx;
        }
        return -1;

    }
    private int toDecimal(int[] buffer){
        int decValue = 0;
        for(int i=0;i<buffer.length;i++){
            decValue = decValue + buffer[i]*(int)pow(2,i);
        }
        return decValue;
    }
}
