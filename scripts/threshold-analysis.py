import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
import seaborn as sns
sns.set(style="whitegrid")
# plt.style.use('fivethirtyeight')
ewma = pd.stats.moments.ewma
seedsList = [10,2000,4000,4234,43204]
# Output_Data_Folder = r"/Volumes/Ghatotkacha/work/output-threshold/"
Output_Data_Folder = r"/Volumes/T5-Vamsee/P2P-Simulations/output-threshold/"
Output_MBP_Folder = r"../output/"
RESULTS_FOLDER_NAME = r"../results/"


def read_wait_times(file_name, stability_time ):
    waiting_times = []
    for each_line in open(file_name):
        each_line = each_line.strip("\n")
        count = each_line.split(':')[0]
        waiting_times.append(float(count))
    return waiting_times[stability_time:]

def mean_waiting_times_Thresholds(folder_name,stability_time,list_policies, threshold):
    mean_sojourn_times = []
    std_sojourn_times = []
    for policy in list_policies:
        waiting_time = []
        for seed in seedsList:
            file_name = folder_name+"T"+str(threshold)+"-"+ policy+"_waitingTime_seed_"+str(seed)+".txt"
            waiting_time = waiting_time+ read_wait_times(file_name, stability_time)
        mean_sojourn_times.append(np.mean(waiting_time))
        std_sojourn_times.append(np.std(waiting_time))
    return mean_sojourn_times,[0.85*x for x in  std_sojourn_times]

def mean_waiting_times_components(file_name, m, stability_time):
    waiting_times = []
    for idx in range(m):
        waiting_times.append([])

    for each_line in open(file_name):
        each_line = each_line.strip("\n")
        counts_array= [int(x) for x in each_line.split(':')[-1].split(' ')[:-2]]
        [waiting_times[idx].append(value) for idx,value in enumerate(counts_array)]
        [waiting_times[idx].append(value) for idx,value in enumerate(counts_array)]
        # print(waiting_times)
    print([np.mean(components_counts_array[stability_time:]) for components_counts_array in waiting_times] )
    return waiting_times[stability_time:]

