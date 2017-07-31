import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
import seaborn as sns
sns.set(style="whitegrid")
# plt.style.use('fivethirtyeight')
ewma = pd.stats.moments.ewma

print(plt.style.available)
# FOLDER_NAME = r"../output/k6lambda4/"
FOLDER_NAME_5 = r"../output/k5lambda4/"
RESULTS_FOLDER_NAME = r"../results/"
FOLDER_NAME_25 = r"../output/k25lambda4/"
FOLDER_NAME_10 = r"../output/k10lambda4/"
FOLDER_NAME_2 = r"../output/k2lambda4/"
# FOLDER_NAME = r"../output/k15lambda4/"

def read_wait_times(file_name):
    waiting_times = []
    for each_line in open(file_name):
        each_line = each_line.strip("\n")
        waiting_times.append(float(each_line))
    return waiting_times
def read_no_pkts(filename):
    file_ptr = open(FOLDER_NAME_5+filename,'r')
    peer_evolution =[]
    for line in file_ptr:
        value = int(line.strip('\n '))
        peer_evolution.append(value)
    return peer_evolution

RANDOM_EVOLUTION = False
if(RANDOM_EVOLUTION):
    FOLDER_NAME_5_NULL  =  r"../output/k5lambda4-null/"
    colors=['red','blue','green','black', 'orange']
    fig,(ax1,ax2)= plt.subplots(2,1,figsize=(8,8))
    names_array = list(["col"+str(x) for x in range(1,6)])
    buffer_df = pd.read_csv(FOLDER_NAME_5_NULL+"Random_distribution.txt",sep='\s+',names=names_array)
    plt.hold(True)
    ax = ax2
    ax.grid(True)
    ax.set_xlabel("Time",fontsize=18)
    marker_list = ['o','*','^','s','o','o']
    idx = 0
    for elem in names_array:
        ax.plot(pd.ewma(buffer_df[elem],halflife=1),'-',marker = marker_list[idx],markevery=70,linewidth=1,alpha=0.8)
        idx = idx+1
    ax.set_ylabel("Marginal Chunk Distribution",fontsize=18)
    ax.set_xlim([0,5000])
    ax.set_ylim([-0.1,1.1])
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    ax.text(0.55, 0.55, 'Random Chunk Policy\n'+ r'$ \lambda = 4, \;\mu = U =1,m=5 $', transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
    # ax.legend(loc=2)
    ax = ax1
    ax1.plot

    peer_evolution = pd.read_csv(FOLDER_NAME_5_NULL+"Random_peers.txt",'\n')
    ax.plot(peer_evolution.values,'-*',alpha=0.7,label="Random",linewidth=2,marker='*',markevery=70)
    ax.grid(True)
    # ax.set_xlabel("Time",fontsize=14)
    # ax.legend(loc=2)
    ax.set_ylabel("Number of Peers",fontsize=18)
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    ax.text(0.3, 0.85, 'Random Chunk Policy\n'+ r'$ \lambda = 4, \; \mu = U =1,m=5 $', transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
    ax.set_xlim([0,5000])
    ax.set_ylim([0,1000])

    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"random-chunk-evolution.pdf",dpi=300);



BUFFEREVOL = False
if(BUFFEREVOL):
    list2= ["Rarest_distribution.txt",     "ModeSup_distribution.txt",  "DistrModeSup_distribution.txt", "GroupSup_distribution.txt","CommonChunk_distribution.txt","Friedman_distribution.txt"]
    # list_peers= ["Rarest_peers.txt",  "ModeSup_peers.txt","DistrModeSup_peers.txt",   "GroupSup_peers.txt",  "CommonChunk_peers.txt" ]
    labels2 =[ r"Rarest First",r"Mode Suppression",r"Distributed Mode Suppression",  r"Group Suppression", r"Common Chunk" , r"Rare Chunk" ]

    # fig,((ax1,ax2),(ax3,ax4),(ax5,ax6))= plt.subplots(2,3,figsize=(10,6),sharex=True,sharey=True)
    fig,((ax1,ax2,ax3),(ax4,ax5,ax6))= plt.subplots(2,3,figsize=(12,6),sharex=True,sharey=True)
    axes = [ax1,ax2,ax3,ax4,ax5,ax6]
    colors=['red','blue','green','black', 'orange']
    colors=['red','palevioletred','darkcyan','mediumseagreen', 'slategray']

    for i in range(6):
        print(FOLDER_NAME_5+list2[i])
        names_array = list(["col"+str(x) for x in range(1,6)])
        buffer_df = pd.read_csv(FOLDER_NAME_5+list2[i],sep='\s+',names=names_array)
        # No_Peers = pd.read_csv(FOLDER_NAME_5+list_peers[i],'\n',names=['col1'])
        # for x in range(1,6):
        #     buffer_df["col"+str(x)] = buffer_df["col"+str(x)] * No_Peers.col1

        plt.hold(True)
        ax = axes[i]
        ax.grid(True)
        idx = 0
        for elem in names_array:
            if(idx == 0):
                ax.plot(pd.ewma(buffer_df[elem],halflife=1),'--',color=colors[idx],alpha = 0.85,linewidth=2)
            else:
                ax.plot(pd.ewma(buffer_df[elem],halflife=1),color=colors[idx],alpha = 0.85,linewidth=2)

            idx= idx+1
        if(i%3 ==0 ):
            ax.set_ylabel("Marginal Chunk Distribution",fontsize=13)
        ax.set_xlim([0,2500])
        ax.set_ylim([-0.02,1])
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        ax.text(0.25, 0.95, labels2[i], transform=ax.transAxes, fontsize=13, verticalalignment='top', bbox=props)
        # ax.legend(loc=2)

    axes[5].set_xlabel("Time",fontsize=14)
    axes[4].set_xlabel("Time",fontsize=14)
    axes[3].set_xlabel("Time",fontsize=14)
    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"buffer-evolution.pdf",dpi=300);


