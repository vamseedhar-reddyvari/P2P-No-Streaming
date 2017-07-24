import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
import seaborn as sns
sns.set(style="whitegrid")
ewma = pd.stats.moments.ewma

FOLDER_NAME = r"../"

def read_wait_times(file_name):
    waiting_times = []
    for each_line in open(file_name):
        each_line = each_line.strip("\n")
        waiting_times.append(float(each_line))
    return waiting_times
def read_no_pkts(filename):
    file_ptr = open(FOLDER_NAME+filename,'r')
    peer_evolution =[]
    for line in file_ptr:
        value = int(line.strip('\n '))
        peer_evolution.append(value)
    return peer_evolution

BUFFEREVOL = True
if(BUFFEREVOL):
    list2= ["Rarest-distribution.txt",  "FD-distribution.txt",  "GS-distribution.txt", "MS-distribution.txt"]
    labels2 =[ r"Rarest", r"Friedman", r"Group Suppr", r"Mode Suppr"]

    peer_evolution = []
    for file_name in list2:
        peer_evolution.append(pd.read_csv(FOLDER_NAME+file_name,'\n'))

    fig,((ax1,ax2),(ax3,ax4))= plt.subplots(2,2,figsize=(14,6))
    axes = [ax1,ax2,ax3,ax4]
    colors=['red','blue','green','black']

    for i in range(4):
        names_array = list(["col"+str(x) for x in range(1,11)])
        buffer_df = pd.read_csv(FOLDER_NAME+list2[i],sep='\s+',names=names_array)
        plt.hold(True)
        ax = axes[i]
        ax.grid(True)
        ax.set_xlabel("Time")
        for elem in names_array:
            ax.plot(pd.ewma(buffer_df[elem],halflife=1))
        ax.set_ylabel("Bufer Marginal Distribution")
        ax.set_xlim([0,10000])
        ax.set_ylim([-0.1,1.1])
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        ax.text(0.5, 0.95, labels2[i], transform=ax.transAxes, fontsize=14, verticalalignment='top', bbox=props)
        # ax.legend(loc=2)

    plt.savefig("buffer_evolution.pdf",dpi=300);




NOPKTS = True
if (NOPKTS):

    # list2 = ["Rarest-Us-2-lambda-1.9.txt" ,"Rarest-Us-2-lambda-4.txt" , "Rarest-Us-2-lambda-50.txt" ,"Rarest-Us-2-lambda-100.txt", ]
    # labels2 =[ r"$U_s=2, \lambda=1.9$", r"$U_s=2, \lambda=4$", r"$U_s=2, \lambda=50$", r"$U_s=2, \lambda=100$"]

    list2= ["Rarest-peers.txt",  "FD-peers.txt",  "GS-peers.txt", "MS-peers.txt"]
    labels2 =[ r"Rarest", r"Friedman", r"Group Suppr", r"Mode Suppr"]

    peer_evolution = []
    for file_name in list2:
        peer_evolution.append(pd.read_csv(FOLDER_NAME+file_name,'\n'))

    fig,((ax1,ax2),(ax3,ax4))= plt.subplots(2,2,figsize=(14,6))
    axes = [ax1,ax2,ax3,ax4]
    colors=['red','blue','green','black']

    for i in range(4):
        axes[i].plot(peer_evolution[i].values,'-',color=colors[i],alpha=0.7,label=labels2[i])
        ax = axes[i]
        ax.grid(True)
        ax.set_xlabel("Time")
        # ax.legend(loc=2)
        ax.set_ylabel("Number of Peers")
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        ax.text(0.5, 0.95, labels2[i], transform=ax.transAxes, fontsize=14, verticalalignment='top', bbox=props)

    plt.suptitle('(m=10)',size=18)
    plt.savefig("peer-evolution.pdf",dpi=300)


SOJURNTIME = True
if(SOJURNTIME):
    file_name = "../waitingTime-rarest.txt"
    waiting_time_rarest= read_wait_times(file_name)

    file_name = "../waitingTime-random.txt"
    waiting_time_random  = read_wait_times(file_name)
    file_name = "../waitingTime-ms.txt"
    waiting_time_mode_suppression= read_wait_times(file_name)

    file_name = "../waitingTime-gs.txt"
    waiting_time_gs= read_wait_times(file_name)

    file_name = "../waitingTime-fd.txt"
    waiting_time_fd= read_wait_times(file_name)

    file_name = "../waitingTime-dms.txt"
    waiting_time_dms= read_wait_times(file_name)
    fig,ax= plt.subplots(1,1,figsize=(14,6))
    # ax.plot(ewma(np.array(waiting_time_random) ,span=2),'r-',label="Rarest")
    ax.plot(ewma(np.array(waiting_time_mode_suppression) ,span=20),'b-',label="Mode Suppression", alpha=0.3)
    # ax.plot(ewma(np.array(waiting_time_gs) ,span=20),'g-',label="Group Suppression")
    ax.plot(ewma(np.array(waiting_time_fd) ,span=20),'k-',label="Forced Friedman",alpha=0.5)
    ax.plot(ewma(np.array(waiting_time_dms) ,span=20),'r-',label="Distributed MS")
    ax.legend(loc=1)
    ax.set(xlabel='time',ylabel="Sojourn Time")
    plt.savefig("waiting_times.pdf",dpi=300)
