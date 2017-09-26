import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
import seaborn as sns
sns.set(style="whitegrid")
# plt.style.use('fivethirtyeight')
ewma = pd.stats.moments.ewma

FOLDER_NAME_3_4 = r"../output/k3lambda4/"
FOLDER_NAME_2_4 = r"../output/k2lambda4/"
FOLDER_NAME_2_100 = r"../output/k2lambda100/"
RESULTS_FOLDER_NAME = r"../results/"

def read_buffer_counts(file_name):
    T_bar_i_list = []
    S_i_list  = []
    S_0_list  = []
    D_bar_i_list = []
    for each_line in open(file_name):
        each_line = each_line.strip('\n')
        (T_bar_i, S_i, S_0, D_bar_i ) = [ int(component.split(':')[-1]) for component in each_line.split(' ')]
        # print (T_bar_i, S_i, S_0, D_bar_i )
        total = T_bar_i + S_i + S_0 + D_bar_i
        T_bar_i_list.append(float(T_bar_i)/total)
        S_i_list.append(float(S_i)/total )
        S_0_list.append(float(S_0)/ total)
        D_bar_i_list.append(float(D_bar_i)/total )
        if(len(T_bar_i_list)>800):
            return (T_bar_i_list,S_i_list,S_0_list,D_bar_i_list)
    return (T_bar_i_list,S_i_list,S_0_list,D_bar_i_list)


COMPONENT_ANALYSIS = True
if(COMPONENT_ANALYSIS):

    list_file_names= ["Random_buffer_counts.txt","Rarest_buffer_counts.txt", "CommonChunk_buffer_counts.txt" ,  "GroupSup_buffer_counts.txt", "ModeSup_buffer_counts.txt", "StrictLocalMode_buffer_counts.txt", "DistrModeSup_buffer_counts.txt",  "Friedman_buffer_counts.txt"]

    policy_names =[ "Random ",r"Rarest First", "Common Chunk",  r"Group Suppression", r"Mode Suppression","Strict Local Mode", r"Distributed Mode Suppression(1)",r"Rare Chunk" ]

    fig,((ax1,ax2),(ax3,ax4),(ax5,ax6),(ax7,ax8))= plt.subplots(4,2,figsize=(14,18))

    ax = [ax1, ax2, ax3, ax4, ax5, ax6, ax7, ax8]
    for idx in range(8):
        (T_bar_i, S_i, S_0, D_bar_i) = read_buffer_counts(FOLDER_NAME_3_4+list_file_names[idx])
        plt.sca(ax[idx])
        norm_value = 1.0
        plt.scatter( range(len(T_bar_i)), [1 for x in range(len(T_bar_i))], c=T_bar_i,s=500,  cmap=plt.cm.Reds, marker='s', vmax=norm_value )
        plt.scatter( range(len(T_bar_i)), [2 for x in range(len(T_bar_i))], c=S_i,s=500,  cmap=plt.cm.Blues, marker='s', vmax=norm_value )
        plt.scatter( range(len(T_bar_i)), [3 for x in range(len(T_bar_i))], c=S_0,s=500,  cmap=plt.cm.Oranges, marker='s', vmax=norm_value )
        plt.scatter( range(len(T_bar_i)), [4 for x in range(len(T_bar_i))], c=D_bar_i,s=500,  cmap=plt.cm.Greys, marker='s', vmax=norm_value )
        plt.yticks([x for x in range(1,5)],[r"$\bar{T}_i$", "$S_i$","$S_0$",r"$\bar{D}_i$" ] ,fontsize=14)
        plt.title(policy_names[idx])
        # ax[idx].set_xlim([0,100])


    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"component-evolution-3-4.pdf",dpi=300);
