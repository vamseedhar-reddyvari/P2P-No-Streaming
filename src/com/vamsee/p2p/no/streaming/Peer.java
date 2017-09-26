package com.vamsee.p2p.no.streaming;

import java.util.Arrays;

/**
 * Created by Vamsee on 6/7/17.
 * This is a class for Peer. Contains the status of the buffer array.
 */
public class Peer {
    public static int NumberOfPieces = 3;
    public static int[] FullBuffer = new int[NumberOfPieces];
    public static int[] NullBuffer = new int[NumberOfPieces];
    public int[] Buffer;
    public double time;

    /** Array of size 4
     * 1. # times this peer is selected as server
     * 2. # times a pkt is transfered
     * 3. # times a pkt is not available to transfer
     * 4. # times a pkt is available but not transfer due to policy
     */
    public int[] suppressionArray;


    /** Constructors
     *  - one for oneClub
     *  - other for empty peers
     */
    public Peer(boolean OneClub){
        Arrays.fill(FullBuffer,1);
        Arrays.fill(NullBuffer,0);
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

        suppressionArray = new int[4];
        Arrays.fill(suppressionArray,0);
    }
    public Peer(){
        Arrays.fill(FullBuffer,1);
        Arrays.fill(NullBuffer,0);
        Buffer = new int[NumberOfPieces];
        for(int i = 0; i< NumberOfPieces; i++){
            Buffer[i] = 0;
        }
        Buffer[0] = 0;
        time = 0;
        suppressionArray = new int[4];
        Arrays.fill(suppressionArray,0);
    }

}
