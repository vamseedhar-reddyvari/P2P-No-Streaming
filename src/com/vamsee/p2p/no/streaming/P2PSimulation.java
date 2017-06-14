package com.vamsee.p2p.no.streaming;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;


/**
 * Created by Vamsee on 6/7/17.
 * This class is the main file for simulation.
 */
public class P2PSimulation {

    // System Variables
    private double lambda_p; // Rate of contacting peer
    private double lambda; // Rate of arrival
    private double Us; // Rate of seed
    private int Ticks; // Amount of time to run the simulaiton
    private int NumberOfPeers; // Total peers in the system
    private ArrayList<Peer> ListPeers; // List of all current peers
    private int NumberOfPieces; // Buffer length
    private boolean RANDOM;
    private boolean RAREST;
    private boolean GROUPSUPP;
    private boolean DECLINE_POPULAR;
    private boolean CHAIN_POLICY;

    PrintWriter writer;
    PrintWriter writer_distribution;

    // Meta Variables
    /* - At any time sum of bufferDistributionCount should be equal to Number of Peers.
    - Also sum of disctionary values should also equal to Number of Peers. */
    private int[] marginalPktDistributionCount; // 1xm array of number of Peers that contain a piece
    private int[] bufferDistributionCount; // 1x2^m array of number of peers that contain same pieces
    private HashMap<Integer,ArrayList<Peer> > PeerNumberOfPktsMap; // 1xm+1 Dictionary which maps the number of packets \
                                                        // to a list of all peers that contain that many number of packets.
    private ArrayList<ArrayList<Peer> > PeerNumberOfPktsArray; // 1xm+1 Dictionary which maps the number of packets \

    // Utility Classes
    private Random rand; // Object for generating random numbers

    //Constructor
    public P2PSimulation(double Us, double lambda, double lambda_p) throws IOException{

        // System Variables
        this.NumberOfPieces = Peer.NumberOfPieces;
        this.lambda_p = lambda_p;
        this.Us = Us;
        this.lambda = lambda;
        this.RANDOM = false;
        this.RAREST = false;
        this.GROUPSUPP = false;
        this.DECLINE_POPULAR = false;
        this.CHAIN_POLICY = false;

        //Initialize Peers
        this.NumberOfPeers = 5; //Lets start with 5 peers
        ListPeers = new ArrayList<Peer>();
        for(int i = 0; i<this.NumberOfPeers; i++){
            Peer newPeer = new Peer();
            ListPeers.add(newPeer);
        }

        // Utility Objects
        rand = new Random(); //initialize random
        writer = new PrintWriter("Rarest-Us-2-lambda-4.txt", "UTF-8");
        writer_distribution = new PrintWriter("Rarest-Us-2-lambda-4-distribution.txt", "UTF-8");

        // Meta-Variable initialization
        marginalPktDistributionCount = new int[NumberOfPieces];
        bufferDistributionCount =  new int[(int)pow(2, NumberOfPieces)];
        Arrays.fill(marginalPktDistributionCount,0); // As all peers begin with 0 packets margianl distribution will be 0
        Arrays.fill(bufferDistributionCount,0); // As all peers begin with 0 packets distribution will be 0
        bufferDistributionCount[0] = NumberOfPeers; // All peers have 0  packets
        //Initialize Hashmap
        PeerNumberOfPktsMap = new HashMap<Integer,ArrayList<Peer>>();
        for(int i = 0; i < NumberOfPieces +1; i++){
            PeerNumberOfPktsMap.put(i,new ArrayList<Peer>());
        }
        for(int j = 0; j< NumberOfPeers; j++){
            PeerNumberOfPktsMap.get(0).add(ListPeers.get(j)); // All peers doesn't have any packets. So 0 packets will map to entire list
        }


        PeerNumberOfPktsArray = new ArrayList<ArrayList<Peer>>();
        for(int i = 0; i < NumberOfPieces +1; i++) {
            PeerNumberOfPktsArray.add(new ArrayList<Peer>());
        }
        for(int j = 0; j< NumberOfPeers; j++){
            PeerNumberOfPktsArray.get(0).add(ListPeers.get(j)); // All peers doesn't have any packets. So 0 packets will map to entire list
        }

        // Sanity Check
        assertMetaVariables();

    }

    private boolean assertMetaVariables(){
        int sum = 0;
        for(int count:bufferDistributionCount){ sum = sum + count; }

        if (sum != NumberOfPeers) {
            System.out.println("Assert Failed Buffer Distribution");
            return false;
        }

        sum = 0;
        for(ArrayList value: PeerNumberOfPktsMap.values()){
            sum = sum + value.size();
        }
        if (sum != NumberOfPeers) {
            System.out.println("Assert failed for HashMap");
            return false;
        }

        return true;

    }

