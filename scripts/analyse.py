import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
import seaborn as sns
sns.set(style="whitegrid")
# plt.style.use('fivethirtyeight')
ewma = pd.stats.moments.ewma
# seedsList = [10,2000,4000,4234,43204,43924,391,28392,45,789]
seedsList = [10,2000,4000,4234,43204]
# print(plt.style.available)
# FOLDER_NAME = r"../output/k6lambda4/"
# External_Data_Folder = r"/Volumes/Ghatotkacha/work/output/"
External_Data_Folder = r"/Volumes/T5-Vamsee/P2P-Simulations/output/"
FOLDER_NAME_5 = r"../output/k5lambda4/"
RESULTS_FOLDER_NAME = r"../results/"
FOLDER_NAME_25 = r"../output/k25lambda4/"
FOLDER_NAME_10 = r"../output/k10lambda4/"
FOLDER_NAME_2 = r"../output/k2lambda4/"
# FOLDER_NAME = r"../output/k15lambda4/"

def read_wait_times(file_name, stability_time, ending_time ):
    waiting_times = []
    for each_line in open(file_name):
        each_line = each_line.strip("\n")
        count = each_line.split(':')[0]
        waiting_times.append(float(count))
    return waiting_times[stability_time:min(ending_time,len(waiting_times)-1 )]
def read_no_pkts(filename):
    file_ptr = open(FOLDER_NAME_5+filename,'r')
    peer_evolution =[]
    for line in file_ptr:
        value = (line.strip('\n '))
        count = int(each_line.split(':')[0])
        peer_evolution.append(value)
    return peer_evolution
def read_distribution(file_name,m):
    NoRows = 10000
    buffer_distribution=np.zeros((NoRows,m))
    idx = 0
    for each_line in open(file_name):
        each_line = each_line.strip("\n")
        each_line = each_line.rstrip(" ")
        buffer_distribution[idx,:]= [float(x) for x in each_line.split(' ')]
        idx = idx+1
        if(idx >= NoRows):
            break
    return buffer_distribution

RANDOM_EVOLUTION = False
if(RANDOM_EVOLUTION):
    FOLDER_NAME_5_NULL  =  r"../output/k5lambda4-null/"
    colors=['red','blue','green','black', 'orange']
    fig,(ax1,ax2)= plt.subplots(2,1,figsize=(8,8))
    names_array = list(["col"+str(x) for x in range(1,6)])
    buffer_df = pd.read_csv(FOLDER_NAME_5_NULL+"Random_distribution.txt",sep='\s+',names=names_array)
    ax = ax2
    ax.grid(True)
    ax.set_xlabel("Time",fontsize=18)
    marker_list = ['o','*','^','s','o','o']
    idx = 0
    for elem in names_array:
        ax.plot(pd.ewma(buffer_df[elem],halflife=1),'-',marker = marker_list[idx],markevery=70,linewidth=1,alpha=0.8)
        idx = idx+1
    ax.set_ylabel("Chunk Frequency",fontsize=18)
    ax.set_xlim([0,5000])
    ax.set_ylim([-0.1,1.1])
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    ax.text(0.55, 0.55, 'Random Chunk Policy\n'+ r'$ \lambda = 4,\mu =1,  U =1,m=5 $', transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
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
    ax.text(0.3, 0.85, 'Random Chunk Policy\n'+ r'$ \lambda = 4,  \mu =1, U =1,m=5 $', transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
    ax.set_xlim([0,5000])
    ax.set_ylim([0,1000])

    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"random-chunk-evolution.pdf",dpi=300);

BUFFEREVOL = False
if(BUFFEREVOL):
    list_policies =[ r"Random", r"Rare Chunk",r"Group Suppression",   r"Mode Suppression", r"Threshold ModeSup"  ]
    list2= ["Random",  "RareChunk", "GroupSup",   "ModeSup",  "T6-ThModeSup"]
    labels2 =list_policies

    fig,((ax1,ax2,ax3),(ax4,ax5,ax6))= plt.subplots(2,3,figsize=(12,7),sharex=False,sharey=True)
    axes = [ax1,ax2,ax3,ax4,ax5,ax6]

    colors=['red','palevioletred','darkcyan','mediumseagreen', 'slategray']

    for i in range(5):
        lambda_value = 5
        file_name = External_Data_Folder + "k5lambda4/" + list2[i] + "_distribution_seed_10.txt"
        buffer_distribution = read_distribution(file_name,5)
        ax = axes[i]
        idx = 0
        for elem in range(lambda_value):
            if(idx == 0):
                ax.plot(list(buffer_distribution[:,elem]),'--',color=colors[idx],alpha = 0.85,linewidth=2)
            else:
                ax.plot(list(buffer_distribution[:,elem]),color=colors[idx],alpha = 0.85,linewidth=2)

            idx= idx+1
        ax.set_xlim([0,2000])
        ax.set_ylim([-0.02,1])
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        ax.text(0.35, 0.9, labels2[i], transform=ax.transAxes, fontsize=13, verticalalignment='top', bbox=props)

    axes[0].set_ylabel("Chunk Frequency",fontsize=13)
    axes[3].set_ylabel("Chunk Frequency",fontsize=13)
    axes[4].set_xlabel("Time",fontsize=14)
    axes[3].set_xlabel("Time",fontsize=14)
    axes[2].set_xlabel("Time",fontsize=14)

    plt.sca(ax6)
    plt.axis('off')
    props = dict(boxstyle='round', facecolor='blue', alpha=0.3)
    ax6.text(0.25, 0.6, "$\lambda = 4, m=5$\n $U =1, \mu = 1$", transform=ax6.transAxes, fontsize=16, verticalalignment='top', bbox=props)

    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"new-marginal-buffer-evolution.pdf",dpi=300);

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


