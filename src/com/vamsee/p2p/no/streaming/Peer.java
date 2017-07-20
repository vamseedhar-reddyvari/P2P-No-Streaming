package com.vamsee.p2p.no.streaming;

import java.util.Arrays;

/**
 * Created by Vamsee on 6/7/17.
 * This is a class for Peer. Contains the status of the buffer array.
 */
public class Peer {
    public static int NumberOfPieces = 2;
    public static int[] FullBuffer = new int[NumberOfPieces];
    public int[] Buffer;
    public Peer(){
        Arrays.fill(FullBuffer,1);
        Buffer = new int[NumberOfPieces];
        for(int i = 0; i< NumberOfPieces; i++){
            Buffer[i] = 0;
        }
    }

}
