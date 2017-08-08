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
        String[] policyList = { "RelaxedModeSup","Random", "Rarest", "Friedman", "ModeSup", "DistrModeSup", "CommonChunk", "GroupSup"};
//        String[] policyList = { "Friedman", "ModeSup", "DistrModeSup", "CommonChunk", "GroupSup"};
        for(String policy : policyList){
            simulation.Run(policy);
        }
    }
}
