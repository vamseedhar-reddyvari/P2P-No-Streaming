package com.vamsee.p2p.no.streaming;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        double Us = 1;
        double lambda = 4;
        double mu= 1;
        P2PSimulation simulation = new P2PSimulation(Us, lambda, mu);
        simulation.Run();
    }
}
