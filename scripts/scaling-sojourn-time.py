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

SCALING_WITH_LAMBDA = False
if(SCALING_WITH_LAMBDA):
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

def min_value_over_delta(m,mu,U,T):
    min_value = 1000000
    min_delta = 0
    delta_space = np.linspace(1,100.2,100)
    for delta in  delta_space:
        C_1 = (2*T -1)*(m-1)  + delta
        term1 = 2*U/(m*m)
        term2 = mu
        term3 = ( mu/(2*m*m) )*(C_1 - (2*T -1)*(m-1) )
        coefficeint = m*C_1/min(term1,term2,term3)
        if(coefficeint < min_value) :
            min_value = coefficeint
            min_delta = delta
    print(delta)
    return min_value

SCALING_COEFICIENT = True
if(SCALING_COEFICIENT):

    fig,ax = plt.subplots(1,1,figsize=(12,6))

    list_coefficeint = []
    T_space = np.linspace(1,200,100)
    # for T in T_space :
    for T in T_space:
        # delta =100
        m =5.0
        mu = 1.0
        U = 1.0
        min_coefficient = min_value_over_delta(m,mu,U,T)
        list_coefficeint.append(min_coefficient)

    plt.plot(T_space,list_coefficeint,'*-r',linewidth=3)
    plt.savefig(RESULTS_FOLDER_NAME + "./scaling_coefficient.pdf")