NOPKTS = False
if (NOPKTS):

    # list2 = ["Rarest_Us-2-lambda-1.9.txt" ,"Rarest_Us-2-lambda-4.txt" , "Rarest_Us-2-lambda-50.txt" ,"Rarest_Us-2-lambda-100.txt", ]
    # labels2 =[ r"$U_s=2, \lambda=1.9$", r"$U_s=2, \lambda=4$", r"$U_s=2, \lambda=50$", r"$U_s=2, \lambda=100$"]

    list2= ["Rarest_peers.txt",  "Friedman_peers.txt",  "GroupSup_peers.txt", "ModeSup_peers.txt", "DistrModeSup_peers.txt", "CommonChunk_peers.txt"]

    labels2 =[ r"Rarest First", r"Friedman", r"Group Suppression", r"Mode Suppression", r"Distributed Mode Suppression", "Common Chunk" ]
    peer_evolution = []
    for file_name in list2:
        peer_evolution.append(pd.read_csv(FOLDER_NAME+file_name,'\n'))

    fig,((ax1,ax2),(ax3,ax4),(ax5,ax6))= plt.subplots(3,2,figsize=(14,6))
    axes = [ax1,ax2,ax3,ax4,ax5,ax6]
    colors=['red','blue','green','black','orange','brown']

    for i in range(6):
        axes[i].plot(peer_evolution[i].values,'-',color=colors[i],alpha=0.7,label=labels2[i])
        ax = axes[i]
        ax.grid(True)
        ax.set_xlabel("Time")
        # ax.legend(loc=2)
        ax.set_ylabel("Number of Peers")
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        ax.text(0.35, 0.95, labels2[i], transform=ax.transAxes, fontsize=14, verticalalignment='top', bbox=props)

    # plt.suptitle('(m=10)',size=18)
    plt.tight_layout()
    plt.savefig(FOLDER_NAME+"peer-evolution.pdf",dpi=300)


SOJURNTIME = False
if(SOJURNTIME):
    file_name = FOLDER_NAME+"Rarest_waitingTime.txt"
    waiting_time_rarest= read_wait_times(file_name)

    # file_name = FOLDER_NAME+"Random-waitingTime.txt"
    # waiting_time_random  = read_wait_times(file_name)
    file_name = FOLDER_NAME+ "ModeSup_waitingTime.txt"
    waiting_time_mode_suppression= read_wait_times(file_name)

    file_name = FOLDER_NAME+ "GroupSup_waitingTime.txt"
    waiting_time_gs= read_wait_times(file_name)

    file_name = FOLDER_NAME+"Friedman_waitingTime.txt"
    waiting_time_fd= read_wait_times(file_name)

    file_name = FOLDER_NAME+"DistrModeSup_waitingTime.txt"
    waiting_time_dms= read_wait_times(file_name)

    file_name = FOLDER_NAME+"CommonChunk_waitingTime.txt"
    waiting_time_cc= read_wait_times(file_name)

    fig,ax= plt.subplots(1,1,figsize=(14,6))
    # ax.plot(ewma(np.array(waiting_time_random) ,span=2),'-',label="Rarest")
    ax.plot(ewma(np.array(waiting_time_mode_suppression) ,span=20),'b-',label="Mode Suppression", alpha=0.3)
    ax.plot(ewma(np.array(waiting_time_gs) ,span=20),'g-',label="Group Suppression",alpha=0.4)
    ax.plot(ewma(np.array(waiting_time_fd) ,span=20),'k-',label="Forced Friedman",alpha=0.5)
    ax.plot(ewma(np.array(waiting_time_dms) ,span=20),'r-',label="Distributed Mode Suppression")
    ax.legend(loc=1)
    ax.set(xlabel='time',ylabel="Sojourn Time")
    ax.set_ylim([0,100])
    plt.tight_layout()
    plt.savefig(FOLDER_NAME+"sojourn-times.pdf",dpi=300)

