from analyse import *


def find_mixing_time(file_name):
    '''
    Returns mixing time by analyzing the steady state distribution.
    The idea is to read 1000 entries and check the stationary distribution is smae in those entries
    '''

    file_ptr = open(file_name)
    for each_line in file_ptr:
        each_line = each_line.strip('\n')
        sontents_array = [int(x) for x in each_line.split(' ')]


find_mixing_time(External_Data_Folder +"m4/lambda4-GroupSup_full_state_seed_10.txt")
