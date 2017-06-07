import matplotlib.pyplot as plt
import itertools
import numpy as np
import pandas as pd
# import seaborn as sns
# sns.set(style="whitegrid")

FOLDER_NAME = r"../"

def read_no_pkts(filename):
    file_ptr = open(FOLDER_NAME+filename,'r')
    peer_evolution =[]
    for line in file_ptr:
        value = int(line.strip('\n '))
        peer_evolution.append(value)
    return peer_evolution

peer_evolution_1_df = pd.read_csv(FOLDER_NAME+"no-peers-US-2-lambda-1.5.txt",'\n')
peer_evolution_2_df = pd.read_csv(FOLDER_NAME+"no-peers-US-2-lambda-1.9.txt",'\n')
peer_evolution_3_df = pd.read_csv(FOLDER_NAME+"no-peers-US-2-lambda-2.5.txt",'\n')
peer_evolution_4_df = pd.read_csv(FOLDER_NAME+"no-peers-US-2-lambda-2.1.txt",'\n')

fig = plt.figure(figsize=(14, 6))
ax1 = fig.add_subplot(2, 2, 1)
ax2 = fig.add_subplot(2, 2, 2)
ax3 = fig.add_subplot(2, 2, 3)
ax4 = fig.add_subplot(2, 2, 4)

ax1.plot(peer_evolution_1_df.values,'b-',alpha=0.7,label=r"$U_s=2, \lambda=1.5$")
ax2.plot(peer_evolution_2_df.values,'g-',alpha=0.8,label=r"$U_s=2, \lambda=1.9$")
ax3.plot(peer_evolution_3_df.values,'r-',alpha=0.7,label=r"$U_s=2, \lambda=2.5$")
ax4.plot(peer_evolution_4_df.values,'k-',alpha=0.9,label=r"$U_s=2, \lambda=2.1$")

for ax in [ax1,ax2,ax3,ax4]:
    ax.grid(True)
    ax.set_xlabel("Time")
    ax.legend(loc=2)
    ax.set_ylabel("Number of Peers")
plt.savefig("peer-evolution-vanilla.pdf",dpi=300)
