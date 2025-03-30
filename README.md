# ESP-GS: A Geographic Space-Oriented Search Algorithm for Robust Placement of Edge Servers

This repository contains the implementation of **ESP-GS**, a novel algorithm for robust edge server placement (ESP) in Mobile Edge Computing (MEC) networks, optimizing both **user coverage** and **network robustness** by transforming the discrete edge server placement (ESP) problem into a continuous optimization task in geographic space. The experiment results are shown in [Comparison of Algorithm Performance](./Experiment%20Results/Comparison%20of%20Algorithm%20Performance.xlsx).

## Dataset

The dataset used in the experiment is [EUA dataset](https://github.com/PhuLai/eua-dataset), which includes location information for 1,465 base stations and 174,305 mobile users in the Melbourne metropolitan area, Australia. The experimental scenarios were constructed by varying three key parameters: the number of base stations $m$, the server budget $k$, and the number of mobile users $n$. The experimental datasets were generated through a two-step process: randomly selecting $m$ base stations and sampling $n$ users within the coverage areas of these selected base stations.

## Random Failure Evaluation

To assess network robustness, a **Random Failure Evaluation** (RFEval) method and two corresponding metrics($AUC_{USN}$ and $AUC_{USR}$) are designed to simulate independent server failure events in the real-worl and to evaluate the changes in user service status before and after network failures, providing a quantified metric for network robustness. The experiment results are shown in [Comparison of Network Robustness with RFEval](./Experiment%20Results/Comparison%20of%20Network%20Robustness%20with%20RFEval.xlsx).

The RFEval simulates different levels of server failures using a failure intensity parameter $\alpha\in[0.0,1.0]$, which consists of three key stages: (1) measuring the initial service capacity by counting the total number of users served under normal network operation; (2) simulating random server failures by inducing sudden disruptions in selected edge servers; (3) evaluating post-failure performance by determining the number of users that remain connected to the network after failures.

## Installation

In the experiments, all algorithms were implemented in Java on the Eclipse platform using JDK 1.8. The hardware environment consisted of an Intel Core i5-10210U processor and 16GB of RAM running on a 64-bit Windows 10 operation system.

1. **Prerequisites**:  
   - Java JDK 1.8+
   - Eclipse
2. **Clone the repository**:

   ```bash
   git clone https://github.com/huhaiquan/ESP-GS.git
   cd ESP-GS
   ```
