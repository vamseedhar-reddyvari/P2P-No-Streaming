package com.vamsee.p2p.no.streaming;

import java.util.Arrays;

/**
 * Created by Vamsee on 6/7/17.
 * This is a class for Peer. Contains the status of the buffer array.
 */
public class Peer {
    public static int NumberOfPieces = 10;
    public static int[] FullBuffer = new int[NumberOfPieces];
    public int[] Buffer;
    public double time;
    public Peer(boolean OneClub){
        Arrays.fill(FullBuffer,1);
        Buffer = new int[NumberOfPieces];
        for(int i = 0; i< NumberOfPieces; i++){
            if(OneClub){
                Buffer[i] = 1;
            }
            else{
                Buffer[i] = 0;
            }
        }
        Buffer[0] = 0;
        time = 0;
    }
    public Peer(){
        Arrays.fill(FullBuffer,1);
        Buffer = new int[NumberOfPieces];
        for(int i = 0; i< NumberOfPieces; i++){
            Buffer[i] = 0;
        }
        Buffer[0] = 0;
        time = 0;
    }

}
