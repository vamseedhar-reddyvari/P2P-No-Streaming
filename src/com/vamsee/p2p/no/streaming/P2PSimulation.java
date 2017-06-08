package com.vamsee.p2p.no.streaming;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    // dictionary of number of packet. Each number of packets maps to a list of all peers (indices in lit of peers) that contain that many number of packets.
    private HashMap<Integer,ArrayList<Peer> > PeerNumberOfPktsMap;


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

        //Initialize Hashmap
        PeerNumberOfPktsMap = new HashMap<Integer,ArrayList<Peer>>();
        for(int i=0; i < NoPackets+1; i++){
            PeerNumberOfPktsMap.put(i,new ArrayList<Peer>());
        }
        for(int j=0;j<NoPeers;j++){
            PeerNumberOfPktsMap.get(0).add(listPeers.get(j)); // All peers doesn't have any packets. So 0 packets will map to entire list
        }
    }

    public int Run() throws IOException{
        // Run the simulation
        // Generate sojourn times for each poisson random variable and execute the event with least sojourn time


        NoEvents = 1000000;
        PrintWriter writer = new PrintWriter("group-suppression-US-2-lambda-4.txt", "UTF-8");
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
                updateHashMap(newPeer,-1);
            }
            else if(peerContactTime<= peerArrivalTime && peerContactTime <= seedContactTime){
                // Peers Exchange packets
                int srcPeerIdx= rand.nextInt(NoPeers);
                Peer srcPeer = listPeers.get(srcPeerIdx);
                int dstPeerIdx = rand.nextInt(NoPeers);
                Peer dstPeer = listPeers.get(dstPeerIdx);
                // transfer random useful packet from src to dst
                int pktIdx = PeerChunkGroupSuppression(dstPeer, srcPeerIdx);
                // Check if dst Peer has recieved all the pacckets. If yes then remove the peer from system
                boolean removed = removePeer(dstPeer, pktIdx);
                if (removed){
                    PeerNumberOfPktsMap.get(NoPackets).remove(dstPeer);
                    writer.println(NoPeers); }
                if (NoPeers==0) {break;}
            }
            else if ( seedContactTime<=peerArrivalTime && seedContactTime<=peerContactTime){
                // int dstPeerIdx = rand.nextInt(NoPeers);
                // Seed gives a packet
                // Select a most deprived peer
                int k=0;
                for(k=0; k <NoPackets+1; k++){
                    if (PeerNumberOfPktsMap.get(k).size() >0){ break;}
                }
                ArrayList<Peer> deprivedList = PeerNumberOfPktsMap.get(k);
                int deprivedListIdx = rand.nextInt(deprivedList.size());
                Peer dstPeer = deprivedList.get(deprivedListIdx);
                // Transfer a random useful packet from seed to dst
                int pktIdx = SeedChunk(dstPeer );

                // Check if dst Peer has recieved all the packets. If yes then remove the peer from system
                boolean removed = removePeer(dstPeer, pktIdx);
                if (removed){
                    PeerNumberOfPktsMap.get(NoPackets).remove( dstPeer);
                    writer.println(NoPeers);
                }
                if (NoPeers==0) {break;}
            }

        }
        writer.close();

        return 0;
    }
    private boolean removePeer(Peer dstPeer, int addedPktIdx){
//        Peer dstPeer = listPeers.get(peerIdx);
        if(Arrays.equals(dstPeer.Buffer, Peer.FullBuffer)){
            // remove
            listPeers.remove(dstPeer);
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

    private int randomPeerChunk(Peer dstPeer, int srcIdx){
        /*
        - Transfers a useful packet from src to destination
        - Returns index of the packet transmitted
        - If no packet is transmitted returns -1
        - Also update marginal and buffer distributions
        */
        int [] dst = dstPeer.Buffer;
        int [] src = listPeers.get(srcIdx).Buffer;

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
            updateDistributions(dst,pktIdx);
            updateHashMap(dstPeer,pktIdx);
            return pktIdx;
        }
        return -1;

    }

    private int PeerChunkGroupSuppression(Peer dstPeer, int srcIdx){
        /*
        - Here group Suppresion is implemented
        - Identify the (decimal) buffer state of largest group
        - If the src is in largest group then check if the dst has more pkts ow abadon
        */
        int [] dst = dstPeer.Buffer;
        int [] src = listPeers.get(srcIdx).Buffer;

        // Determine largestbuffer
        int largestBuffer = 0;
        int largestValue = 0;
        for(int i=0; i< bufferDistributionCount.length;i++){
            int newvalue = bufferDistributionCount[i];
            if (newvalue > largestValue){
                largestValue = newvalue;
                largestBuffer = i;
            }
        }
        if(toDecimal(src) == largestBuffer){
            if (sumPackets(dst) < sumPackets(src) ){
                return -1; // abandoning the transfer to avoid single club
            }
        }


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
            updateDistributions(dst,pktIdx);
            updateHashMap(dstPeer,pktIdx);
            return pktIdx;
        }
        return -1;

    }

    private int SeedChunk(Peer dstPeer){
        /*
        src contains all elements
        */
        int [] dst = dstPeer.Buffer;
        int[] src = Peer.FullBuffer;
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
            //update Distribution
            updateDistributions(dst,pktIdx);
            updateHashMap(dstPeer,pktIdx);
            return pktIdx;
        }
        return -1;

    }
    private void updateHashMap(Peer dstPeer, int pktIdx){
        if(pktIdx==-1){
            // new packet
            PeerNumberOfPktsMap.get(0).add(dstPeer);

        }
        else {
            int[] tempBuffer = dstPeer.Buffer;
            PeerNumberOfPktsMap.get(sumPackets(tempBuffer) - 1).remove(dstPeer);
            tempBuffer[pktIdx] = 1;
            PeerNumberOfPktsMap.get(sumPackets(tempBuffer)).add(dstPeer);
        }


    }

    private int sumPackets(int [] buffer){
        int sum = 0;
        for(int i=0; i<NoPackets; i++){
            sum = sum + buffer[i];
        }
        return sum;
    }
    private void updateDistributions(int[] dst,int pktIdx){
        // update distribution
        marginalPktDistributionCount[pktIdx] = (marginalPktDistributionCount[pktIdx] +1);
        int dstDecimalValue = toDecimal(dst);
        bufferDistributionCount[dstDecimalValue] = (bufferDistributionCount[dstDecimalValue] +1 );
        dstDecimalValue = dstDecimalValue - (int)pow(2,pktIdx);
        bufferDistributionCount[dstDecimalValue] = (bufferDistributionCount[dstDecimalValue] -1 );

    }
    private int toDecimal(int[] buffer){
        int decValue = 0;
        for(int i=0;i<buffer.length;i++){
            decValue = decValue + buffer[i]*(int)pow(2,i);
        }
        return decValue;
    }
}
