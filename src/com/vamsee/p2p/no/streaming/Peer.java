package com.vamsee.p2p.no.streaming;

import java.util.Arrays;

/**
 * Created by Vamsee on 6/7/17.
 * This is a class for Peer. Contains the status of the buffer array.
 */
public class Peer {
    public static int NoPackets = 10;
    public static int[] FullBuffer = new int[NoPackets];
    public int[] Buffer;
    public Peer(){
        Arrays.fill(FullBuffer,1);
        Buffer = new int[NoPackets];
        for(int i=0; i<NoPackets;i++){
            Buffer[i] = 0;
        }
    }

}
