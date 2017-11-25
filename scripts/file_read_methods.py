import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
def read_wait_times(file_name, stability_time ):
    """
    Reads sojourn times from the file and return an array removing the first "stability_time" compnents
    """
    waiting_times = []
    for each_line in open(file_name):
        each_line = each_line.strip("\n")
        count = each_line.split(':')[0]
        waiting_times.append(float(count))
    return waiting_times[stability_time:]

def mean_sojourn_times(folder_name,stability_time,list_policies, seedsList,lam):
    """
    Returns the mean and variance of sojourn times of each policy listed in list_policies

    """
    mean_sojourn_times = []
    std_sojourn_times = []
    for policy in list_policies:
        waiting_time = []
        for seed in seedsList:
            file_name = folder_name+"lambda"+str(lam)+"-"+ policy+"_waitingTime_seed_"+str(seed)+".txt"
            waiting_time = waiting_time+ read_wait_times(file_name, stability_time)
        mean_sojourn_times.append(np.mean(waiting_time))
        std_sojourn_times.append(np.std(waiting_time))
    return mean_sojourn_times,[0.85*x for x in  std_sojourn_times]
