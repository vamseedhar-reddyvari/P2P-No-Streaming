package com.vamsee.p2p.no.streaming;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws IOException {
        // write your code here
        double U = 1;
//        double lambda = 4;
        double mu = U;
        String outputDir = args[0];
        int[] seedsList = {10,2000,4000,4234,43204};
//        int[] seedsList = {10,2000,4000,4234};
        int[] FileLengths= {2,5,10,15, 25,30,  40};
//        int[] FileLengths = {10};
//        int[] listThresholds = {1,2,5,6,10,15,25,40};
        HashMap<Integer, Integer> optimalThresholds = new HashMap<Integer, Integer>();

        int[] listLambdas = {30};

        for (int randomSeed : seedsList) {
        for (int lambda : listLambdas) {
//        for (int thresh : listThresholds) {
            for (int numberOfPieces : FileLengths) {
                    P2PSimulation simulation = new P2PSimulation(U, lambda, mu, outputDir, randomSeed, numberOfPieces, optimalThresholds);
            String[] policyList = { "GroupSup","BoostGroupSup", "ModeSup", "BoostModeSup", "StrictLocalMode",   "SupprLocalMode",  "RareChunk",  "SupprRareChunk", "CommonChunk", "SupprCommonChunk", "ThModeSup", "BoostThMode"};
//                String[] policyList = {   "RareChunk",  "SupprRareChunk", "CommonChunk", "SupprCommonChunk", "SupprLocalMode"};
//            String[] policyList = {  "GroupSup", "ModeSup", "CommonChunk", "DistrModeSup", "Rarest" ,"Random" };
//            String[] policyList = { "ThModeSup", "BoostThMode"  };

                    for (String policy : policyList) {
                        simulation.Run(policy);
                    }
                }
            }
        }
    }
//    }
}
