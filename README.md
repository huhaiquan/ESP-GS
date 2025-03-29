# ESP-GS：A Geographic Space-Oriented Search Algorithm for Robust Placement of Edge Servers

This repository contains the implementation of **ESP-GS**, a novel algorithm for robust edge server placement (ESP) in Mobile Edge Computing (MEC) networks, optimizing both **user coverage** and **network robustness** by transforming the discrete edge server placement (ESP) problem into a continuous optimization task in geographic space.

## Constraint Relaxation

A **Constraint Relaxation** technique is first proposed to downscale the ESP problem from an *m*-dimensional combinatorial optimization to a *k*-dimensional continuous optimization problem, which solves the combinatorial explosion due to topology dependence of traditional methods and provides a scalable solution for `large-scale` MEC network deployment.

The original ESP problem is formally defined as:

$$\arg\max_{\mathbf{p}}CR(\mathbf{p})\quad\textbf{s.t.}\quad p_i\in \{0,1\},~\sum_{i=1}^mp_i=k$$

where $\mathbf{p}=\{p_1,...,p_i,...,p_m\}$ and $m$ is number of base stations. The relaxed ESP is reformulated as:

$$\arg\max_{NNS(X)}CR(NNS(X)),\quad \mathbf{p}=\{p_1,...,p_i,...,p_m\} = NNS(X)$$

where $X=\{x_1,...,x_j,...,x_k\}$ and $x_j=(lat_j,lon_j)\in \Omega \subseteq \mathbb{R}^{2}$. $||\mathbf{p}||_1=|X|=k$ is constant to satisfy the constraint of original ESP problem. The $NNS(*)$ formula is implemented using KD-tree structure.

## Random Failure Evaluation

To assess network robustness, a **Random Failure Evaluation** (RFEval) method and two corresponding metrics($AUC_{USN}$ and $AUC_{USR}$) are designed to simulate independent server failure events in the real-worl and to evaluate the changes in user service status before and after network failures, providing a quantified metric for network robustness. The experiment result shown in [Comparison of Network Robustness with RFEval](./Experiment%20Results/Comparison%20of%20Network%20Robustness%20with%20RFEval.xlsx).

Tab. 1 Comparison of the user survival number with different server failure rates

| Server Fault Rate |     0    |    0.1   |    0.2   |    0.3   |    0.4   |    0.5   |    0.6   |    0.7   |    0.8   |    0.9   | $AUC_{USN}$ |
|:-----------------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:-----------:|
|       Random      | 4562.22  | 4379.17  | 4169.95  | 3927.60  | 3638.67  | 3297.83  | 2898.01  | 2402.09  | 1790.09  | 1091.30  |   3066.08   |
|       Greedy      | 3311.18  | 3263.67  | 3210.11  | 3144.75  | 3068.15  | 2973.45  | 2844.79  | 2682.25  | 2397.84  | 1940.92  |   2836.21   |
|       ESP-A       | 3627.44  | 3555.79  | 3466.87  | 3376.88  | 3259.94  | 3123.49  | 2953.66  | 2738.87  | 2408.26  | 1886.81  |   2974.51   |
|       ESP-GS      | 4433.06  | 4395.82  | 4336.97  | 4253.85  | 4140.77  | 3970.97  | 3725.48  | 3351.77  | 2749.97  | 1920.08  |   3649.52   |

Tab. 2 Comparison of the user survival rate with different server failure rates

| Server Fault Rate |   0.1  |   0.2  |   0.3  |   0.4  |   0.5  |   0.6  |   0.7  |   0.8  |   0.9  | $AUC_{USR}$ |
|:-----------------:|:------:|:------:|:------:|:------:|:------:|:------:|:------:|:------:|:------:|:-----------:|
|       Random      | 0.9599 | 0.9140 | 0.8609 | 0.7976 | 0.7229 | 0.6352 | 0.5265 | 0.3924 | 0.2392 |    0.6721   |
|       Greedy      | 0.9857 | 0.9695 | 0.9497 | 0.9266 | 0.8980 | 0.8591 | 0.8101 | 0.7242 | 0.5862 |    0.8566   |
|       ESP-A       | 0.9802 | 0.9557 | 0.9309 | 0.8987 | 0.8611 | 0.8143 | 0.7550 | 0.6639 | 0.5201 |    0.8200   |
|       ESP-GS      | 0.9916 | 0.9783 | 0.9596 | 0.9341 | 0.8958 | 0.8404 | 0.7561 | 0.6203 | 0.4331 |    0.8233   |

## Installation

1. **Prerequisites**:  
   - Java JDK 1.8+
   - Eclipse
2. **Clone the repository**:

   ```bash
   git clone https://github.com/huhaiquan/ESP-GS.git
   cd ESP-GS
   ```
