import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
import seaborn as sns
sns.set(style="whitegrid")

FOLDER_NAME = r"../"

def read_no_pkts(filename):
    file_ptr = open(FOLDER_NAME+filename,'r')
    peer_evolution =[]
    for line in file_ptr:
        value = int(line.strip('\n '))
        peer_evolution.append(value)
    return peer_evolution

BUFFEREVOL = True
if(BUFFEREVOL):
    names_array = list(["col"+str(x) for x in range(1,11)])
    buffer_df = pd.read_csv(FOLDER_NAME+"Rarest-Us-2-lambda-4-distribution.txt",sep='\s+',names=names_array)
    plt.figure()
    plt.hold(True)
    for elem in names_array:
        plt.plot(pd.ewma(buffer_df[elem],halflife=1))
    plt.ylabel("Bufer Marginal Distribution")
    plt.savefig("buffer_evolution.pdf",dpi=300);

NOPKTS = False
if (NOPKTS):
    list1 = [ "group-suppression-US-2-lambda-1.9.txt", "group-suppression-US-2-lambda-2.5.txt", "group-suppression-US-2-lambda-4.0.txt","group-suppression-US-2-lambda-100.txt" ]
    labels1 =[ r"$U_s=2, \lambda=1.9$", r"$U_s=2, \lambda=2.5$", r"$U_s=2, \lambda=4.0$", r"$U_s=2, \lambda=100$"]

    list3 = ["Decline-Most-Popular-Us-2-lambda-1.9.txt" ,"Decline-Most-Popular-Us-2-lambda-4.txt" , "Decline-Most-Popular-Us-2-lambda-50.txt" ,"Decline-Most-Popular-Us-2-lambda-100.txt", ]
    labels3 =[ r"$U_s=2, \lambda=1.9$", r"$U_s=2, \lambda=4$", r"$U_s=2, \lambda=50$", r"$U_s=2, \lambda=100$"]

    list2 = ["Rarest-Us-2-lambda-1.9.txt" ,"Rarest-Us-2-lambda-4.txt" , "Rarest-Us-2-lambda-50.txt" ,"Rarest-Us-2-lambda-100.txt", ]
    labels2 =[ r"$U_s=2, \lambda=1.9$", r"$U_s=2, \lambda=4$", r"$U_s=2, \lambda=50$", r"$U_s=2, \lambda=100$"]

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
        ax.legend(loc=2)
        ax.set_ylabel("Number of Peers")

    plt.suptitle('(m=10)',size=18)
    plt.savefig("peer-evolution.pdf",dpi=300)
