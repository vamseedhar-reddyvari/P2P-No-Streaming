package com.vamsee.p2p.no.streaming;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        double Us = 2;
        double lambda = 2.5;
        double lambda_p = 1;
        P2PSimulation simulation = new P2PSimulation(Us, lambda, lambda_p);
        simulation.Run();
    }
}
