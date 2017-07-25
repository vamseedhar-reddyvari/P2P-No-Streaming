package com.vamsee.p2p.no.streaming;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
    private boolean RANDOM, RAREST,  GROUPSUPP , MODE_SUPPRESSION, DISTR_MODE_SUPPRESSION, CHAIN_POLICY, FRIEDMAN, COMMONCHUNK;
    private PrintWriter writerNoPeers, writerDistribution, writerTime;

    // Meta Variables
    /* - At any time sum of bufferDistributionCount should be equal to Number of Peers.
    - Also sum of dictionary values should also equal to Number of Peers. */
    private int[] marginalPktDistributionCount; // 1xm array of number of Peers that contain a piece - pi|x|
    private int[] bufferDistributionCount; // 1x2^m array of number of peers that contain same pieces - x
    // 1xm+1 Dictionary which maps the number of packets to a list of all peers that contain that many number of packets.
    private ArrayList<ArrayList<Peer> > PeerNumberOfPktsArray; // 1xm+1 Dictionary which maps the number of packets \

    // Utility Classes
    private Random rand; // Object for generating random numbers
    private void initializeOneClubPeers(){
        this.NumberOfPeers = 500; //Lets start with 500 peers
        this.ListPeers = new ArrayList<Peer>();
        for(int i = 0; i<this.NumberOfPeers; i++){
            boolean oneClub = true;
            Peer newPeer = new Peer(oneClub);
            this.ListPeers.add(newPeer);
        }
        // Meta-Variable initialization for one-club state
        marginalPktDistributionCount = new int[NumberOfPieces];
        Arrays.fill(marginalPktDistributionCount,this.NumberOfPeers); // As all peers begin with 0 packets margianl distribution will be 0
        marginalPktDistributionCount[0] = 0;


        bufferDistributionCount =  new int[(int)pow(2, NumberOfPieces)];
        Arrays.fill(bufferDistributionCount,0); // As all peers begin with 0 packets distribution will be 0
        bufferDistributionCount[(int)(pow(2,NumberOfPieces) -2)] = NumberOfPeers; // All peers have 0  packets
        //Initialize Hashmap
        PeerNumberOfPktsArray = new ArrayList<ArrayList<Peer>>();
        for(int i = 0; i < NumberOfPieces +1; i++) {
            PeerNumberOfPktsArray.add(new ArrayList<Peer>());
        }
        for(int j = 0; j< NumberOfPeers; j++){
            PeerNumberOfPktsArray.get(this.NumberOfPieces-1).add(ListPeers.get(j)); // All peers doesn't have any packets. So 0 packets will map to entire list
        }
    }

    private void initializeEmptyPeers(){
        this.ListPeers = new ArrayList<Peer>();
        for(int i = 0; i<this.NumberOfPeers; i++){
            Peer newPeer = new Peer();
            this.ListPeers.add(newPeer);
        }
        // Meta-Variable initialization for one-club state
        marginalPktDistributionCount = new int[NumberOfPieces];
        bufferDistributionCount =  new int[(int)pow(2, NumberOfPieces)];
        Arrays.fill(marginalPktDistributionCount,0); // As all peers begin with 0 packets margianl distribution will be 0
        Arrays.fill(bufferDistributionCount,0); // As all peers begin with 0 packets distribution will be 0
        bufferDistributionCount[0] = NumberOfPeers; // All peers have 0  packets
        //Initialize Hashmap
        PeerNumberOfPktsArray = new ArrayList<ArrayList<Peer>>();
        for(int i = 0; i < NumberOfPieces +1; i++) {
            PeerNumberOfPktsArray.add(new ArrayList<Peer>());
        }
        for(int j = 0; j< NumberOfPeers; j++){
            PeerNumberOfPktsArray.get(0).add(ListPeers.get(j)); // All peers doesn't have any packets. So 0 packets will map to entire list
        }
    }
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
        this.MODE_SUPPRESSION = false;
        this.CHAIN_POLICY = false;
        this.DISTR_MODE_SUPPRESSION = false;
        this.FRIEDMAN = false;
        this.COMMONCHUNK = false;

        //Initialize Peers
        initializeOneClubPeers();