def mean_waiting_times(folder_name,stability_time,list_policies,ending_time = pow(10,5)):
    mean_sojourn_times = []
    std_sojourn_times = []
    for policy in list_policies:
        waiting_time = []
        for seed in seedsList[0:3]:
            file_name = folder_name+ policy+"_waitingTime_seed_"+str(seed)+".txt"
            waiting_time = waiting_time+ read_wait_times(file_name, stability_time, ending_time)
        mean_sojourn_times.append(np.mean(waiting_time))
        std_sojourn_times.append(np.std(waiting_time))
    return mean_sojourn_times,[0.85*x for x in  std_sojourn_times]

SOJURNHIST_RESTRICT_ONE_PEER = True
lamb =30



if(SOJURNHIST_RESTRICT_ONE_PEER):
    Stability_Time = 3000


    labels = [  "Rare Chunk-1","Common Chunk-1", "DMS-1","Group Suppr-1","Mode Suppr-1",  "Threshold Mode-1" ]
    list_policy_names = ["SupprRareChunk", "SupprCommonChunk","SupprLocalMode","GroupSup", "ModeSup",  "ThModeSup" ]
    # list_policy_names = ["SupprRareChunk", "SupprCommonChunk","GroupSup", "ModeSup","ThModeSup", "SupprLocalMode",  ]
    # labels = [  "Rare Chunk-1","Common Chunk-1","Group Suppr-1","Mode Suppr-1",  "Threshold Mode-1", "DMS-1"]

    fig,(ax1,ax2)= plt.subplots(2,1,figsize=(14,10), sharey = True)
    # colors = ['blue', 'tomato','violet','mediumseagreen','c','gray']
    colors = ['lightsteelblue', 'tomato','violet','mediumseagreen','blue','gray']
    ecolors = ['blue', 'red','pink','darkgreen','darkblue','black']

    list_file_sizes = [2,10,15,25,30,40];
    # list_folders = [External_Data_Folder+"k"+str(m) + "lambda"+str(lamb)+"/" for m in list_file_sizes]
    list_folders = [External_Data_Folder+"m"+str(m) +"/"+ "lambda"+str(lamb)+"-" for m in list_file_sizes]
    width=0.15

    ax = ax1
    rects = list(range(len(list_folders)))
    for idx in range(len(list_folders)):
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(list_folders[idx],Stability_Time,list_policy_names)
        rects[idx] = ax.bar([x+(0.25+idx)*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color=colors[idx],yerr=std_sojourn_times,alpha=0.8, ecolor=ecolors[idx])

    ax.legend(tuple(rects), ('$m=2$','$m=10$','$m=15$','$m=25$','$m=30$','$m=40$'),fontsize=16,loc=1)
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    # ax.text(0.35, 0.95, r"$\lambda = 4, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
    ax.text(0.35, 0.95, "$\lambda = $"+str(lamb)+"$, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
    # ax.set_xlabel(xlabel='Policy',fontsize=16)
    ax.set_ylabel("Mean Sojourn Time (Stationarity)",fontsize=16)
    plt.sca(ax1)
    plt.xticks([x+0.45 for x in range(1,len(labels)+1)],labels,fontsize=14)
    # ax.set_ylim([0,80])

    ax = ax2
    # labels = [  "Mode Suppr-3","Group Suppr-3", "DMS-3","Rare Chunk-3", "Common Chunk-3", "Threshold Mode-3" ]
    # list_policy_names = ["BoostModeSup","BoostGroupSup", "StrictLocalMode", "RareChunk", "CommonChunk", "BoostThMode" ]

    labels = [  "Rare Chunk-3","Common Chunk-3", "DMS-3" ,"Group Suppr-3","Mode Suppr-3",  "Threshold Mode-3"]
    list_policy_names = ["RareChunk", "CommonChunk","StrictLocalMode","BoostGroupSup", "BoostModeSup",  "BoostThMode" ]

    rects = list(range(len(list_folders)))

    for idx in range(len(list_folders)):
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(list_folders[idx],Stability_Time,list_policy_names)
        rects[idx] = ax.bar([x+(.25+idx)*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color=colors[idx],yerr=std_sojourn_times,alpha=0.75,ecolor=ecolors[idx])

    ax.legend(tuple(rects), ('$m=2$','$m=10$','$m=15$','$m=25$','$m=30$','$m=40$'),fontsize=16,loc=1)
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    ax.text(0.35, 0.95, "$\lambda = $"+str(lamb)+"$, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)

    # ax.set_xlabel(xlabel='Policy',fontsize=16)
    ax.set_ylabel("Mean Sojourn Time (Stationarity)",fontsize=16)
    plt.sca(ax)
    plt.xticks([x+0.45 for x in range(1,len(labels)+1)],labels,fontsize=14)
    ax.set_ylim([0,100])

    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"Sojourn-DMS-Stationary-lambda-"+str(lamb)+".pdf",dpi=300)

SOJURNHIST_BOOST_THREE_PEERS= True
if(SOJURNHIST_BOOST_THREE_PEERS):

    fig,(ax1,ax2)= plt.subplots(2,1,figsize=(14,10),sharey=True)
    colors = ['lightsteelblue', 'tomato','violet','mediumseagreen','blue','gray']
    ecolors = ['blue', 'red','pink','darkgreen','darkblue','black']

    ax = ax1
    list_file_sizes = [2,10,15,25,30,40];
    # list_folders = [External_Data_Folder+"k"+str(m) + "lambda"+str(lamb)+"/" for m in list_file_sizes]
    list_folders = [External_Data_Folder+"m"+str(m) +"/"+ "lambda"+str(lamb)+"-" for m in list_file_sizes]
    # labels = [  "Mode Suppr-1","Group Suppr-1", "DMS-1","Rare Chunk-1", "Common Chunk-1", "Threshold Mode-1" ]
    # list_policy_names = ["ModeSup","GroupSup", "SupprLocalMode", "SupprRareChunk", "SupprCommonChunk", "ThModeSup" ]

    labels = [  "Rare Chunk-1","Common Chunk-1","DMS-1", "Group Suppr-1","Mode Suppr-1",  "Threshold Mode-1" ]
    list_policy_names = ["SupprRareChunk", "SupprCommonChunk","SupprLocalMode","GroupSup", "ModeSup",  "ThModeSup" ]
    width=0.15
    rects = list(range(len(list_folders)))
    for idx in range(len(list_folders)):
        beginning_time = 0
        ending_time = 1000
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(list_folders[idx],beginning_time,list_policy_names, ending_time)
        rects[idx] = ax.bar([x+(1+idx)*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color=colors[idx],yerr=std_sojourn_times,alpha=0.75,ecolor=ecolors[idx])

    ax.legend(tuple(rects), ('$m=2$','$m=10$','$m=15$','$m=25$','$m=30$','$m=40$'),fontsize=16,loc=1)
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    # ax.text(0.35, 0.95, r"$\lambda = 4, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
    ax.text(0.35, 0.95, "$\lambda = $"+str(lamb)+"$, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
    # ax.set_xlabel(xlabel='Policy',fontsize=16)
    ax.set_ylabel("Mean Sojourn Time (Mixing) ",fontsize=16)
    plt.sca(ax1)
    plt.xticks([x+0.45 for x in range(1,len(labels)+1)],labels,fontsize=14)


    ax = ax2
    # list_folders = [External_Data_Folder+"k"+str(m) + "lambda"+str(lamb)+"/" for m in list_file_sizes]
    # labels = [  "Mode Suppr-3","Group Suppr-3", "DMS-3","Rare Chunk-3", "Common Chunk-3", "Threshold Mode-3" ]
    # list_policy_names = ["BoostModeSup","BoostGroupSup", "StrictLocalMode", "RareChunk", "CommonChunk", "BoostThMode" ]

    labels = [  "Rare Chunk-3","Common Chunk-3","DMS-3","Group Suppr-3","Mode Suppr-3",  "Threshold Mode-3" ]
    list_policy_names = ["RareChunk", "CommonChunk","StrictLocalMode","BoostGroupSup", "BoostModeSup",  "BoostThMode" ]

    rects = list(range(len(list_folders)))

    for idx in range(len(list_folders)):
        Starting_time = 0
        ending_time = 1000
        mean_sojourn_times,std_sojourn_times = mean_waiting_times(list_folders[idx],Starting_time,list_policy_names,ending_time)
        rects[idx] = ax.bar([x+(1.5+idx)*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color=colors[idx],yerr=std_sojourn_times,alpha=0.75,ecolor=ecolors[idx])

    ax.legend(tuple(rects), ('$m=2$','$m=10$','$m=15$','$m=25$','$m=30$','$m=40$'),fontsize=16,loc=1)
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    # ax.text(0.35, 0.95, r"$\lambda = 4, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
    ax.text(0.35, 0.95, "$\lambda = $"+str(lamb)+"$, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)

    # ax.set_xlabel(xlabel='Policy',fontsize=16)
    ax.set_ylabel("Mean Sojourn Time (Mixing) ",fontsize=16)
    plt.sca(ax)
    plt.xticks([x+0.45 for x in range(1,len(labels)+1)],labels,fontsize=14)
    ax.set_ylim([0,105])


    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"Sojourn-DMS-Mixing-lambda-"+str(lamb)+".pdf",dpi=300)

def mean_number_users(folder_name,stability_time,list_policies):
    mean_sojourn_times = []
    std_sojourn_times = []
    for policy in list_policies:
        waiting_time = []
        for seed in seedsList:
            file_name = folder_name+ policy+"_peers_seed_"+str(seed)+".txt"
            waiting_time = waiting_time+ read_wait_times(file_name,stability_time)
        mean_sojourn_times.append(np.mean(waiting_time))
        std_sojourn_times.append(np.std(waiting_time))
    return mean_sojourn_times,[x for x in  std_sojourn_times]
# Plot average number of peers in the system in histogram format

NUMBER_OF_USERS= False
if( NUMBER_OF_USERS):

    Stability_Time = 2000
    fig,(ax1,ax2)= plt.subplots(2,1,figsize=(12,10),sharey=True)

    lamb = 4
    list_file_sizes = [2,10,15,25,30,40];
    list_folders = [External_Data_Folder+"k"+str(m) + "lambda"+str(lamb)+"/" for m in list_file_sizes]
    width=0.13
    colors = ['blue', 'tomato','violet','green','yellow','gray']


    ax = ax1
    labels_1 = [  "Mode Suppr-1","Group Suppr-1", "DMS-1","Rare Chunk-1", "Common Chunk-1", "Threshold Mode-1" ]
    list_policy_names = ["ModeSup","GroupSup", "SupprLocalMode", "SupprRareChunk", "SupprCommonChunk", "ThModeSup" ]
    rects = list(range(len(list_folders)))

    for idx in range(len(list_folders)):
        mean_sojourn_times,std_sojourn_times = mean_number_users(list_folders[idx],Stability_Time,list_policy_names)
        rects[idx] = ax.bar([x+(1.5+idx)*width for x in range(1,len(labels_1)+1)],mean_sojourn_times,width,color=colors[idx],yerr=std_sojourn_times,alpha=0.75)

    ax = ax2
    labels_3 = [  "Mode Suppr-3","Group Suppr-3", "DMS-3","Rare Chunk-3", "Common Chunk-3", "Threshold Mode-3" ]
    list_policy_names = ["BoostModeSup","BoostGroupSup", "StrictLocalMode", "RareChunk", "CommonChunk", "BoostThMode" ]

    for idx in range(len(list_folders)):
        mean_sojourn_times,std_sojourn_times = mean_number_users(list_folders[idx],Stability_Time,list_policy_names)
        rects[idx] = ax.bar([x+(1.5+idx)*width for x in range(1,len(labels_3)+1)],mean_sojourn_times,width,color=colors[idx],yerr=std_sojourn_times,alpha=0.75)

    for ax in [ax1,ax2]:
        ax.legend( tuple(rects), ('$m=2$','$m=10$','$m=15$','$m=25$','$m=30$','$m=40$'),fontsize=16,loc=1)
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        ax.text(0.35, 0.95, r"$\lambda = 4, \;\mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)
        ax.set_ylabel("Mean Number of Users",fontsize=16)
        # ax.set_ylim([0,340])

    plt.sca(ax1)
    plt.xticks([x+0.45 for x in range(1,len(labels_1)+1)],labels_1,fontsize=14)
    plt.sca(ax2)
    plt.xticks([x+0.45 for x in range(1,len(labels_3)+1)],labels_3,fontsize=14)

    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"Average-Peers-p1000-lambda-"+str(lamb)+".pdf",dpi=300)

NO_PKTS = False
if (NO_PKTS):

    # list2 = ["Rarest_Us-2-lambda-1.9.txt" ,"Rarest_Us-2-lambda-4.txt" , "Rarest_Us-2-lambda-50.txt" ,"Rarest_Us-2-lambda-100.txt", ]
    # labels2 =[ r"$U_s=2, \lambda=1.9$", r"$U_s=2, \lambda=4$", r"$U_s=2, \lambda=50$", r"$U_s=2, \lambda=100$"]

    policy_files= ["Rarest_peers.txt",   "ModeSup_peers.txt", "DistrModeSup_peers.txt" ]
    policy_names=[ r"Rarest First",  r"Mode Suppression", r"Distributed Mode Suppression" ]
    # lambda_list = ["0.5","4","10","20","100"]
    lambda_list = ["0.5","4","10","20"]

    fig,(ax0,ax1,ax2)= plt.subplots(1,3,figsize=(12,4))
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

RELAXED_SOJUOURN = False
if(RELAXED_SOJUOURN):

    lamb = 4
    folder_2 =  r"../output/k2" + "lambda"+str(lamb)+"/"
    folder_5 =  r"../output/k5" + "lambda"+str(lamb)+"/"
    folder_10 =  r"../output/k10" + "lambda"+str(lamb)+"/"
    folder_15 =  r"../output/k15" + "lambda"+str(lamb)+"/"
    folder_20 =  r"../output/k20" + "lambda"+str(lamb)+"/"
    fig,(ax1)= plt.subplots(1,1,figsize=(10,5))

    ax = ax1
    folder_5 =  r"../output/k5" + "lambda"+str(lamb)+"/"
    # labels = [ "Group Suppr",  "Mode Suppr", " DMS ", "Boosted MS","Rare Chunk", "Common Chunk"]
    labels = [ "Group Suppr",  "Mode Suppr", " DMS(sample2) ", "Rare Chunk", "Common Chunk"]
    width=0.18

    mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_2)
    rects1 = ax.bar([x+0.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='blue',yerr=std_sojourn_times,alpha=0.5)
    mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_5)
    rects2 = ax.bar([x+1.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='darkseagreen',yerr=std_sojourn_times,alpha=0.75)
    mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_10)
    rects3 = ax.bar([x+2.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='tomato',yerr=std_sojourn_times,alpha=0.75)
    mean_sojourn_times,std_sojourn_times = mean_waiting_times(folder_15)
    rects4 = ax.bar([x+3.5*width for x in range(1,len(labels)+1)],mean_sojourn_times,width,color='violet',yerr=std_sojourn_times,alpha=0.75)

    ax.legend((rects1[0], rects2[0], rects3[0], rects4[0]), ('$m=2$', '$m=5$','$m=10$','$m=15$'),fontsize=16,loc=1)
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    ax.text(0.35, 0.95, r"$\lambda = 4,\; \mu =1,\; U =1$", transform=ax.transAxes, fontsize=16, verticalalignment='top', bbox=props)



    # ax.set_xlabel(xlabel='Policy',fontsize=16)
    ax.set_ylabel("Mean Sojourn Time",fontsize=16)
    plt.sca(ax1)
    plt.xticks([x+0.45 for x in range(1,len(labels)+1)],labels,fontsize=14)
    ax.set_ylim([0,50])

    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"sojourn-boosted-histogram-lambda-"+str(lamb)+".pdf",dpi=300)

def read_suppression_file(file_name):
    percentage_suppressed_list = []
    percentage_not_transfered = []
    percentage_transfered = []
    percentage_not_available= []
    for each_line in open(file_name):
        each_line = each_line.strip('\n')
        (No_sampled, No_transfered, No_NotAvailable, No_Suppressed) = [ int(x) for x in each_line.split(' ')]
        if No_sampled > 0:
            percentage = 100.0 * float(No_Suppressed)/float(No_sampled)
            percentage_suppressed_list.append(percentage)
            percentage = 100.0 * float(No_Suppressed+No_NotAvailable)/float(No_sampled)
            percentage_not_transfered.append(percentage)
            percentage = 100.0 * float(No_transfered)/float(No_sampled)
            percentage_transfered.append(percentage)
            percentage = 100.0 * float(No_NotAvailable)/float(No_sampled)
            percentage_not_available.append(percentage)
    return percentage_suppressed_list, percentage_transfered, percentage_not_transfered, percentage_not_available

SUPPRESSION_TIME = False
if(SUPPRESSION_TIME):

    lamb = 100
    folder_5 =  r"../output/k2" + "lambda"+str(lamb)+"/"
    # folder_10 =  r"../output/k10" + "lambda"+str(lamb)+"/"
    # folder_15 =  r"../output/k15" + "lambda"+str(lamb)+"/"
    # folder_20 =  r"../output/k20" + "lambda"+str(lamb)+"/"
    fig,((ax1,ax2),(ax3,ax4))= plt.subplots(2,2,figsize=(14,16))

    list_suppression= [ "GroupSup_starvations.txt","ModeSup_starvations.txt",  "StrictLocalMode_starvations.txt", "Friedman_starvations.txt", "CommonChunk_starvations.txt"]
    policy_names_list = [ "GroupSup", "ModeSup",  "DistrModeSup","Friedman", "CommonChunk"]
    names_array = list(["col"+str(x) for x in range(1,2)])

    combined_suppression_list = []
    combined_transfered_list = []
    combined_not_transfered_list= []
    combined_not_available_list= []
    for suppression_file, policy_name in zip(list_suppression,policy_names_list):
        print(suppression_file,":",policy_name)
        percentage_suppressed, percentage_transfered, percentage_not_transfered , percentage_not_available= read_suppression_file(folder_5+suppression_file)
        combined_suppression_list.append(percentage_suppressed[3000:])
        combined_transfered_list.append(percentage_transfered[3000:])
        combined_not_transfered_list.append(percentage_not_transfered[3000:])
        combined_not_available_list.append(percentage_not_available[3000:])

    violin_parts = ax1.violinplot(combined_transfered_list, showmeans=True ,showextrema = False)
    for pc in violin_parts['bodies']:
        pc.set_facecolor('red')
        pc.set_edgecolor('black')
    violin_parts = ax2.violinplot(combined_not_transfered_list, showmeans=True ,showextrema = False)
    for pc in violin_parts['bodies']:
        pc.set_facecolor('green')
        pc.set_edgecolor('black')
    ax3.violinplot(combined_suppression_list, showmeans=True ,showextrema = False)
    violin_parts = ax4.violinplot(combined_not_available_list, showmeans=True ,showextrema = False)
    for pc in violin_parts['bodies']:
        pc.set_facecolor('violet')
        pc.set_edgecolor('black')

    ax1.set_ylabel("Percentage Transfered",fontsize=16)
    ax2.set_ylabel("Percentage Not Transfered",fontsize=16)
    ax3.set_ylabel("Percentage Suppressed",fontsize=16)
    ax4.set_ylabel("Percentage Not Available",fontsize=16)
    # ax1.set_ylim([0,60])
    for ax in [ax1,ax2,ax3,ax4]:
        plt.sca(ax)
        plt.xticks(range(1,len(list_suppression)+1),policy_names_list,fontsize=12)


    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"suppression-time.pdf",dpi=300)

NUMBER_OF_PEERS = False
if(NUMBER_OF_PEERS):
    policy_files= ["Random_peers.txt", "Rarest_peers.txt",   "ModeSup_peers.txt", "DistrModeSup_peers.txt", "CommonChunk_peers.txt" , "Friedman_peers.txt", "StrictLocalMode_peers.txt", "GroupSup_peers.txt"]
    policy_names=[ "Random Chunk",r"Rarest First",  r"Mode Suppression", r"Distributed Mode Suppression", "Common Chunk","Rare Chunk", "Strict Local Mode Sup" , "Group Suppression"]
    # lambda_list = ["0.5","4","10","20","100"]
    lambda_list = ["100"]

    fig,((ax0,ax1),(ax2,ax3),(ax4,ax5), (ax6,ax7))= plt.subplots(4,2,figsize=(12,16))
    ax_list = [ax0,ax1,ax2,ax3,ax4,ax5,ax6,ax7]
    # colors=['red','blue','green','black','orange','brown']

    marker_list = ['o','*','^','s','^','o']
    lambda_idx = 0
    for each_lambda in lambda_list:
        for idx in range(len(policy_files)):
            ax = ax_list[idx]
            folder_name = r"../output/k2"+"lambda"+each_lambda+"/"
            peers_evolution_list = pd.read_csv(folder_name+policy_files[idx],'\n').values
            ax.plot(peers_evolution_list,'-',alpha=0.7,label= policy_names[idx]+": $\lambda=$ "+each_lambda, marker = marker_list[lambda_idx],markevery=100,linewidth=1)
        lambda_idx = lambda_idx+1


    for ax in ax_list:
        ax.grid(True)
        ax.set_xlabel("Time",fontsize=14)
        # ax.set_xlim([0,50000])
        # ax.set_ylim([0,10000])
        ax.legend(loc=1,fontsize=14)
        # props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        # ax.text(0.35, 0.95, "lambda", transform=ax.transAxes, fontsize=14, verticalalignment='top', bbox=props)

    ax0.set_ylabel("Number of Peers",fontsize=14)
    # ax0.set_title("Random Chunk Policy",fontsize=16)
    # ax1.set_title("Mode Suppression",fontsize=16)
    # ax2.set_title("Distributed Mode Suppression",fontsize=16)
    # plt.suptitle('(m=10)',size=18)
    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"number_peers_m2.pdf",dpi=300)