def mean_waiting_times(folder_name):
    # file_name = folder_name+"Rarest_waitingTime.txt"
    # waiting_time_rarest= read_wait_times(file_name)
    #
    # file_name = folder_name+"Random-waitingTime.txt"
    # waiting_time_random  = read_wait_times(file_name)
    file_name = folder_name+ "ModeSup_waitingTime.txt"
    waiting_time_mode_suppression= read_wait_times(file_name)

    file_name = folder_name+ "GroupSup_waitingTime.txt"
    waiting_time_gs= read_wait_times(file_name)

    file_name = folder_name+"Friedman_waitingTime.txt"
    waiting_time_fd= read_wait_times(file_name)

    file_name = folder_name+"DistrModeSup_waitingTime.txt"
    waiting_time_dms= read_wait_times(file_name)

    file_name = folder_name+"CommonChunk_waitingTime.txt"
    waiting_time_cc= read_wait_times(file_name)

    # labels = [ "Group Suppression", "Forced Friedman", "Mode Suppression", "Distributed Mode Suppression", "Common Chunk"]
    labels = [ "Group Suppression",  "Mode Suppression", "Distributed Mode ", "Forced Friedman", "Common Chunk"]
    time = 10000
    mean_sojourn_times = [  np.mean(waiting_time_gs[time:]), np.mean(waiting_time_mode_suppression[time:]), np.mean(waiting_time_dms[time:]),np.mean(waiting_time_fd[time:]), np.mean(waiting_time_cc[time:])]
    std_sojourn_times = [  np.std(waiting_time_gs[time:]), np.std(waiting_time_mode_suppression[time:]), np.std(waiting_time_dms[time:]),np.std(waiting_time_fd[time:]), np.std(waiting_time_cc[time:])]

    return mean_sojourn_times,[0.85*x for x in  std_sojourn_times]


