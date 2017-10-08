package com.vamsee.p2p.no.streaming;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        double U = 1;
        double lambda = 4;
        double mu= U;
        String outputDir = args[0];
        int[] seedsList = {10,2000,4000,4234,43204,43924,391,28392,45,789};
        for(int randomSeed: seedsList){
            P2PSimulation simulation = new P2PSimulation(U, lambda, mu, outputDir, randomSeed);
            String[] policyList = { "GroupSup","BoostGroupSup", "ModeSup", "BoostModeSup", "StrictLocalMode",   "SupprLocalMode",  "RareChunk",  "SupprRareChunk", "CommonChunk", "SupprCommonChunk"};
            //String[] policyList = { "StrictLocalMode", "Friedman",  "GroupSup", "ModeSup", "CommonChunk", "DistrModeSup", "Rarest" ,"Random" };
            //String[] policyList = {  "SupprRareChunk"};
            for(String policy : policyList){
                simulation.Run(policy);
            }
        }
    }
}