ANALYZE_THRESHOLD = True
if(ANALYZE_THRESHOLD):
    fig,axs= plt.subplots(1,1,figsize=(14,8))

    for ax in [axs]:
        average_sojourn_times = []
        var_sojourn_times = []
        listThresholds = [1,2, 3,4, 5,6,10,15]
        for thresh in listThresholds:
            mean_time, var_time = mean_waiting_times_Thresholds(Output_Data_Folder+"k2lambda4/",1000,["ThModeSup"],thresh)
            average_sojourn_times.append(mean_time[0])
            var_sojourn_times.append(var_time[0])
        ax.plot(listThresholds, average_sojourn_times, 'k-*',label="m=2", alpha = 0.8)
        ax.fill_between(listThresholds,np.array(average_sojourn_times)- np.array(var_sojourn_times), np.array(average_sojourn_times)+np.array( var_sojourn_times), alpha=0.2,  facecolor='black', antialiased=True )


        average_sojourn_times = []
        var_sojourn_times = []
        listThresholds = [1,2,  5,6,10,15,25, 40]
        for thresh in listThresholds:
            mean_time, var_time = mean_waiting_times_Thresholds(Output_Data_Folder+"k5lambda4/",1000,["ThModeSup"],thresh)
            average_sojourn_times.append(mean_time[0])
            var_sojourn_times.append(var_time[0])
        ax.plot(listThresholds, average_sojourn_times, '-*',color='teal',label="m=5", alpha = 0.8)
        ax.fill_between(listThresholds,np.array(average_sojourn_times)- np.array(var_sojourn_times), np.array(average_sojourn_times)+np.array( var_sojourn_times), alpha=0.2,  facecolor='yellow', antialiased=True )

        average_sojourn_times = []
        var_sojourn_times = []
        listThresholds = [1,2, 4, 6, 8, 10,  12, 16,20,30, 50, 80, 100]
        for thresh in listThresholds:
            mean_time, var_time = mean_waiting_times_Thresholds(Output_Data_Folder+"k10lambda4/",1000,["ThModeSup"],thresh)
            average_sojourn_times.append(mean_time[0])
            var_sojourn_times.append(var_time[0])
        ax.plot(listThresholds, average_sojourn_times, 'r-*',label="m=10", alpha = 0.8)
        ax.fill_between(listThresholds,np.array(average_sojourn_times)- np.array(var_sojourn_times), np.array(average_sojourn_times)+np.array( var_sojourn_times), alpha=0.2,  facecolor='red', antialiased=True )


        listThresholds = [1, 8,  12, 16, 20, 25, 30,  50, 80, 100, 150,200,300 ]
        # listThresholds = [1, 8,  12, 16, 20, 25, 30]
        average_sojourn_times = []
        var_sojourn_times = []
        for thresh in listThresholds:
            mean_time, var_time = mean_waiting_times_Thresholds(Output_Data_Folder+"k25lambda4/",1000,["ThModeSup"],thresh)
            average_sojourn_times.append(mean_time[0])
            var_sojourn_times.append(var_time[0])
        ax.plot(listThresholds, average_sojourn_times, 'g-*',label="m=25")
        ax.fill_between(listThresholds,np.array(average_sojourn_times)- np.array(var_sojourn_times), np.array(average_sojourn_times)+np.array( var_sojourn_times), alpha=0.2, facecolor='green', antialiased=True )

        listThresholds = [1, 8 , 12, 20 , 100, 150,200, 300,500]
        # listThresholds = [1, 8,  12, 16, 20, 25, 30]
        average_sojourn_times = []
        var_sojourn_times = []
        for thresh in listThresholds:
            mean_time, var_time = mean_waiting_times_Thresholds(Output_Data_Folder+"k40lambda4/",1000,["ThModeSup"],thresh)
            average_sojourn_times.append(mean_time[0])
            var_sojourn_times.append(var_time[0])
        # ax.errorbar(listThresholds, average_sojourn_times,'b-*', yerr= var_sojourn_times,label="m=40")
        ax.fill_between(listThresholds,np.array(average_sojourn_times)- np.array(var_sojourn_times), np.array(average_sojourn_times)+np.array( var_sojourn_times), alpha=0.2,  facecolor='blue', antialiased=True )
        ax.plot(listThresholds, average_sojourn_times, 'b-*',label="m=40")

        ax.legend(fontsize=18)
        ax.set_xlabel("Threshold",fontsize=18)
        ax.set_ylabel("Average Sojourn Time",fontsize=18)

    # axs[1].set_xlim([0,50])
    # axs[1].set_ylim([0,60])
    axs.set_xlim([1,350])
    axs.set_ylim([0,100])
    ax
    axs.set_xticklabels([ int(x) for x in axs.get_xticks()], rotation=0, fontsize=18)
    axs.set_yticklabels([int(x) for x in axs.get_yticks()], rotation=0, fontsize=18)
    plt.tight_layout()
    plt.savefig(RESULTS_FOLDER_NAME+"threhsold_sojourn_time.pdf",dpi=300)



HISTOGRAM_WITH_COMPONENTS = False
if(HISTOGRAM_WITH_COMPONENTS):
    mean_waiting_times_components(Output_MBP_Folder+"k5lambda4/"+"T8-ModeSup_peers_seed_10.txt",5,5000)
    mean_waiting_times_components(Output_MBP_Folder+"k40lambda4/"+"T30-ModeSup_peers_seed_2000.txt",40,60000)
    mean_waiting_times_components(Output_MBP_Folder+"k40lambda4/"+"T30-ModeSup_peers_seed_4234.txt",40,60000)
    mean_waiting_times_components(Output_MBP_Folder+"k40lambda4/"+"T30-ModeSup_peers_seed_4000.txt",40,60000)
    mean_waiting_times_components(Output_MBP_Folder+"k40lambda4/"+"T30-ModeSup_peers_seed_43204.txt",40,60000)
