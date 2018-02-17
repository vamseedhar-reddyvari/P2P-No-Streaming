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
    private int NumberOfChunks; // Buffer length
    private boolean BESTCASE, RANDOM, RAREST,  GROUPSUPP ,BOOST_GROUPSUPP, THMODESUP, BOOST_TH_MODESUP;
    private boolean MODE_SUPPRESSION, BOOST_MODE_SUPPRESSION, DISTR_MODE_SUPPRESSION, CHAIN_POLICY ;
    private boolean RARECHUNK, COMMONCHUNK, RELAXEDMODESUP, BOOSTEDRAREST, STRICTLOCALMODE, SUPPRRARECHUNK;
    private boolean SUPPRCOMMONCHUNK, SUPPRLOCALMODE, EWMA_MODESUP, BOOST_EWMA_MODESUP;
    private PrintWriter writerNoPeers, writerDistribution, writerTime, writerStarvations, writerCounts, writerFullState;
    private String outputDir;
    private boolean restirctToOnePeer ;
    private boolean USING_DICT;
    private int randomSeedInput;
    private int THRESH;

    // Meta Variables
    /* - At any time sum of bufferDistributionCount should be equal to Number of Peers.
    - Also sum of dictionary values should also equal to Number of Peers. */
    private int[] marginalPktDistributionCount; // 1xm array of number of Peers that contain a piece - pi|x|
    private int[] bufferDistributionCount; // 1x2^m array of number of peers that contain same pieces - x
    private HashMap<String,Integer> bufferDistributionCountDict; // 1x2^m array of number of peers that contain same pieces - x

    // 1xm+1 Dictionary which maps the number of packets to a list of all peers that contain that many number of packets.
    private ArrayList<ArrayList<Peer> > PeerNumberOfPktsArray; // 1xm+1 Dictionary which maps the number of packets \

    // Utility Classes
    private Random rand; // Object for generating random numbers
    private void initializeOneClubPeers(int numberOfChunks){
        this.NumberOfPeers = 500; //Lets start with 500 peers
        this.ListPeers = new ArrayList<Peer>();
        for(int i = 0; i<this.NumberOfPeers; i++){
            boolean oneClub = true;
            Peer newPeer = new Peer(oneClub, numberOfChunks);
            this.ListPeers.add(newPeer);
        }
        // Meta-Variable initialization for one-club state
        marginalPktDistributionCount = new int[NumberOfChunks];
        Arrays.fill(marginalPktDistributionCount,this.NumberOfPeers); // As all peers begin with 0 packets margianl distribution will be 0
        marginalPktDistributionCount[0] = 0;


        if(!USING_DICT) {
            bufferDistributionCount = new int[(int) pow(2, NumberOfChunks)];
            Arrays.fill(bufferDistributionCount, 0); // As all peers begin with 0 packets distribution will be 0
            bufferDistributionCount[(int) (pow(2, NumberOfChunks) - 2)] = NumberOfPeers; // All peers have 0  packets
        }
        else {
            bufferDistributionCountDict = new HashMap<String, Integer>();
            bufferDistributionCountDict.put(Arrays.toString(Peer.OneClubBuffer), NumberOfPeers);
        }

        //Initialize Hashmap
        PeerNumberOfPktsArray = new ArrayList<ArrayList<Peer>>();
        for(int i = 0; i < NumberOfChunks +1; i++) {
            PeerNumberOfPktsArray.add(new ArrayList<Peer>());
        }
        for(int j = 0; j< NumberOfPeers; j++){
            PeerNumberOfPktsArray.get(this.NumberOfChunks -1).add(ListPeers.get(j)); // All peers doesn't have any packets. So 0 packets will map to entire list
        }




    }

    private void initializeEmptyPeers(int numberOfChunks){
        this.NumberOfPeers = 500; //Lets start with 500 peers
        this.ListPeers = new ArrayList<Peer>();
        for(int i = 0; i<this.NumberOfPeers; i++){
            Peer newPeer = new Peer(numberOfChunks);
            this.ListPeers.add(newPeer);
        }
        // Meta-Variable initialization for one-club state
        marginalPktDistributionCount = new int[NumberOfChunks];


        if(!USING_DICT){
            bufferDistributionCount =  new int[(int)pow(2, NumberOfChunks)];
            Arrays.fill(bufferDistributionCount,0); // As all peers begin with 0 packets distribution will be 0
            bufferDistributionCount[0] = NumberOfPeers; // All peers have 0  packets
        }
        else{
            bufferDistributionCountDict = new HashMap<String,Integer>();
            bufferDistributionCountDict.put(Arrays.toString(Peer.NullBuffer),NumberOfPeers);
        }

        Arrays.fill(marginalPktDistributionCount,0); // As all peers begin with 0 packets margianl distribution will be 0
        //Initialize Hashmap
        PeerNumberOfPktsArray = new ArrayList<ArrayList<Peer>>();
        for(int i = 0; i < NumberOfChunks +1; i++) {
            PeerNumberOfPktsArray.add(new ArrayList<Peer>());
        }
        for(int j = 0; j< NumberOfPeers; j++){
            PeerNumberOfPktsArray.get(0).add(ListPeers.get(j)); // All peers doesn't have any packets. So 0 packets will map to entire list
        }
    }
    //Constructor
    public P2PSimulation(double Us, double lambda, double lambda_p, String outputDir, int seedInput, int numberOfChunks, HashMap<Integer,Integer> optimalThresholds) throws IOException{

        System.out.println("################ Random Generator Seed: "+seedInput+" ###################### ");
        // System Variables
//        this.THRESH = optimalThresholds.get(numberOfChunks);
        this.THRESH = 2*numberOfChunks-1;
        this.restirctToOnePeer = false;
        this.USING_DICT= true;
        this.NumberOfChunks = numberOfChunks;
        this.lambda_p = lambda_p;
        this.Us = Us;
        this.lambda = lambda;
        this.RANDOM = false;
        this.BESTCASE = false;
        this.BOOSTEDRAREST = false;
        this.STRICTLOCALMODE= false;
        this.RAREST = false;
        this.GROUPSUPP = false;
        this.BOOST_GROUPSUPP = false;
        this.MODE_SUPPRESSION = false;
        this.BOOST_MODE_SUPPRESSION = false;
        this.CHAIN_POLICY = false;
        this.DISTR_MODE_SUPPRESSION = false;
        this.RARECHUNK = false;
        this.COMMONCHUNK = false;
        this.RELAXEDMODESUP = false;
        this.outputDir = outputDir;
        this.randomSeedInput = seedInput;

        //Initialize Peers
        initializeOneClubPeers(this.NumberOfChunks);
        //initializeEmptyPeers(this.NumberOfChunks);

        // Utility Objects
        rand = new Random(); //initialize random
        rand.setSeed(seedInput);
        //rand.setSeed(0);


        //Testing
        //testMethods();

        // Print Simulation Parameters
        System.out.println("Lambda: "+this.lambda + ", m: "+this.NumberOfChunks);
        if(restirctToOnePeer){
            System.out.println("Restrict to One Peer Enabled");
        }
        else{
            System.out.println("Restrict to One Peer Disabled");
        }
        if(!USING_DICT){
            System.out.println("Using Array instead of Dictionary");
        }
        else{
            System.out.println("Using Dictionary");
        }
        // Sanity Check
        assertMetaVariables();

    }

    public int Run(String policy ) throws IOException{
        // Run the simulation
        // Generate sojourn times for each poisson random variable and execute the event with least sojourn time


        System.out.println("Running "+policy+" Policy....");
        Ticks = (int) pow(10,5);

        if(policy.equals("Rarest")) {
            RAREST = true;
        }
        else if(policy.equals("Random")) RANDOM = true;
        else if(policy.equals("BestCasePolicy")) BESTCASE = true;
        else if(policy.equals("ModeSup")) MODE_SUPPRESSION= true;
        else if(policy.equals("EWMAModeSup")) EWMA_MODESUP= true;
        else if(policy.equals("BoostEWMAModeSup")) BOOST_EWMA_MODESUP= true;
        else if(policy.equals("BoostModeSup")) BOOST_MODE_SUPPRESSION= true;
        else if(policy.equals("DistrModeSup")) DISTR_MODE_SUPPRESSION= true;
        else if(policy.equals("GroupSup")) GROUPSUPP= true;
        else if(policy.equals("BoostGroupSup")) BOOST_GROUPSUPP= true;
        else if(policy.equals("RareChunk")) RARECHUNK = true;
        else if(policy.equals("CommonChunk")) COMMONCHUNK= true;
        else if(policy.equals("RelaxedModeSup")) RELAXEDMODESUP = true;
        else if(policy.equals("BoostedRarest")) BOOSTEDRAREST = true;
        else if(policy.equals("StrictLocalMode")) STRICTLOCALMODE= true;
        else if(policy.equals("SupprRareChunk")) SUPPRRARECHUNK= true;
        else if(policy.equals("SupprCommonChunk")) SUPPRCOMMONCHUNK= true;
        else if(policy.equals("SupprLocalMode")) SUPPRLOCALMODE= true;
        else if(policy.equals("ThModeSup")) THMODESUP= true;
        else if(policy.equals("BoostThMode")) BOOST_TH_MODESUP = true;

        writerNoPeers = new PrintWriter(this.outputDir+"m"+ NumberOfChunks +"/lambda"+(int)this.lambda+"-"+policy+"_"+"peers_seed_"+this.randomSeedInput+".txt", "UTF-8");
        writerDistribution = new PrintWriter(this.outputDir+"m"+ NumberOfChunks +"/lambda"+(int)this.lambda+"-"+policy+"_"+"distribution_seed_"+this.randomSeedInput+".txt", "UTF-8");
        writerTime = new PrintWriter(this.outputDir+"m"+ NumberOfChunks +"/lambda"+(int)this.lambda+"-"+policy+"_"+"waitingTime_seed_"+this.randomSeedInput+".txt","UTF-8");
        writerStarvations = new PrintWriter(this.outputDir+"m"+ NumberOfChunks +"/lambda"+(int)this.lambda+"-"+policy+"_"+"starvations_seed_"+this.randomSeedInput+".txt","UTF-8");
        writerCounts= new PrintWriter(this.outputDir+"m"+ NumberOfChunks +"/lambda"+(int)this.lambda+"-"+policy+"_"+"buffer_counts_seed_"+this.randomSeedInput+".txt","UTF-8");
//        writerFullState= new PrintWriter(this.outputDir+"m"+ NumberOfChunks +"/lambda"+(int)this.lambda+"-"+policy+"_"+"full_state_seed_"+this.randomSeedInput+".txt","UTF-8");

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
            }
            else if ( seedContactTime<=peerArrivalTime && seedContactTime<=peerContactTime){
                if (NumberOfPeers >0) {
                    seedTransferEvent(seedContactTime);
                }
            }
        }
        writerNoPeers.close();
        writerDistribution.close();
        writerTime.close();
        writerStarvations.close();
        writerCounts.close();

        //RESET
        RANDOM = false;
        BESTCASE = false;
        BOOSTEDRAREST = false;
        STRICTLOCALMODE= false;
        RAREST = false;
        GROUPSUPP = false;
        BOOST_GROUPSUPP = false;
        COMMONCHUNK = false;
        MODE_SUPPRESSION = false;
        EWMA_MODESUP= false;
        BOOST_EWMA_MODESUP= false;
        BOOST_MODE_SUPPRESSION = false;
        DISTR_MODE_SUPPRESSION = false;
        RARECHUNK = false;
        RELAXEDMODESUP = false;
        SUPPRCOMMONCHUNK = false;
        SUPPRLOCALMODE = false;
        THMODESUP = false;
        BOOST_TH_MODESUP = false;
        SUPPRRARECHUNK = false;

        initializeOneClubPeers(this.NumberOfChunks);