    public int Run() throws IOException{
        // Run the simulation
        // Generate sojourn times for each poisson random variable and execute the event with least sojourn time


        Ticks = 2*(int) pow(10,5);
        for(int t = 0; t< Ticks; t++){
            RANDOM = true;
//            CHAIN_POLICY = true;
//            DECLINE_POPULAR = true;
            if (t> pow(10,5) ){
                RANDOM = false;
                GROUPSUPP = false;
                RAREST = false;
                DECLINE_POPULAR = true;
                CHAIN_POLICY = false;
            }
            double [] peersTicks = new double[NumberOfPeers];
            double minTick = 100;
            int minTickIdx = 0;
            for(int i=0;i<NumberOfPeers;i++){
                double newTick = expRand(lambda_p);
                if (minTick > newTick){
                    minTick = newTick;
                    minTickIdx = i;
                }
            }

            double peerContactTime = minTick;
            double seedContactTime= expRand(Us);
            double peerArrivalTime = expRand(lambda);

            // Arrival Event
            if ( peerArrivalTime <= peerContactTime & peerArrivalTime <= seedContactTime){
                arrivalEvent();
            }
            else if(peerContactTime<= peerArrivalTime && peerContactTime <= seedContactTime){
                peer2PeerEvent(minTickIdx);
                if (NumberOfPeers ==0) {break;}
            }
            else if ( seedContactTime<=peerArrivalTime && seedContactTime<=peerContactTime){
                seedTransferEvent();
                if (NumberOfPeers ==0) {break;}
            }
        }
        writer.close();
        writer_distribution.close();

        return 0;
    }


    /* Events */
    private void peer2PeerEvent(int srcPeerIdx){
        // Peers Exchange packets
        Peer srcPeer = ListPeers.get(srcPeerIdx);
        int dstPeerIdx = rand.nextInt(NumberOfPeers);
        Peer dstPeer = ListPeers.get(dstPeerIdx);
        if (RANDOM){
            // transfer random useful packet from src to dst
            int pktIdx = randomPeerChunk(dstPeer, srcPeerIdx);
        }
        else if (GROUPSUPP){
            int pktIdx = peerChunkGroupSuppression(dstPeer, srcPeerIdx);
        }
        else if (RAREST){
            int pktIdx = peerRarestChunk(dstPeer, srcPeerIdx);
        }
        else if (DECLINE_POPULAR){
             int pktIdx = declineMostPopularChunk(dstPeer, srcPeerIdx);
        }
        else if (CHAIN_POLICY){

            int pktIdx = peerChainPolicy(dstPeer, srcPeerIdx);
        }
        assertMetaVariables();
        // Check if dst Peer has received all the packets. If yes then remove the peer from system
        boolean removed = removePeer(dstPeer);
    }
    private void arrivalEvent(){
        Peer newPeer = new Peer();
        ListPeers.add(newPeer);
        NumberOfPeers = NumberOfPeers +1;
        //update Distribution
        bufferDistributionCount[0] += 1;
        PeerNumberOfPktsMap.get(0).add(newPeer);
        PeerNumberOfPktsArray.get(0).add(newPeer);
        assertMetaVariables();
        writer.println(NumberOfPeers);
        double []pdf = normalize();
        for(int k=0;k< NumberOfPieces; k++){
            String prob = String.format("%.3f", pdf[k] );
            writer_distribution.print(prob+" ");
        }
        writer_distribution.println();
    }
    private double[] normalize(){
        double[] pdf = new double[NumberOfPieces];
        for(int i=0;i < NumberOfPieces;i++){
            pdf[i] = (double) marginalPktDistributionCount[i]/NumberOfPeers;
        }
        return pdf;

    }
    private void seedTransferEvent(){
        // Seed gives a packet
        /* Peer Selection - Either random or Most Deprived*/
        boolean RANDOMPEER = true;

        int dstPeerIdx=0;
        Peer dstPeer = ListPeers.get(dstPeerIdx);
        if (RANDOMPEER) {
            dstPeerIdx = rand.nextInt(NumberOfPeers);
            dstPeer = ListPeers.get(dstPeerIdx);
        }
        else if (GROUPSUPP || CHAIN_POLICY){
            // Select a most deprived peer
            int k=0;
            for(k=0; k <NumberOfPieces+1; k++){
                if (PeerNumberOfPktsMap.get(k).size() >0){
                    break;
                }
            }
            ArrayList<Peer> deprivedList = PeerNumberOfPktsMap.get(k);
//            ArrayList<Peer> deprivedList = PeerNumberOfPktsMap.get(k);
            int deprivedListIdx = rand.nextInt(deprivedList.size());
            dstPeer = deprivedList.get(deprivedListIdx);
        }

        if (RANDOM || GROUPSUPP || CHAIN_POLICY) {
            int pktIdx = seedRandomChunk(dstPeer);
        }
        else if (RAREST) {
            int pktIdx = seedRarestChunk(dstPeer);
        }
        else if (DECLINE_POPULAR){
            int pktIdx = seedDeclineMostPopularChunk(dstPeer);
        }
        // Transfer a random useful packet from seed to dst
        assertMetaVariables();

        // Check if dst Peer has received all the packets. If yes then remove the peer from system
        boolean removed = removePeer(dstPeer );
        assertMetaVariables();
    }