//        initializeEmptyPeers();

        // Utility Objects
        rand = new Random(); //initialize random


        // Sanity Check
        assertMetaVariables();

    }

    public int Run(String policy) throws IOException{
        // Run the simulation
        // Generate sojourn times for each poisson random variable and execute the event with least sojourn time


        System.out.println("Running "+policy+" Policy....");
        Ticks = 4*(int) pow(10,5);

        if(policy.equals("Rarest")) {
            RAREST = true;
        }
        else if(policy.equals("Random")) RANDOM = true;
        else if(policy.equals("ModeSup")) MODE_SUPPRESSION= true;
        else if(policy.equals("DistrModeSup")) DISTR_MODE_SUPPRESSION= true;
        else if(policy.equals("GroupSup")) GROUPSUPP= true;
        else if(policy.equals("Friedman")) FRIEDMAN= true;
        else if(policy.equals("CommonChunk")) COMMONCHUNK= true;

        writerNoPeers = new PrintWriter("output/"+"k"+NumberOfPieces+"lambda4/"+policy+"_"+"peers.txt", "UTF-8");
        writerDistribution = new PrintWriter("output/"+"k"+NumberOfPieces+"lambda4/"+policy+"_"+"distribution.txt", "UTF-8");
        writerTime = new PrintWriter("output/"+"k"+NumberOfPieces+"lambda4/"+policy+"_"+"waitingTime.txt","UTF-8");

        for(int t = 0; t< Ticks; t++){

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
                arrivalEvent(peerArrivalTime);
            }
            else if(peerContactTime<= peerArrivalTime && peerContactTime <= seedContactTime){
                if (NumberOfPeers >0)
                {
                    peer2PeerEvent(minTickIdx, peerContactTime);
                }
//                if (NumberOfPeers ==0) {break;}
            }
            else if ( seedContactTime<=peerArrivalTime && seedContactTime<=peerContactTime){
                if (NumberOfPeers >0) {
                    seedTransferEvent(seedContactTime);
                }
//                if (NumberOfPeers ==0) {break;}
            }
        }
        writerNoPeers.close();
        writerDistribution.close();
        writerTime.close();

        //RESET
        RANDOM = false;
        RAREST = false;
        GROUPSUPP = false;
        COMMONCHUNK = false;
        MODE_SUPPRESSION = false;
        DISTR_MODE_SUPPRESSION = false;
        FRIEDMAN = false;
        initializeOneClubPeers();


        return 0;
    }


    /* Events */
    private void peer2PeerEvent(int srcPeerIdx, double timeEpoch){
        // Peers Exchange packets
        assertMetaVariables();
        updateTime(timeEpoch);
        Peer srcPeer = ListPeers.get(srcPeerIdx);
        int dstPeerIdx = rand.nextInt(NumberOfPeers);
        Peer dstPeer = ListPeers.get(dstPeerIdx);
        if (RANDOM){
            // transfer random useful packet from src to dst
            int pktIdx = peerRandomChunkPolicy(dstPeer, srcPeerIdx);
        }
        else if (GROUPSUPP){
            int pktIdx = peerChunkGroupSuppression(dstPeer, srcPeerIdx);
        }
        else if (RAREST){
            int pktIdx = peerRarestChunk(dstPeer, srcPeerIdx);
        }
        else if (MODE_SUPPRESSION){
             int pktIdx = peerModeSuppresionPolicy(dstPeer, srcPeerIdx);
        }
        else if (CHAIN_POLICY){

            int pktIdx = peerChainPolicy(dstPeer, srcPeerIdx);
        }
        else if(DISTR_MODE_SUPPRESSION){
            int pktIdx = peerDistributedModeSuppression(dstPeer, srcPeerIdx);
        }
        else if(FRIEDMAN){
            int pktIdx = peerFriedmanPolicy( ListPeers.get(srcPeerIdx) );
        }
        else if(COMMONCHUNK){
            int pktIdx = peerCommonChunkPolicy( ListPeers.get(srcPeerIdx) );
        }
        else{
            System.out.println("Peer Chunk Policy Not found");
        }
        // Check if dst Peer has received all the packets. If yes then remove the peer from system
//        boolean removed = removePeer(dstPeer);
        assertMetaVariables();
    }
    private void arrivalEvent(double timeEpoch){

        assertMetaVariables();
        updateTime(timeEpoch);
        Peer newPeer = new Peer();
        this.ListPeers.add(newPeer);
        NumberOfPeers = NumberOfPeers +1;
        //update Distribution
        bufferDistributionCount[0] += 1;
        PeerNumberOfPktsArray.get(0).add(newPeer);
        assertMetaVariables();
        writerNoPeers.println(NumberOfPeers);
        double []pdf = normalize();
        for(int k=0;k< NumberOfPieces; k++){
            String prob = String.format("%.3f", pdf[k] );
            writerDistribution.print(prob+" ");
        }
        writerDistribution.println();
        assertMetaVariables();
    }
    private void seedTransferEvent(double timeEpoch){
        // Seed gives a packet
        /* Peer Selection - Either random or Most Deprived*/

        // Peer Selection

        assertMetaVariables();
        updateTime(timeEpoch);
        int dstPeerIdx=0;
        Peer dstPeer = ListPeers.get(dstPeerIdx);
        if (RAREST || RANDOM || MODE_SUPPRESSION || DISTR_MODE_SUPPRESSION || FRIEDMAN) {
            dstPeerIdx = rand.nextInt(NumberOfPeers);
            dstPeer = ListPeers.get(dstPeerIdx);
        }
        else if (GROUPSUPP || CHAIN_POLICY){
            // Select a most deprived peer
            int k=0;
            for(k=0; k <NumberOfPieces+1; k++){
                if (PeerNumberOfPktsArray.get(k).size() >0){
                    break;
                }
            }
            ArrayList<Peer> deprivedList = PeerNumberOfPktsArray.get(k);
            int deprivedListIdx = rand.nextInt(deprivedList.size());
            dstPeer = deprivedList.get(deprivedListIdx);
        }

        // Piece Selection
        if (RANDOM || GROUPSUPP || CHAIN_POLICY) {
            int pktIdx = seedRandomChunk(dstPeer);
        }
        else if (RAREST) {
            int pktIdx = seedRarestChunk(dstPeer);
        }
        else if (MODE_SUPPRESSION){
            int pktIdx = seedDeclineMostPopularChunk(dstPeer);
        }
        else if (DISTR_MODE_SUPPRESSION){
            int pktIdx = distributedSeedDeclineMostPopularChunk(dstPeer);
        }
        else if(FRIEDMAN){
            int pktIdx = seedFriedmanPolicy(dstPeer);
        }
        else if(COMMONCHUNK){
            int pktIdx = seedFriedmanPolicy(dstPeer);
        }
        else{
            System.out.println("Seed Policy Not found");
        }
        // Transfer a random useful packet from seed to dst
//        assertMetaVariables();

        // Check if dst Peer has received all the packets. If yes then remove the peer from system
//        boolean removed = removePeer(dstPeer );
        assertMetaVariables();
    }


    /* Chunk Selection Policies */
    private int peerModeSuppresionPolicy(Peer dstPeer, int srcIdx){
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
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;

    }
    private int peerRandomChunkPolicy(Peer dstPeer, int srcIdx){
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
            // Check if dst Peer has received all the packets. If yes then remove the peer from system
            // update distribution
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
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
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
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
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
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
            updateMedaData(dstPeer,rarestPktIdx);
            boolean removed = removePeer(dstPeer );
            return rarestPktIdx;
        }
        return -1;

    }
    private int peerDistributedModeSuppression(Peer dstPeer, int srcIdx){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */
        int [] fullBuffer = new int[NumberOfPieces];
        for(int k=0;k<NumberOfPieces;k++){
            fullBuffer[k] = 1;
        }
        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = fullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = fullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = fullBuffer;

        int pktIdx = nonAbundentMatch(dstPeer,buffer1,buffer2,buffer3);

        // Select a random packet and transfer
        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;
    }
    private int peerFriedmanPolicy(Peer dstPeer ){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */
        int [] fullBuffer = new int[NumberOfPieces];
        for(int k=0;k<NumberOfPieces;k++){
            fullBuffer[k] = 1;
        }


        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = fullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = fullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = fullBuffer;

        int pktIdx = rareMatch(dstPeer,buffer1,buffer2,buffer3);

        // Select a random packet and transfer
        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;

    }
    private int peerCommonChunkPolicy(Peer dstPeer ){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */

        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = Peer.FullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = Peer.FullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = Peer.FullBuffer;


        int noPktsDstPeer = 0;
        int pktIdx = -1;
        for(int piece:dstPeer.Buffer){
            noPktsDstPeer = noPktsDstPeer + piece;
        }
        if(noPktsDstPeer ==0){
            pktIdx = rareMatch(dstPeer,buffer1,buffer2,buffer3);
        }
        else if(noPktsDstPeer < NumberOfPieces-1){
            // Random pkt
            pktIdx = randomMatch(dstPeer, buffer1);

        }
        else if(noPktsDstPeer == NumberOfPieces -1){
            // Check if all other pkts are available abundently (atleast 2 times)
            pktIdx = conditionalMatch(dstPeer, buffer1, buffer2, buffer3);

        }

        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
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
            // Check if dst Peer has received all the packets. If yes then remove the peer from system
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
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
            updateMedaData(dstPeer,rarestPktIdx);
            boolean removed = removePeer(dstPeer );
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
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;

    }
    private int distributedSeedDeclineMostPopularChunk(Peer dstPeer){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */
        int [] fullBuffer = new int[NumberOfPieces];
        for(int k=0;k<NumberOfPieces;k++){
            fullBuffer[k] = 1;
        }


        int randomIdx1 = ListPeers.size() +1;
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = fullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = fullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = fullBuffer;

        int pktIdx = nonAbundentMatch(dstPeer,buffer1,buffer2,buffer3);

        // Select a random packet and transfer
        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;
    }
    private int seedFriedmanPolicy(Peer dstPeer ){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */
        int [] fullBuffer = new int[NumberOfPieces];
        for(int k=0;k<NumberOfPieces;k++){
            fullBuffer[k] = 1;
        }


        int randomIdx1 = ListPeers.size() +1;
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = fullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = fullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = fullBuffer;

        int pktIdx = rareMatch(dstPeer,buffer1,buffer2,buffer3);

        // Select a random packet and transfer
        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;

    }
    private int seedCommonChunkPolicy(Peer dstPeer ){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */

        int randomIdx1 = ListPeers.size()+1;
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = Peer.FullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = Peer.FullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = Peer.FullBuffer;


        int noPktsDstPeer = 0;
        int pktIdx = -1;
        for(int piece:dstPeer.Buffer){
            noPktsDstPeer = noPktsDstPeer + piece;
        }
        if(noPktsDstPeer ==0){
            pktIdx = rareMatch(dstPeer,buffer1,buffer2,buffer3);
        }
        else if(noPktsDstPeer < NumberOfPieces-1){
            // Random pkt
            pktIdx = randomMatch(dstPeer, buffer1);

        }
        else if(noPktsDstPeer == NumberOfPieces -1){
            // Check if all other pkts are available abundently (atleast 2 times)
            pktIdx = conditionalMatch(dstPeer, buffer1, buffer2, buffer3);

        }

        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMedaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;

    }

    private boolean removePeer(Peer dstPeer){
        // Returns true if peer has all the packets and removed, else returns false
        writerNoPeers.println(NumberOfPeers);
        double []pdf = normalize();
        for(int k=0;k< NumberOfPieces; k++){
            String prob = String.format("%.3f", pdf[k] );
            writerDistribution.print(prob+" ");
        }
        writerDistribution.println();
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
            PeerNumberOfPktsArray.get(NumberOfPieces).remove(dstPeer);
//            assertMetaVariables();
            writerTime.println(dstPeer.time);
            return true; // Peer is removed
        }
        return false; // Peer is not removed
    }

    /* Metadata Update Function */
    private void updateMedaData(Peer dstPeer, int pktIdx){
        int[] tempBuffer = dstPeer.Buffer;

        PeerNumberOfPktsArray.get(sumPackets(tempBuffer) - 1).remove(dstPeer);
        PeerNumberOfPktsArray.get(sumPackets(tempBuffer)).add(dstPeer);


        int[] dst = dstPeer.Buffer;
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
    private boolean assertMetaVariables(){
        int sum = 0;
        for(int count:bufferDistributionCount){
            if(count < 0) System.out.println("Assert Failed: Buffer Distribution Count negative");
            sum = sum + count;
        }

        if (sum != NumberOfPeers) {
            System.out.println("Assert Failed: Buffer Distribution Sum");
            return false;
        }

        sum = 0;
        for(ArrayList value: PeerNumberOfPktsArray){
            sum = sum + value.size();
        }
        if (sum != NumberOfPeers) {
            System.out.println("Assert failed: HashMap Sum");
            return false;
        }

        return true;

    }
    private double[] normalize(){
        double[] pdf = new double[NumberOfPieces];
        for(int i=0;i < NumberOfPieces;i++){
            pdf[i] = (double) marginalPktDistributionCount[i]/NumberOfPeers;
        }
        return pdf;

    }
    private void updateTime(double timeEpoch){
        for(Peer eachPeer: ListPeers){
            eachPeer.time = eachPeer.time+ timeEpoch;
        }
    }

    private int rareMatch(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){
        ArrayList<Integer> rareIndicies = new ArrayList<Integer>();
        for(int i=0;i < NumberOfPieces; i++){
            if(dstPeer.Buffer[i] == 0){
                int count = buffer1[i] + buffer2[i] + buffer3[i];
                if (count ==1) {
                    rareIndicies.add(i);
                }
            }
        }
        if (rareIndicies.size() >0){

            int pktIdx = rareIndicies.get(rand.nextInt(rareIndicies.size()));
            return pktIdx;
        }
        else return -1;

    }
    private int nonAbundentMatch(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){
        ArrayList<Integer> rareIndicies = new ArrayList<Integer>();
        for(int i=0;i < NumberOfPieces; i++){
            if(dstPeer.Buffer[i] == 0){
                int count = buffer1[i] + buffer2[i] + buffer3[i];
                if (count !=3) {
                    rareIndicies.add(i);
                }
            }
        }
        if (rareIndicies.size() >0){

            int pktIdx = rareIndicies.get(rand.nextInt(rareIndicies.size()));
            return pktIdx;
        }
        else return -1;

    }
    private int conditionalMatch(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){
        int pktIdx = -1;
        for(int i=0;i < NumberOfPieces; i++){
            if(dstPeer.Buffer[i] == 0){
                pktIdx = i;
                int count = buffer1[i] + buffer2[i] + buffer3[i];
                if (count ==0) return -1;
            }
            else{
                int count = buffer1[i] + buffer2[i] + buffer3[i];
                if (count <2)  return -1;

            }
        }
        return pktIdx;

    }
    private int randomMatch(Peer dstPeer, int[] buffer1){
        ArrayList<Integer> usefulIndicies = new ArrayList<Integer>();
        for(int i=0;i < NumberOfPieces; i++){
            if(dstPeer.Buffer[i] == 0){
                if (buffer1[i] == 1) {
                    usefulIndicies.add(i);
                }
            }
        }
        if (usefulIndicies.size() >0){

            int pktIdx = usefulIndicies.get(rand.nextInt(usefulIndicies.size()));
            return pktIdx;
        }
        else return -1;

    }
}
