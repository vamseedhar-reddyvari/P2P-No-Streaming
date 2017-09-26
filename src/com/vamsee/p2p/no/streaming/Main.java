package com.vamsee.p2p.no.streaming;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        double U = 1;
        double lambda = 4;
        double mu= U;
        String outputDir = args[0];
        P2PSimulation simulation = new P2PSimulation(U, lambda, mu, outputDir);
//        String[] policyList = { "StrictLocalMode","BoostedRarest","Random",  "Friedman", "ModeSup",  "CommonChunk", "GroupSup"};
        String[] policyList = { "StrictLocalMode", "Friedman",  "GroupSup", "ModeSup", "CommonChunk", "DistrModeSup", "Rarest" ,"Random" };
//        String[] policyList = { };
        for(String policy : policyList){
            simulation.Run(policy);
        }
    }
}