    private boolean removePeer(Peer dstPeer){
        // Returns true if peer has all the packets and removed, else returns false
        writer.println(NumberOfPeers);
        double []pdf = normalize();
        for(int k=0;k< NumberOfPieces; k++){
            String prob = String.format("%.3f", pdf[k] );
            writer_distribution.print(prob+" ");
        }
        writer_distribution.println();
        // return value
        if(Arrays.equals(dstPeer.Buffer, Peer.FullBuffer)){
            // remove
            ListPeers.remove(dstPeer);
            NumberOfPeers = NumberOfPeers -1;
            // update distributions
            for(int i = 0; i < NumberOfPieces; i++){
                marginalPktDistributionCount[i] = marginalPktDistributionCount[i] -1;
            }
            bufferDistributionCount[toDecimal(Peer.FullBuffer)] = bufferDistributionCount[toDecimal(Peer.FullBuffer)] -1;
            PeerNumberOfPktsMap.get(NumberOfPieces).remove(dstPeer);
            PeerNumberOfPktsArray.get(NumberOfPieces).remove(dstPeer);
            assertMetaVariables();
            return true; // Peer is removed
        }
        return false; // Peer is not removed
    }

    /* Chunk Selection Policies */
    private int declineMostPopularChunk(Peer dstPeer, int srcIdx){
        /*
        - Transfers a useful packet from src to destination
        - Returns index of the packet transmitted
        - If no packet is transmitted returns -1
        - Also update marginal and buffer distributions
        */
        int [] dst = dstPeer.Buffer;
        int [] src = ListPeers.get(srcIdx).Buffer;

        //find most popular pktidx
        int maxPopularity = 0;
        for(int j=0; j < NumberOfPieces; j++){
            int value = marginalPktDistributionCount[j];
            if(maxPopularity < value){
                maxPopularity = value;
            }
        }
        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j=0; j < NumberOfPieces; j++) {
            if (marginalPktDistributionCount[j] == maxPopularity){
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfPieces){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i=0; i< NumberOfPieces ; i++){
            // Check if i is popular index
            boolean popularIdx = false;
            for(int mostPopularIdx: mostPopularIdxList){
                if (mostPopularIdx == i){
                    popularIdx = true;
                }
            }

            if(popularIdx) continue;
            else if (src[i] > dst[i]){
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
            updateHashMap(dstPeer);
            return pktIdx;
        }
        return -1;

    }
    private int randomPeerChunk(Peer dstPeer, int srcIdx){
        /*
        - Transfers a useful packet from src to destination
        - Returns index of the packet transmitted
        - If no packet is transmitted returns -1
        - Also update marginal and buffer distributions
        */
        int [] dst = dstPeer.Buffer;
        int [] src = ListPeers.get(srcIdx).Buffer;

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
            updateHashMap(dstPeer);
            return pktIdx;
        }
        return -1;

    }
    private int peerChunkGroupSuppression(Peer dstPeer, int srcIdx){
        /*
        - Here group Suppresion is implemented
        - Identify the (decimal) buffer state of largest group
        - If the src is in largest group then check if the dst has more pkts ow abadon
        */
        int [] dst = dstPeer.Buffer;
        int [] src = ListPeers.get(srcIdx).Buffer;

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
            updateHashMap(dstPeer);
            return pktIdx;
        }
        return -1;

    }
    private int peerChainPolicy(Peer dstPeer, int srcIdx){
        /*
        - Here Chain policy is implemented
        - A src will upload only if dst has more number of pacekts
        - A peer uploads to peer which has only
        */
        int [] dst = dstPeer.Buffer;
        int [] src = ListPeers.get(srcIdx).Buffer;

        // Determine largestbuffer
        if (sumPackets(dst) < sumPackets(src) ){
            return -1; // abandoning the transfer to avoid single club
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
            updateHashMap(dstPeer);
            return pktIdx;
        }
        return -1;

    }

    private int peerRarestChunk(Peer dstPeer, int srcIdx){
        /*
        - Transfers a useful packet from src to destination when ever available
        - Gives preference to rearest packet available
        - Returns index of the packet transmitted
        - If no packet is transmitted returns -1
        */
        int [] dst = dstPeer.Buffer;
        int [] src = ListPeers.get(srcIdx).Buffer;

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i=0; i< dst.length; i++){
            if (src[i] > dst[i]){
                usefulPktIdx.add(i);
            }
        }
        if (usefulPktIdx.size() >0) {
            // Select the rarest packet
            int rarestPktIdx = usefulPktIdx.get(0);
            int rarestPktCount = NumberOfPeers;
            for (int j = 0; j < usefulPktIdx.size(); j++) {
                int count = marginalPktDistributionCount[usefulPktIdx.get(j)];
                if (count < rarestPktCount) {
                    rarestPktCount = count;
                    rarestPktIdx = usefulPktIdx.get(j);
                }

            }
            dst[rarestPktIdx] = 1;
            // update distribution
            updateDistributions(dst,rarestPktIdx);
            updateHashMap(dstPeer);
            return rarestPktIdx;
        }
        return -1;

    }



    /* Seed Policies */
    private int seedRandomChunk(Peer dstPeer){
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
            updateHashMap(dstPeer);
            return pktIdx;
        }
        return -1;

    }
    private int seedRarestChunk(Peer dstPeer){
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
        if (usefulPktIdx.size() >0) {
            // Select the rarest packet
            int rarestPktIdx = usefulPktIdx.get(0);
            int rarestPktCount = NumberOfPeers;
            for (int j = 0; j < usefulPktIdx.size(); j++) {
                int count = marginalPktDistributionCount[usefulPktIdx.get(j)];
                if (count < rarestPktCount) {
                    rarestPktCount = count;
                    rarestPktIdx = usefulPktIdx.get(j);
                }

            }
//            rarestPktIdx = usefulPktIdx.get(0);
            dst[rarestPktIdx] = 1;
            // update distribution
            updateDistributions(dst,rarestPktIdx);
            updateHashMap(dstPeer);
            return rarestPktIdx;
        }
        return -1;

    }
    private int seedDeclineMostPopularChunk(Peer dstPeer){
        /*
        src contains all elements
        */
        int [] dst = dstPeer.Buffer;
        int[] src = Peer.FullBuffer;

        //find most popular pktidx
        int maxPopularity = 0;
        for(int j=0; j < NumberOfPieces; j++){
            int value = marginalPktDistributionCount[j];
            if(maxPopularity < value){
                maxPopularity = value;
            }
        }
        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j=0; j < NumberOfPieces; j++) {
            if (marginalPktDistributionCount[j] == maxPopularity){
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfPieces){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i=0; i< NumberOfPieces ; i++){
            // Check if i is popular index
            boolean popularIdx = false;
            for(int mostPopularIdx: mostPopularIdxList){
                if (mostPopularIdx == i){
                    popularIdx = true;
                }
            }

            if(popularIdx) continue;
            else if (src[i] > dst[i]){
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
            updateHashMap(dstPeer);
            return pktIdx;
        }
        return -1;

    }



    /* Metadata Update Function */
    private void updateHashMap(Peer dstPeer){
        int[] tempBuffer = dstPeer.Buffer;
        PeerNumberOfPktsMap.get(sumPackets(tempBuffer) - 1).remove(dstPeer);
        PeerNumberOfPktsMap.get(sumPackets(tempBuffer)).add(dstPeer);


        PeerNumberOfPktsArray.get(sumPackets(tempBuffer) - 1).remove(dstPeer);
        PeerNumberOfPktsArray.get(sumPackets(tempBuffer)).add(dstPeer);

        if(!assertMetaVariables()) {
            System.out.println("Testing Asserstion");
        }


    }
    private void updateDistributions(int[] dst,int pktIdx){
        // update distribution
        marginalPktDistributionCount[pktIdx] = (marginalPktDistributionCount[pktIdx] +1);
        int dstDecimalValue = toDecimal(dst);
        bufferDistributionCount[dstDecimalValue] = (bufferDistributionCount[dstDecimalValue] +1 );
        dstDecimalValue = dstDecimalValue - (int)pow(2,pktIdx);
        bufferDistributionCount[dstDecimalValue] = (bufferDistributionCount[dstDecimalValue] -1 );

    }

    /* Utility Function */
    private int sumPackets(int [] buffer){
        int sum = 0;
        for(int i = 0; i< NumberOfPieces; i++){
            sum = sum + buffer[i];
        }
        return sum;
    }
    private int toDecimal(int[] buffer){
        int decValue = 0;
        for(int i=0;i<buffer.length;i++){
            decValue = decValue + buffer[i]*(int)pow(2,i);
        }
        return decValue;
    }
    private double expRand(double lambda) {
        // Return an exponential random varible with parameter lambda
        if (lambda <=0){
            return Math.pow(2,30);
        }
        return  Math.log(1-rand.nextDouble())/(-lambda);
    }
}
