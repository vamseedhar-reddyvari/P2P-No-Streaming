import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
import seaborn as sns
from file_read_methods import *
sns.set(style="whitegrid")
# plt.style.use('fivethirtyeight')

Output_Data_Folder = r"/Volumes/Ghatotkacha/work/output-threshold/"
Output_MBP_Folder = r"../output/"
RESULTS_FOLDER_NAME = r"../results/"

lambda_List = [2,4,8,16,32,64,128]
colors=['red','palevioletred','darkcyan','mediumseagreen', 'slategray','blue','green','brown']

policies_list = ["ModeSup","ThModeSup","GroupSup","RareChunk","CommonChunk","BoostModeSup", "BoostThMode", "BoostGroupSup"]
labels_policies= ["ModeSup-1","ThModeSup-1","GroupSup-1","RareChunk-3","CommonChunk-3","Mode Sup-3", "Th Mode Sup-3", "Group Sup-3"]
mean_sojourn_time_matrix = [[] for x in policies_list]
var_sojourn_time_matrix =  [[] for x in policies_list]
for lamb in lambda_List:
    seeds_list = [10,2000,4000]
    (mean_sojourn, var_sojourn) = mean_sojourn_times(Output_MBP_Folder+"m5/",10000,policies_list , seeds_list,lamb)
    for idx,value in enumerate(mean_sojourn):
        mean_sojourn_time_matrix[idx].append(value)
    for idx,value in enumerate(var_sojourn):
        var_sojourn_time_matrix[idx].append(value)

fig,ax = plt.subplots(1,1,figsize=(12,6))

for idx in range(len(policies_list)):
    ax.plot(lambda_List, mean_sojourn_time_matrix[idx],'*-',linewidth=2,color=colors[idx],label=labels_policies[idx],alpha=0.8)
    # ax.errorbar(lambda_List, mean_sojourn_time_matrix[idx],yerr=var_sojourn_time_matrix[idx],marker='*',linewidth=2,color=colors[idx],label=policies_list[idx],alpha=0.8)
    ax.legend(fontsize=12)
ax.set_xlabel("$\lambda$",fontsize=16)
ax.set_ylabel("Mean Sojourn time",fontsize=16)
ax.set_ylim([5,14])
ax.set_title("$m=5, U=1, \mu=1$",fontsize=16)
plt.savefig(RESULTS_FOLDER_NAME+"sojourn-lambda.pdf",dpi=300)