SOJURNHIST = True
if(SOJURNHIST):


        fig,(ax1,ax2)= plt.subplots(2,1,figsize=(10,10))

        ax = ax1
        lamb = 4
        folder_2 =  r"../output/k2" + "lambda"+str(lamb)+"/"
        folder_5 =  r"../output/k5" + "lambda"+str(lamb)+"/"
        # folder_10 =  r"../output/k10" + "lambda"+str(lamb)+"/"
        folder_15 =  r"../output/k15" + "lambda"+str(lamb)+"/"
        folder_20 =  r"../output/k20" + "lambda"+str(lamb)+"/"
        # folder_25 =  r"../output/k25" + "lambda"+str(lamb)+"/"
        # labels = [ "Group Suppression", "Forced Friedman", "Mode Suppression", "Distributed Mode ", "Common Chunk"]
        labels = [ "Group Suppression",  "Mode Suppression", " DMS ", "Rare Chunk", "Common Chunk"]
        ax.hold(True)
        width=0.18
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_2)
        rects1 = ax.bar([x+0.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='blue',yerr=std_sojourn_times,alpha=0.5)
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_5)
        rects2 = ax.bar([x+1.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='darkseagreen',yerr=std_sojourn_times,alpha=0.75)
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_15)
        rects3 = ax.bar([x+2.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='violet',yerr=std_sojourn_times,alpha=0.75)
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_20)
        rects4 = ax.bar([x+3.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='tomato',yerr=std_sojourn_times,alpha=0.75)

        ax.legend((rects1[0], rects2[0], rects3[0], rects4[0]), ('$m=2$', '$m=5$','$m=15$','$m=20$'),fontsize=16,loc=1)
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        ax.text(0.35, 0.95, r"$\lambda = 4, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)

        # ax.set_xlabel(xlabel='Policy',fontsize=16)
        ax.set_ylabel("Mean Sojourn Time",fontsize=16)
        plt.sca(ax1)
        plt.xticks([x+0.45 for x in range(1,len(labels)+1)],labels,fontsize=14)
        ax.set_ylim([0,50])


        ax = ax2
        lamb =100
        folder_2 =  r"../output/k2" + "lambda"+str(lamb)+"/"
        folder_5 =  r"../output/k5" + "lambda"+str(lamb)+"/"
        # folder_10 =  r"../output/k10" + "lambda"+str(lamb)+"/"
        folder_15 =  r"../output/k15" + "lambda"+str(lamb)+"/"
        folder_20 =  r"../output/k20" + "lambda"+str(lamb)+"/"
        # folder_25 =  r"../output/k25" + "lambda"+str(lamb)+"/"
        # labels = [ "Group Suppression", "Forced Friedman", "Mode Suppression", "Distributed Mode ", "Common Chunk"]
        labels = [ "Group Suppression",  "Mode Suppression", " DMS ", "Rare Chunk", "Common Chunk"]
        ax.hold(True)
        width=0.18
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_2)
        rects1 = ax.bar([x+0.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='blue',yerr=std_sojourn_times,alpha=0.5)
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_5)
        rects2 = ax.bar([x+1.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='darkseagreen',yerr=std_sojourn_times,alpha=0.75)
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_15)
        rects3 = ax.bar([x+2.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='violet',yerr=std_sojourn_times,alpha=0.75)
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_20)
        rects4 = ax.bar([x+3.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='tomato',yerr=std_sojourn_times,alpha=0.75)

        ax.legend((rects1[0], rects2[0], rects3[0], rects4[0]), ('$m=2$', '$m=5$','$m=15$','$m=20$'),fontsize=16,loc=1)
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        ax.text(0.35, 0.95, r"$\lambda = 100,\; \mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)

        # ax.set_xlabel(xlabel='Policy',fontsize=16)
        ax.set_ylabel("Mean Sojourn Time",fontsize=16)
        plt.sca(ax2)
        plt.xticks([x+0.45 for x in range(1,len(labels)+1)],labels,fontsize=14)
        ax.set_ylim([0,55])


        plt.tight_layout()
        plt.savefig(RESULTS_FOLDER_NAME+"sojourn-histogram-lambda-"+str(lamb)+".pdf",dpi=300)



NO_PKTS = False
if (NO_PKTS):

    # list2 = ["Rarest_Us-2-lambda-1.9.txt" ,"Rarest_Us-2-lambda-4.txt" , "Rarest_Us-2-lambda-50.txt" ,"Rarest_Us-2-lambda-100.txt", ]
    # labels2 =[ r"$U_s=2, \lambda=1.9$", r"$U_s=2, \lambda=4$", r"$U_s=2, \lambda=50$", r"$U_s=2, \lambda=100$"]

    policy_files= ["Rarest_peers.txt",   "ModeSup_peers.txt", "DistrModeSup_peers.txt" ]
    policy_names=[ r"Rarest First",  r"Mode Suppression", r"Distributed Mode Suppression" ]
    # lambda_list = ["0.5","4","10","20","100"]
    lambda_list = ["0.5","4","10","20"]

    fig,(ax0,ax1,ax2)= plt.subplots(1,3,figsize=(12,4))
    ax0.hold(True)
    ax1.hold(True)
    ax2.hold(True)
    marker_list = ['o','*','^','s','^','o']
    idx = 0
    for each_lambda in lambda_list:
        folder_name = r"../output/k5"+"lambda"+each_lambda+"/"
        peers_evolution_list = pd.read_csv(folder_name+policy_files[0],'\n').values
        ax0.plot(peers_evolution_list,'-',alpha=0.7,label="$\lambda=$ "+each_lambda, marker = marker_list[idx],markevery=200,linewidth=1)

        peers_evolution_list = pd.read_csv(folder_name+policy_files[1],'\n').values
        ax1.plot(peers_evolution_list,'-',alpha=0.7,label="$\lambda=$ "+each_lambda, marker = marker_list[idx],markevery=200,linewidth=1)

        peers_evolution_list = pd.read_csv(folder_name+policy_files[2],'\n').values
        ax2.plot(peers_evolution_list,'-',alpha=0.7,label="$\lambda=$ "+each_lambda , marker = marker_list[idx],markevery=200,linewidth=1)
        idx = idx+1

    colors=['red','blue','green','black','orange','brown']

    for ax in [ax0,ax1,ax2]:
        ax.grid(True)
        ax.set_xlabel("Time",fontsize=14)
        ax.set_xlim([0,10000])
        ax.set_ylim([0,1000])
        ax.legend(loc=1,fontsize=14)
        # props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        # ax.text(0.35, 0.95, "lambda", transform=ax.transAxes, fontsize=14, verticalalignment='top', bbox=props)

    ax0.legend(loc=2,fontsize=14)
    ax0.set_ylim([0,2400])
    ax0.set_ylabel("Number of Peers",fontsize=14)
    ax0.set_title("Random Chunk Policy",fontsize=16)
    ax1.set_title("Mode Suppression",fontsize=16)
    ax2.set_title("Distributed Mode Suppression",fontsize=16)
    # plt.suptitle('(m=10)',size=18)
    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"peer-evolution-comparison.pdf",dpi=300)