//        initializeEmptyPeers(this.NumberOfChunks);


        return 0;
    }


    /* Events */
    private void peer2PeerEvent(int minPeerIdx, double timeEpoch){
        // Peers Exchange packets
        assertMetaVariables();
        updateTime(timeEpoch);

        int dstPeerIdx = minPeerIdx;
        Peer dstPeer = ListPeers.get(dstPeerIdx);

        // Randomly select source
        int srcPeerIdx = rand.nextInt(NumberOfPeers);
        Peer srcPeer = ListPeers.get(srcPeerIdx);


        if (RANDOM){
            // transfer random useful packet from src to dst
            int pktIdx = peerRandomChunkPolicy(dstPeer, srcPeerIdx);
        }
        else if (GROUPSUPP){
            int pktIdx = peerChunkGroupSuppression(dstPeer, srcPeerIdx);
        }
        else if (BOOST_GROUPSUPP){
            int pktIdx = peerChunkGroupSuppression(dstPeer, srcPeerIdx);
            if(pktIdx == -1){
                int srcPeerIdx2 = rand.nextInt(NumberOfPeers);
                pktIdx = peerChunkGroupSuppression(dstPeer, srcPeerIdx2);
            }
            if(pktIdx == -1) {
                int srcPeerIdx3 = rand.nextInt(NumberOfPeers);
                pktIdx = peerChunkGroupSuppression(dstPeer, srcPeerIdx3);
            }
        }
        else if (RAREST){
            int pktIdx = peerRarestChunk(dstPeer, srcPeerIdx);
        }
        else if (MODE_SUPPRESSION){
             int pktIdx = peerModeSuppresionPolicy(dstPeer, srcPeerIdx);
        }
        else if (EWMA_MODESUP){
            int pktIdx = peerEwmaModeSuppresionPolicy(dstPeer, srcPeerIdx);
        }
        else if (BOOST_EWMA_MODESUP){
            int pktIdx = peerBoostEwmaModeSuppresionPolicy(dstPeer, srcPeerIdx);
        }
        else if (THMODESUP){
            int pktIdx = peerThresholdModeSuppresionPolicy(dstPeer, srcPeerIdx);
        }
        else if (BOOST_TH_MODESUP){
            boolean DONT_SELECT_SEED = false;
            int pktIdx = peerBoostedThresholdModeSuppresionPolicy( dstPeer, DONT_SELECT_SEED);
        }
        else if (BOOST_MODE_SUPPRESSION){
            boolean DONT_SELECT_SEED = false;
            int pktIdx = peerBoostedModeSuppresionPolicy( dstPeer, DONT_SELECT_SEED);
        }
        else if (CHAIN_POLICY){

            int pktIdx = peerChainPolicy(dstPeer, srcPeerIdx);
        }
        else if(DISTR_MODE_SUPPRESSION){
            boolean DONT_SELECT_SEED = false;
            int pktIdx = peerDistributedModeSuppression(dstPeer, DONT_SELECT_SEED);
        }
        else if(RARECHUNK){
            int pktIdx = peerFriedmanPolicy( dstPeer, false );
        }
        else if(COMMONCHUNK){
            int pktIdx = peerCommonChunkPolicy( dstPeer );
        }
        else if(STRICTLOCALMODE){
            boolean DONT_SELECT_SEED = false;
            int pltIdx = peerStrictLocalModeSuppression(dstPeer, DONT_SELECT_SEED);
        }
        else if(SUPPRRARECHUNK){
            restirctToOnePeer = true;
            int pktIdx = peerFriedmanPolicy( dstPeer, false );
            restirctToOnePeer = false;
        }
        else if(SUPPRLOCALMODE){
            restirctToOnePeer = true;
            int pltIdx = peerStrictLocalModeSuppression(dstPeer, false);
            restirctToOnePeer = false;

        }
        else if(SUPPRCOMMONCHUNK){
            restirctToOnePeer = true;
            int pltIdx = peerCommonChunkPolicy(dstPeer );
            restirctToOnePeer = false;

        }
        else if(BESTCASE){
            // always have all the packets
            seedRandomChunk(dstPeer);
        }
        else{
            System.out.println("Peer Chunk Policy Not found");
        }
        assertMetaVariables();
    }
    private void arrivalEvent(double timeEpoch){

        assertMetaVariables();
        updateTime(timeEpoch);
        Peer newPeer = new Peer(this.NumberOfChunks);
        this.ListPeers.add(newPeer);
        NumberOfPeers = NumberOfPeers +1;
        //update Distribution

        if(!USING_DICT) {
            bufferDistributionCount[0] += 1;
        }
        else {

            if (bufferDistributionCountDict.containsKey(Arrays.toString(Peer.NullBuffer))) {
                int prevCount = bufferDistributionCountDict.get(Arrays.toString(Peer.NullBuffer));
                bufferDistributionCountDict.put(Arrays.toString(Peer.NullBuffer), prevCount + 1);
            } else {
                bufferDistributionCountDict.put(Arrays.toString(Peer.NullBuffer), 1);
            }
        }

        PeerNumberOfPktsArray.get(0).add(newPeer);
        assertMetaVariables();
        writerNoPeers.print(NumberOfPeers+ ":");
        // write the number peers that contain a certain number of packets also
        for(ArrayList<Peer> peers: PeerNumberOfPktsArray){
           writerNoPeers.print(peers.size() + " ");
        }
        writerNoPeers.println();
        double []pdf = normalize();
        for(int k = 0; k< NumberOfChunks; k++){
            String prob = String.format("%.3f", pdf[k] );
            writerDistribution.print(prob+" ");
        }
        writerDistribution.println();
        if(!assertMetaVariables()){
            System.out.print("Assert Failed");
        }
//        for(int k=0; k < pow(2,NumberOfChunks)- 1; k++){
//            String dec_string = new String(dectoString(k));
//            if (bufferDistributionCountDict.containsKey(dec_string) ){
//                writerFullState.print(bufferDistributionCountDict.get(dec_string)+" ");
//            }
//            else{
//                writerFullState.print(0+" ");
//            }
//        }
//        writerFullState.println();


    }
    private void seedTransferEvent(double timeEpoch){
        // Seed gives a packet
        /* Peer Selection - Either random or Most Deprived*/

        // Peer Selection

        assertMetaVariables();
        updateTime(timeEpoch);
        int dstPeerIdx=0;
        Peer dstPeer = ListPeers.get(dstPeerIdx);
        if ( BOOST_EWMA_MODESUP|| EWMA_MODESUP || THMODESUP || GROUPSUPP || BOOST_GROUPSUPP || BESTCASE || RAREST || RANDOM || MODE_SUPPRESSION)   {
            dstPeerIdx = rand.nextInt(NumberOfPeers);
            dstPeer = ListPeers.get(dstPeerIdx);
        }
        else if(DISTR_MODE_SUPPRESSION || RARECHUNK || RELAXEDMODESUP ||COMMONCHUNK || BOOSTEDRAREST || STRICTLOCALMODE || BOOST_TH_MODESUP || BOOST_MODE_SUPPRESSION || SUPPRCOMMONCHUNK || SUPPRLOCALMODE || SUPPRRARECHUNK) {

            dstPeerIdx = rand.nextInt(NumberOfPeers);
            dstPeer = ListPeers.get(dstPeerIdx);
        }
        else if (GROUPSUPP || BOOST_GROUPSUPP){
            // Select a most deprived peer
            int k=0;
            for(k=0; k < NumberOfChunks +1; k++){
                if (PeerNumberOfPktsArray.get(k).size() >0){
                    break;
                }
            }
            ArrayList<Peer> deprivedList = PeerNumberOfPktsArray.get(k);
            int deprivedListIdx = rand.nextInt(deprivedList.size());
            dstPeer = deprivedList.get(deprivedListIdx);
        }
        else{
            System.out.println("No Peer Selection Policy");
        }

        boolean SELECT_SEED = true;
        // Piece Selection
        if (RANDOM || GROUPSUPP || CHAIN_POLICY || BOOST_GROUPSUPP) {
            int pktIdx = seedRandomChunk(dstPeer);
        }
        else if (RAREST) {
            int pktIdx = seedRarestChunk(dstPeer);
        }
        else if (MODE_SUPPRESSION){
            int pktIdx = seedModeSuppressionPolicy(dstPeer);
        }
        else if (EWMA_MODESUP){
            int pktIdx = peerEwmaModeSuppresionPolicy(dstPeer, -1);
        }
        else if (BOOST_EWMA_MODESUP){
            int pktIdx = peerBoostEwmaModeSuppresionPolicy(dstPeer, -1);
        }
        else if (THMODESUP){
            int pktIdx = peerThresholdModeSuppresionPolicy(dstPeer, -1);
        }
        else if (BOOST_MODE_SUPPRESSION){
            int pktIdx = peerBoostedModeSuppresionPolicy( dstPeer, SELECT_SEED);
        }
        else if (BOOST_TH_MODESUP){
            int pktIdx = peerBoostedThresholdModeSuppresionPolicy( dstPeer, SELECT_SEED);
        }
        else if (DISTR_MODE_SUPPRESSION){
            int pktIdx = peerDistributedModeSuppression(dstPeer, SELECT_SEED);
        }
        else if(RARECHUNK){
            int pktIdx = peerFriedmanPolicy( dstPeer, SELECT_SEED);
        }
        else if(COMMONCHUNK){
            int pktIdx = seedCommonChunkPolicy(dstPeer);
        }
        else if(STRICTLOCALMODE){
            int pktIdx = peerStrictLocalModeSuppression(dstPeer,SELECT_SEED);
        }
        else if(SUPPRRARECHUNK){
            restirctToOnePeer = true;
            int pktIdx = peerFriedmanPolicy( dstPeer, true);
            restirctToOnePeer = false;
        }
        else if(SUPPRLOCALMODE){
            restirctToOnePeer = true;
            int pltIdx = peerStrictLocalModeSuppression(dstPeer, true);
            restirctToOnePeer = false;

        }
        else if(SUPPRCOMMONCHUNK){
            restirctToOnePeer = true;
            int pltIdx = seedCommonChunkPolicy(dstPeer );
            restirctToOnePeer = false;

        }
        else if(BESTCASE){
            seedRandomChunk(dstPeer);
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
//        int secondPopular= 0;
//        int delta = 1000;
        int maxPopularity = 0;
        for(int j = 0; j < NumberOfChunks; j++){
            int value = marginalPktDistributionCount[j];
            if(maxPopularity < value){
//                secondPopular = maxPopularity;
                maxPopularity = value;
            }
        }
        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j = 0; j < NumberOfChunks; j++) {
            if (marginalPktDistributionCount[j] == maxPopularity){
//             if (marginalPktDistributionCount[j] >= secondPopular + delta){
                 // If the distribution is way abundant then only suppress
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfChunks){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i = 0; i< NumberOfChunks; i++){
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
        int pktIdx = -1;
        if (usefulPktIdx.size() >0){
            int randomIdx = rand.nextInt(usefulPktIdx.size());
            pktIdx = usefulPktIdx.get(randomIdx);
            dst[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
        return -1;

    }

    private int peerEwmaModeSuppresionPolicy(Peer dstPeer, int srcIdx){
        /*
        - Transfers a useful packet from src to destination
        - Returns index of the packet transmitted
        - If no packet is transmitted returns -1
        - Also update marginal and buffer distributions
        */
        int [] dst = dstPeer.Buffer;
        int [] src;
        if(srcIdx !=-1){
            src =  ListPeers.get(srcIdx).Buffer;
            dstPeer.updateMovingAvg(ListPeers.get(srcIdx));
//            ListPeers.get(srcIdx).updateMovingAvg(dstPeer);
        }
        else{
            src = Peer.FullBuffer;
        }

        //find most popular pktidx
        double maxPopularity = -1;
        double minPopularity = pow(10,5);
        for(int j = 0; j < NumberOfChunks; j++){
            double value = dstPeer.ewmaMarginalDistribution[j];
            if(maxPopularity < value){
                maxPopularity = value;
            }
            if(minPopularity > value){
                minPopularity = value;
            }
        }
        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j = 0; j < NumberOfChunks; j++) {
//            if ( (Math.round(dstPeer.ewmaMarginalDistribution[j]*1000))/1000 == Math.round(maxPopularity*1000)/1000 && minPopularity <= 0.0001){
//                mostPopularIdxList.add(j);
//            }
//            else if((Math.round(dstPeer.ewmaMarginalDistribution[j]*1000))/1000 == Math.round(maxPopularity*1000)/1000 && maxPopularity/minPopularity > 2){
//                mostPopularIdxList.add(j);
//            }
            if ( dstPeer.ewmaMarginalDistribution[j] == maxPopularity && minPopularity <= 0.0001){
                mostPopularIdxList.add(j);
            }
            else if(dstPeer.ewmaMarginalDistribution[j] == maxPopularity && maxPopularity/minPopularity > 2){
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfChunks){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i = 0; i< NumberOfChunks; i++){
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
        int pktIdx = -1;
        if (usefulPktIdx.size() >0){
            int randomIdx = rand.nextInt(usefulPktIdx.size());
            pktIdx = usefulPktIdx.get(randomIdx);
            dst[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;

    }
    private int peerBoostEwmaModeSuppresionPolicy(Peer dstPeer, int srcIdx){
        /*
        - Transfers a useful packet from src to destination
        - Returns index of the packet transmitted
        - If no packet is transmitted returns -1
        - Also update marginal and buffer distributions
        */

        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        if(srcIdx == -1){
            randomIdx1 = ListPeers.size();
        }
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) {
            buffer1 = ListPeers.get(randomIdx1).Buffer;
            dstPeer.updateMovingAvg(ListPeers.get(randomIdx1));
        }
        else buffer1 = Peer.FullBuffer;
        if (randomIdx2 < ListPeers.size()) {
            buffer2 = ListPeers.get(randomIdx2).Buffer;
            dstPeer.updateMovingAvg(ListPeers.get(randomIdx2));
        }
        else buffer2 = Peer.FullBuffer;
        if (randomIdx3 < ListPeers.size()) {
            buffer3 = ListPeers.get(randomIdx3).Buffer;
            dstPeer.updateMovingAvg(ListPeers.get(randomIdx3));
        }
        else buffer3 = Peer.FullBuffer;


        int [] dst = dstPeer.Buffer;
        //find most popular pktidx
        double maxPopularity = -1;
        double minPopularity = pow(10,5);
        for(int j = 0; j < NumberOfChunks; j++){
            double value = dstPeer.ewmaMarginalDistribution[j];
            if(maxPopularity < value){
                maxPopularity = value;
            }
            if(minPopularity > value){
                minPopularity = value;
            }
        }
        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j = 0; j < NumberOfChunks; j++) {
//            if ( (Math.round(dstPeer.ewmaMarginalDistribution[j]*1000))/1000 == Math.round(maxPopularity*1000)/1000 && minPopularity <= 0.00001){
//                mostPopularIdxList.add(j);
//            }
//            else if((Math.round(dstPeer.ewmaMarginalDistribution[j]*1000))/1000 == Math.round(maxPopularity*1000)/1000 && maxPopularity/minPopularity > 2){
//                mostPopularIdxList.add(j);
//            }
            if ( dstPeer.ewmaMarginalDistribution[j] == maxPopularity && minPopularity <= 0.0001){
                mostPopularIdxList.add(j);
            }
            else if(dstPeer.ewmaMarginalDistribution[j] == maxPopularity && maxPopularity/minPopularity > 2){
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfChunks){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i = 0; i< NumberOfChunks; i++){
            // Check if i is popular index
            boolean popularIdx = false;
            for(int mostPopularIdx: mostPopularIdxList){
                if (mostPopularIdx == i){
                    popularIdx = true;
                }
            }

            if(popularIdx) continue;
            else if ( (buffer1[i] + buffer2[i] + buffer3[i] >=1) &&  (dst[i] == 0) ){
                usefulPktIdx.add(i);
            }
        }
        // Select a random packet and transfer
        int pktIdx = -1;
        if (usefulPktIdx.size() >0){
            int randomIdx = rand.nextInt(usefulPktIdx.size());
            pktIdx = usefulPktIdx.get(randomIdx);
            dst[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;

    }
    private int peerThresholdModeSuppresionPolicy(Peer dstPeer, int srcIdx){
        /*
        - Transfers a useful packet from src to destination
        - Returns index of the packet transmitted
        - If no packet is transmitted returns -1
        - Also update marginal and buffer distributions
        */
        int THRESHOLD = THRESH;
        int [] dst = dstPeer.Buffer;

        int [] src;
        if(srcIdx == -1){
            src = Peer.FullBuffer;
        }
        else{

            src = ListPeers.get(srcIdx).Buffer;
        }
        //find most popular pktidx
//        int secondPopular= 0;
//        int delta = 1000;
        int maxPopularity = 0;
        int minPopularity =marginalPktDistributionCount[0] ;
        for(int j = 0; j < NumberOfChunks; j++){
            int value = marginalPktDistributionCount[j];
            if(maxPopularity < value){
//                secondPopular = maxPopularity;
                maxPopularity = value;
            }
            if(minPopularity > value){
                minPopularity  =value;
            }
        }
        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j = 0; j < NumberOfChunks; j++) {
            if ( (marginalPktDistributionCount[j] >= minPopularity + THRESHOLD)  && (marginalPktDistributionCount[j] == maxPopularity)  ) {
//             if (marginalPktDistributionCount[j] >= secondPopular + delta){
                // If the distribution is way abundant then only suppress
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfChunks){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i = 0; i< NumberOfChunks; i++){
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
        int pktIdx = -1;
        if (usefulPktIdx.size() >0){
            int randomIdx = rand.nextInt(usefulPktIdx.size());
            pktIdx = usefulPktIdx.get(randomIdx);
            dst[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
//            updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
//        updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
        return -1;

    }
    private int peerBoostedModeSuppresionPolicy(Peer dstPeer, boolean SEED_SELECTED){

        if(!assertMetaVariables()){
            System.out.print("Assert Error");
        }
        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        if(SEED_SELECTED){
            randomIdx1 = ListPeers.size();
        }
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = Peer.FullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = Peer.FullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = Peer.FullBuffer;


        int [] dst = dstPeer.Buffer;

        //find most popular pktidx
        int maxPopularity = 0;
        for(int j = 0; j < NumberOfChunks; j++){
            int value = marginalPktDistributionCount[j];
            if(maxPopularity < value){
                maxPopularity = value;
            }
        }
        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j = 0; j < NumberOfChunks; j++) {
            if (marginalPktDistributionCount[j] == maxPopularity){
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfChunks){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i = 0; i< NumberOfChunks; i++){
            // Check if i is popular index
            boolean popularIdx = false;
            for(int mostPopularIdx: mostPopularIdxList){
                if (mostPopularIdx == i){
                    popularIdx = true;
                }
            }

            if(popularIdx) continue;
            else if ( (buffer1[i] + buffer2[i]+ buffer3[i] >= 1) &&  (dst[i]==0) ) {
                usefulPktIdx.add(i);
            }
        }
        // Select a random packet and transfer
        int pktIdx = -1;
        if (usefulPktIdx.size() >0){
            int randomIdx = rand.nextInt(usefulPktIdx.size());
            pktIdx = usefulPktIdx.get(randomIdx);
            dst[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);

        boolean rr = assertMetaVariables();
        return -1;

    }

    private int peerBoostedThresholdModeSuppresionPolicy(Peer dstPeer, boolean SEED_SELECTED){
        if(!assertMetaVariables()){
            System.out.print("Assert Error");
        }
        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        if(SEED_SELECTED){
            randomIdx1 = ListPeers.size();
        }
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = Peer.FullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = Peer.FullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = Peer.FullBuffer;


        int Threshold = THRESH;
        int [] dst = dstPeer.Buffer;

        //find most popular pktidx
        int maxPopularity = 0;
        int minPopularity =marginalPktDistributionCount[0] ;
        for(int j = 0; j < NumberOfChunks; j++) {
            int value = marginalPktDistributionCount[j];
            if (maxPopularity < value) {
//                secondPopular = maxPopularity;
                maxPopularity = value;
            }
            if (minPopularity > value) {
                minPopularity = value;
            }
        }

        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j = 0; j < NumberOfChunks; j++) {
            if ( (marginalPktDistributionCount[j] > minPopularity + Threshold) && (marginalPktDistributionCount[j] == maxPopularity) ){
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfChunks){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i = 0; i< NumberOfChunks; i++){
            // Check if i is popular index
            boolean popularIdx = false;
            for(int mostPopularIdx: mostPopularIdxList){
                if (mostPopularIdx == i){
                    popularIdx = true;
                }
            }

            if(popularIdx) continue;
            else if ( (buffer1[i] + buffer2[i]+ buffer3[i] >= 1) &&  (dst[i]==0) ) {
                usefulPktIdx.add(i);
            }
        }
        // Select a random packet and transfer
        int pktIdx = -1;
        if (usefulPktIdx.size() >0){
            int randomIdx = rand.nextInt(usefulPktIdx.size());
            pktIdx = usefulPktIdx.get(randomIdx);
            dst[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);

        boolean rr = assertMetaVariables();
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
        int pktIdx = -1;
        if (usefulPktIdx.size() >0){
            int randomIdx = rand.nextInt(usefulPktIdx.size());
            pktIdx = usefulPktIdx.get(randomIdx);
            dst[pktIdx] = 1;
            // Check if dst Peer has received all the packets. If yes then remove the peer from system
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer);
            return pktIdx;
        }
        updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
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
        int largestBuffer = 1;
        int largestValue = 0;
        String largestBufferS = "";
        int largestValue2 = 0;

        // Ignore the group with no packets in finding the largest group
        if(!USING_DICT) {
            for (int i = 0; i < bufferDistributionCount.length; i++) {
                int newvalue = bufferDistributionCount[i];
                if (newvalue > largestValue) {
                    largestValue = newvalue;
                    largestBuffer = i;
                }
            }
            if(toDecimal(src) == largestBuffer){
            if (sumPackets(dst) < sumPackets(src)) {
                updateStarvationBuffer(ListPeers.get(srcIdx), dstPeer.Buffer, -1);
                return -1; // abandoning the transfer to avoid single club
            }
        }

        }
        else {
            for (String bufferCandidate : bufferDistributionCountDict.keySet()) {
                int count = bufferDistributionCountDict.get(bufferCandidate);
                if (count > largestValue2) {
                    largestBufferS = bufferCandidate;
                    largestValue2 = count;
                }

            }
//            if (largestBufferS.equals(Arrays.toString(src))) {
            if(bufferDistributionCountDict.get(Arrays.toString(src)) == largestValue2){
                if (sumPackets(dst) <= sumPackets(src)) {
                    updateStarvationBuffer(ListPeers.get(srcIdx), dstPeer.Buffer, -1);
                    return -1; // abandoning the transfer to avoid single club
                }
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
            updateMetaData(dstPeer,pktIdx);
            updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, -1);
        return -1;

    }
    private int peerChunkBoostGroupSuppression(Peer dstPeer, int srcIdx){
        /*
        - Here group Suppresion is implemented
        - Identify the (decimal) buffer state of largest group
        - If the src is in largest group then check if the dst has more pkts ow abadon
        */
        int [] dst = dstPeer.Buffer;
        int [] src = Peer.FullBuffer;
        if(srcIdx == -1){
            src = Peer.FullBuffer;
        }
        else{
             src = ListPeers.get(srcIdx).Buffer;
        }

        // Determine largestbuffer
        int largestBuffer = 1;
        int largestValue = 0;
        String largestBufferS = "";
        int largestValue2 = 0;
        // Ignore the group with no packets in finding the largest group

        if(!USING_DICT) {
            for (int i = 1; i < bufferDistributionCount.length; i++) {
                int newvalue = bufferDistributionCount[i];
                if (newvalue > largestValue) {
                    largestValue = newvalue;
                    largestBuffer = i;
                }
            }
            if (toDecimal(src) == largestBuffer) {
                if (sumPackets(dst) < sumPackets(src)) {
                    updateStarvationBuffer(ListPeers.get(srcIdx), dstPeer.Buffer, -1);
                    return -1; // abandoning the transfer to avoid single club
                }
            }
        }
        else {
            for (String bufferCandidate : bufferDistributionCountDict.keySet()) {
                int count = bufferDistributionCountDict.get(bufferCandidate);
                if (count > largestValue2) {
                    largestBufferS = bufferCandidate;
                    largestValue2 = count;
                }

            }
            if (largestBufferS.equals(Arrays.toString(src))) {
                if (sumPackets(dst) < sumPackets(src)) {
                    updateStarvationBuffer(ListPeers.get(srcIdx), dstPeer.Buffer, -1);
                    return -1; // abandoning the transfer to avoid single club
                }
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
            updateMetaData(dstPeer,pktIdx);
            updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, -1);
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
            updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, -1);
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
            updateMetaData(dstPeer,pktIdx);
            updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, -1);
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
            updateMetaData(dstPeer,rarestPktIdx);
            updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, rarestPktIdx);
            boolean removed = removePeer(dstPeer );
            return rarestPktIdx;
        }
        updateStarvationBuffer(ListPeers.get(srcIdx),dstPeer.Buffer, -1);
        return -1;

    }
    private int peerDistributedModeSuppression(Peer dstPeer, boolean SELECT_SEED){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */
        int [] fullBuffer = new int[NumberOfChunks];
        for(int k = 0; k< NumberOfChunks; k++){
            fullBuffer[k] = 1;
        }
        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        if(SELECT_SEED){
            randomIdx1 = ListPeers.size();
        }
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = fullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = fullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = fullBuffer;

        // Try rarechunk at s_0
        int pktIdx = 0;

        int noPktsDstPeer = sumPackets(dstPeer.Buffer);
        pktIdx = nonAbundentMatch(dstPeer,buffer1,buffer2,buffer3);

        // Select a random packet and transfer
        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            if (randomIdx1< ListPeers.size() ) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        if (randomIdx1< ListPeers.size() )  updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, -1);
        return -1;
    }
    private int peerFriedmanPolicy(Peer dstPeer, boolean seedCall ){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */
        int [] fullBuffer = new int[NumberOfChunks];
        for(int k = 0; k< NumberOfChunks; k++){
            fullBuffer[k] = 1;
        }

        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        if(seedCall){
            randomIdx1 = ListPeers.size();
        }
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
            updateMetaData(dstPeer,pktIdx);
            if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
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
        else if(noPktsDstPeer < NumberOfChunks -1){
            // Random pkt
            pktIdx = randomMatch(dstPeer, buffer1);

        }
        else if(noPktsDstPeer == NumberOfChunks -1){
            // Check if all other pkts are available abundently (atleast 2 times)
            pktIdx = conditionalMatch(dstPeer, buffer1, buffer2, buffer3);

        }

        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
        return -1;

    }
    private int peerStrictLocalModeSuppression(Peer dstPeer, boolean SELECT_SEED){
        /*
        - Randomly samples three users and tries to estimate mu
        - The seed also included in the sampling list
        */

        int randomIdx1 = rand.nextInt(ListPeers.size() +1);
        if(SELECT_SEED){
            randomIdx1 = ListPeers.size();
        }
        int randomIdx2 = rand.nextInt(ListPeers.size()+1);
        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
        int [] buffer1, buffer2, buffer3;
        if (randomIdx1 < ListPeers.size()) buffer1 = ListPeers.get(randomIdx1).Buffer;
        else buffer1 = Peer.FullBuffer;
        if (randomIdx2 < ListPeers.size()) buffer2 = ListPeers.get(randomIdx2).Buffer;
        else buffer2 = Peer.FullBuffer;
        if (randomIdx3 < ListPeers.size()) buffer3 = ListPeers.get(randomIdx3).Buffer;
        else buffer3 = Peer.FullBuffer;


        int pktIdx = striclLocalModeMatch(dstPeer, buffer1, buffer2 , buffer3);
        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            updateMetaData(dstPeer,pktIdx); // update distribution
            if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        if(randomIdx1 < ListPeers.size()) updateStarvationBuffer(ListPeers.get(randomIdx1),dstPeer.Buffer, pktIdx);
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
            updateMetaData(dstPeer,pktIdx);
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
            updateMetaData(dstPeer,rarestPktIdx);
            boolean removed = removePeer(dstPeer );
            return rarestPktIdx;
        }
        return -1;

    }
    private int seedModeSuppressionPolicy(Peer dstPeer){
        /*
        src contains all elements
        */
        int [] dst = dstPeer.Buffer;
        int[] src = Peer.FullBuffer;

        //find most popular pktidx
        int maxPopularity = 0;
        for(int j = 0; j < NumberOfChunks; j++){
            int value = marginalPktDistributionCount[j];
            if(maxPopularity < value){
                maxPopularity = value;
            }
        }
        ArrayList<Integer> mostPopularIdxList = new ArrayList<Integer>();
        for(int j = 0; j < NumberOfChunks; j++) {
            if (marginalPktDistributionCount[j] == maxPopularity){
                mostPopularIdxList.add(j);
            }

        }
        if (mostPopularIdxList.size() == NumberOfChunks){
            // Uniform Distribution
            mostPopularIdxList = new ArrayList<Integer>();
        }

        ArrayList<Integer> usefulPktIdx = new ArrayList<Integer>();

        // Find list of useful packets
        for(int i = 0; i< NumberOfChunks; i++){
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
            updateMetaData(dstPeer,pktIdx);
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

        int randomIdx1 = ListPeers.size();
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
        else if(noPktsDstPeer < NumberOfChunks -1){
            // Random pkt
            pktIdx = randomMatch(dstPeer, buffer1);

        }
        else if(noPktsDstPeer == NumberOfChunks -1){
            // Check if all other pkts are available abundently (atleast 2 times)
            pktIdx = conditionalMatch(dstPeer, buffer1, buffer2, buffer3);

        }

        if (pktIdx != -1){
            dstPeer.Buffer[pktIdx] = 1;
            // update distribution
            updateMetaData(dstPeer,pktIdx);
            boolean removed = removePeer(dstPeer );
            return pktIdx;
        }
        return -1;

    }


    private boolean removePeer(Peer dstPeer){

        // Returns true if peer has all the packets and removed, else returns false
        double []pdf = normalize();

        writerNoPeers.print(NumberOfPeers+ ":");
        // write the number peers that contain a certain number of packets also
        for(ArrayList<Peer> peers: PeerNumberOfPktsArray){
            writerNoPeers.print(peers.size() + " ");
        }
        writerNoPeers.println();
        for(int k = 0; k< NumberOfChunks; k++){
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
            for(int i = 0; i < NumberOfChunks; i++){
                marginalPktDistributionCount[i] = marginalPktDistributionCount[i] -1;
            }


            if(!USING_DICT){

                bufferDistributionCount[toDecimal(Peer.FullBuffer)] = bufferDistributionCount[toDecimal(Peer.FullBuffer)] -1;
            }
            else{

                if(bufferDistributionCountDict.containsKey(Arrays.toString(Peer.FullBuffer))){
                    int prevCount = bufferDistributionCountDict.get(Arrays.toString(Peer.FullBuffer) );
                    bufferDistributionCountDict.put(Arrays.toString(Peer.FullBuffer),prevCount-1);
                    if(prevCount-1 == 0){
                       bufferDistributionCountDict.remove(Arrays.toString(Peer.FullBuffer));  // Why are we removing FullBuffer???? Why?????????
                    }

                }
            }


            PeerNumberOfPktsArray.get(NumberOfChunks).remove(dstPeer);
            writerTime.println(dstPeer.time);
            writerStarvations.println(dstPeer.suppressionArray[0] + " "+dstPeer.suppressionArray[1] + " "+ dstPeer.suppressionArray[2] + " "+dstPeer.suppressionArray[3] );

//            if(NumberOfChunks ==3){
//                int D_i = bufferDistributionCount[toDecimal(D_i_array1)] ;
//                writerCounts.print("T_bar_i:"+T_i_bar+" S_i:"+S_i+" S_0:"+ S_zero + " D_bar_i:"+D_i+"\n");
//            }


            return true; // Peer is removed
        }
        return false; // Peer is not removed
    }
    /* Metadata Update Function */
    private void updateMetaData(Peer dstPeer, int pktIdx){
        int[] tempBuffer = dstPeer.Buffer;

        // Check if the dstPeer is preset first
        if (!PeerNumberOfPktsArray.get(sumPackets(tempBuffer) - 1).contains(dstPeer) ){
            System.out.println("Dst Peer doesn't exists to delete");
        }
        PeerNumberOfPktsArray.get(sumPackets(tempBuffer) - 1).remove(dstPeer);
        PeerNumberOfPktsArray.get(sumPackets(tempBuffer)).add(dstPeer);


        int[] dstCopy = new int[NumberOfChunks];
        for(int i=0;i < NumberOfChunks;i++){
            dstCopy[i] = dstPeer.Buffer[i];
        }
        // update distribution
        marginalPktDistributionCount[pktIdx] = (marginalPktDistributionCount[pktIdx] +1);


        if(!USING_DICT){
            int dstDecimalValue = toDecimal(dstCopy);
            bufferDistributionCount[dstDecimalValue] = (bufferDistributionCount[dstDecimalValue] +1 );
            dstDecimalValue = dstDecimalValue - (int)pow(2,pktIdx);
            bufferDistributionCount[dstDecimalValue] = (bufferDistributionCount[dstDecimalValue] -1 );

        }
        else{
            if(bufferDistributionCountDict.containsKey(Arrays.toString(dstCopy))){
                int prevCount = bufferDistributionCountDict.get(Arrays.toString(dstCopy) );
                bufferDistributionCountDict.put(Arrays.toString(dstCopy),prevCount+1);
            }
            else{
                bufferDistributionCountDict.put(Arrays.toString(dstCopy),1);

            }
            dstCopy[pktIdx] = 0; // Buffer before adding pktIdx
            if(bufferDistributionCountDict.containsKey(Arrays.toString(dstCopy))){
                int prevCount = bufferDistributionCountDict.get(Arrays.toString(dstCopy) );
                bufferDistributionCountDict.put(Arrays.toString(dstCopy),prevCount-1);
                if(prevCount-1 == 0){
                    bufferDistributionCountDict.remove(Arrays.toString(dstCopy));  // Why are we removing FullBuffer???? Why?????????
                }
            }
            else{
                System.out.println(" Error in bufferDistributionCount HashMap");
            }
        }

    }

    /* Utility Function */
    private int sumPackets(int [] buffer){
        int sum = 0;
        for(int i = 0; i< NumberOfChunks; i++){
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
        int sum1 = 0;
        int sum = 0;

        if(!USING_DICT){

            for(int count:bufferDistributionCount){
                if(count < 0) {
                    System.out.println("Assert Failed: Buffer Distribution Count negative");
                }
                sum = sum + count;
            }
            if (sum != NumberOfPeers ){
                System.out.println("Assert Failed: Buffer Distribution Sum");
                return false;
            }
        }

        else {
            for (int count : bufferDistributionCountDict.values()) {
                if (count < 0) {
                    System.out.println("Assert Failed: Buffer Distribution Count negative");
                }
                sum1 = sum1 + count;
            }
            if (sum1 != NumberOfPeers) {
                System.out.println("Assert Failed: Buffer Distribution Sum");
                return false;
            }

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
        double[] pdf = new double[NumberOfChunks];
        for(int i = 0; i < NumberOfChunks; i++){
            pdf[i] = (double) marginalPktDistributionCount[i]/NumberOfPeers;
        }
        return pdf;

    }
    private void updateTime(double timeEpoch){
        for(Peer eachPeer: ListPeers){
            eachPeer.time = eachPeer.time+ timeEpoch;
        }
    }
    private String dectoString(int decimalValue){

        int[] buffer = new int[NumberOfChunks];

        int q = decimalValue;
        for(int i=0; i< NumberOfChunks;i++){
            int r = q%2;
            q = q/2;
            buffer[i] = r;
        }

        String bufferString= new String();
        bufferString = Arrays.toString(buffer);
        return bufferString;

    }

    /* Matches in Distributed Algorithms */
    private int rareMatch(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){
        ArrayList<Integer> rareIndicies = new ArrayList<Integer>();
        for(int i = 0; i < NumberOfChunks; i++){
            if(dstPeer.Buffer[i] == 0){
                int count = buffer1[i] + buffer2[i] + buffer3[i];
                if (count ==1) {
                    if (restirctToOnePeer){
                       // Only downloads from buffer1
                        if(buffer1[i]==1) rareIndicies.add(i);
                    }
                    else{
                        rareIndicies.add(i);
                    }
                }
            }
        }
        if (rareIndicies.size() >0){

            int pktIdx = rareIndicies.get(rand.nextInt(rareIndicies.size()));
            return pktIdx;
        }
        else return -1;

    }
    private ArrayList<Integer> rareMatchArray(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){
        ArrayList<Integer> rareIndicies = new ArrayList<Integer>();
        for(int i = 0; i < NumberOfChunks; i++){
            if(dstPeer.Buffer[i] == 0){
                int count = buffer1[i] + buffer2[i] + buffer3[i];
                if (count ==1) {
                    if (restirctToOnePeer){
                        // Only downloads from buffer1
                        if(buffer1[i]==1) rareIndicies.add(i);
                    }
                    else{
                        rareIndicies.add(i);
                    }
                }
            }
        }

        return rareIndicies;
    }
    private int nonAbundentMatch(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){
        ArrayList<Integer> rareIndicies = new ArrayList<Integer>();

        // Find the marginal counts
        int[] localMarginalCounts = new int[NumberOfChunks];
        int maxCount = 0;
        for(int i=0; i<NumberOfChunks;i++){
            localMarginalCounts[i] = buffer1[i] + buffer2[i] + buffer3[i];
            if (maxCount < localMarginalCounts[i] ){
                maxCount = localMarginalCounts[i];
            }
        }

        for(int i = 0; i < NumberOfChunks; i++){
            if(dstPeer.Buffer[i] == 0){
                int count = localMarginalCounts[i];
                if (count >0) {
                    // There is a chunk to give
                    if(count !=3 ) {
                    // Should not selected popular packet
                        if (restirctToOnePeer) {
                            // Only downloads from buffer1
                            if (buffer1[i] == 1) rareIndicies.add(i);
                        } else {
                            rareIndicies.add(i);
                        }
                    }
                }
            }
        }

        if (rareIndicies.size() >0){
            int randValue = rand.nextInt(rareIndicies.size());
            int pktIdx = rareIndicies.get(randValue);
            return pktIdx;
        }
        else return -1;

    }
    private int conditionalMatch(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){
        int pktIdx = -1;
        for(int i = 0; i < NumberOfChunks; i++){
            if(dstPeer.Buffer[i] == 0){
                pktIdx = i;
                int count = buffer1[i] + buffer2[i] + buffer3[i];
                if (count ==0) return -1;
                if(restirctToOnePeer){
                    // only download from buffer1
                    if (buffer1[i] ==0 ) return -1;
                }
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
        for(int i = 0; i < NumberOfChunks; i++){
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
    private int randomMatchWith3(Peer dstPeer, int[] buffer1, int[] buffer2, int[] buffer3){
        ArrayList<Integer> usefulIndicies = new ArrayList<Integer>();
        for(int i = 0; i < NumberOfChunks; i++){
            if(dstPeer.Buffer[i] == 0){
                if (buffer1[i] + buffer2[i] + buffer3[i] >= 1) {
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
    private void updateStarvationBuffer(Peer srcPeer, int[] dstBuffer,int pktIdx){

        if(pktIdx == -1) {
            srcPeer.suppressionArray[0] = srcPeer.suppressionArray[0] + 1; // # times selected
            if (sumPackets(dstBuffer) < sumPackets(srcPeer.Buffer)) {
                srcPeer.suppressionArray[3] = srcPeer.suppressionArray[3] + 1; // # times suppression took place
            } else {
                srcPeer.suppressionArray[2] = srcPeer.suppressionArray[2] + 1; // # times pkt is not available
            }
        }
        else{
            srcPeer.suppressionArray[0] = srcPeer.suppressionArray[0] + 1; // # times selected
            srcPeer.suppressionArray[1] = srcPeer.suppressionArray[1] + 1; // # times pkt is transfered

        }
    }
    private int striclLocalModeMatch(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){

//        int [] buffer4 = new int[NumberOfChunks];
//        int randomIdx3 = rand.nextInt(ListPeers.size()+1);
//        if (randomIdx3 < ListPeers.size()) buffer4 = ListPeers.get(randomIdx3).Buffer;
////        buffer4 = Peer.NullBuffer;
//        buffer3 = Peer.NullBuffer;

        // Find the marginal counts
        int[] localMarginalCounts = new int[NumberOfChunks];
        int maxCount = 0;
        for(int i=0; i<NumberOfChunks;i++){
            localMarginalCounts[i] = buffer1[i] + buffer2[i] + buffer3[i];
            if (maxCount < localMarginalCounts[i] ){
                maxCount = localMarginalCounts[i];
            }
        }

        // Check if all chunks are equally popular
        boolean uniformDist= false;
        if(maxCount >= 1 && (sumPackets(localMarginalCounts) == maxCount *NumberOfChunks) ) {
            uniformDist = true;
        }

        ArrayList<Integer> possilbeCandidates = new ArrayList<>();
        for(int k = 0;k < NumberOfChunks;k++){
            if( (dstPeer.Buffer[k] == 0) && (localMarginalCounts[k] >0) ) {
                // Then only u have piece to transfer
                if (uniformDist) {
                    if(restirctToOnePeer){
                        if(buffer1[k] ==1) possilbeCandidates.add(k);
                    }
                    else{
                        possilbeCandidates.add(k);
                    }
                } else if (maxCount <=1){
                    if(restirctToOnePeer){
                       if(buffer1[k] ==1) possilbeCandidates.add(k);
                    }
                    else{
                        possilbeCandidates.add(k);
                    }

                } else if (localMarginalCounts[k] != maxCount) {
                    if(restirctToOnePeer){
                        if(buffer1[k] ==1) possilbeCandidates.add(k);
                    }
                    else{
                        possilbeCandidates.add(k);
                    }
                }
            }
        }

        int pktIdx  = -1;
        if(possilbeCandidates.size()>0){
            // Select a random packet from possible Candidates
            pktIdx = possilbeCandidates.get(rand.nextInt(possilbeCandidates.size()));
            if(pktIdx ==1){
                int kkkk = 0;
            }
        }
        return pktIdx;
    }
    private ArrayList<Integer> striclLocalModeArray(Peer dstPeer, int[] buffer1, int[] buffer2 , int[] buffer3){

        // Find the marginal counts
        int[] localMarginalCounts = new int[NumberOfChunks];
        int maxCount = 0;
        for(int i=0; i<NumberOfChunks;i++){
            localMarginalCounts[i] = buffer1[i] + buffer2[i] + buffer3[i];
            if (maxCount < localMarginalCounts[i] ){
                maxCount = localMarginalCounts[i];
            }
        }

        // Check if all chunks are equally popular
        boolean uniformDist= false;
        if(maxCount >= 1 && (sumPackets(localMarginalCounts) == maxCount *NumberOfChunks) ) {
            uniformDist = true;
        }

        ArrayList<Integer> possilbeCandidates = new ArrayList<>();
        for(int k = 0;k < NumberOfChunks;k++){
            if( (dstPeer.Buffer[k] == 0) && (localMarginalCounts[k] >0) ) {
                // Then only u have piece to transfer
                if (uniformDist) {
                    possilbeCandidates.add(k);
                } else if (localMarginalCounts[k] != maxCount) {
                    possilbeCandidates.add(k);
                }
            }
        }
        // Test so that it is exactly equal to Rarechunk case when m=2
        if(localMarginalCounts[0]==2&&localMarginalCounts[1]==2){
            possilbeCandidates = new ArrayList<>();
        }

        return possilbeCandidates;
    }
    private void testMethods(){
        Peer testPeer = new Peer(this.NumberOfChunks);
        for(int i=0;i < NumberOfChunks;i++){
            testPeer.Buffer[i] = 0;
        }
        int[] buffer1 = {0,1};
        int[] buffer2 = {1,0};
        int[] buffer3 = {1,1};
        int pktIdxSelected = nonAbundentMatch(testPeer,buffer1,buffer2,buffer3);
        pktIdxSelected = striclLocalModeMatch(testPeer,buffer1,buffer2,buffer3);
        pktIdxSelected = rareMatch(testPeer,buffer1,buffer2,buffer3);

        pktIdxSelected = nonAbundentMatch(testPeer,buffer1,buffer2,buffer3);
        pktIdxSelected = striclLocalModeMatch(testPeer,buffer1,buffer2,buffer3);

        int[] fullbuffer = {1,1,1,1,1};
        pktIdxSelected = nonAbundentMatch(testPeer,fullbuffer,fullbuffer,fullbuffer);
        pktIdxSelected = striclLocalModeMatch(testPeer,fullbuffer,fullbuffer,fullbuffer);



        int[] buffer4 = {0,1,1,1,1};
        int[] buffer5 = {0,0,0,0,0};
        testPeer.Buffer[0] = 1;
        pktIdxSelected = nonAbundentMatch(testPeer,buffer4,buffer4,buffer5);
        pktIdxSelected = striclLocalModeMatch(testPeer,buffer4,buffer4,buffer5);

        buffer1[4] = 0;
        pktIdxSelected = nonAbundentMatch(testPeer,buffer1,fullbuffer,fullbuffer);
        pktIdxSelected = striclLocalModeMatch(testPeer,buffer1,fullbuffer,fullbuffer);


    }
}
