package ESP_GS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import KDTreeUtils.KDTree;
import model.Algorithm;
import model.BaseStation;
import model.ConstNum;
import model.Location;
import model.Result;
import model.Solution;

public class ESP_GS_nonAlignment implements Algorithm {

	/**
	 * Stores the number of particle
	 */
	private int swarmSize;

	/**
	 * Stores the maximum number of iteration
	 */
	private int maxIterations;

	/**
	 * Stores the current number of iteration
	 */
	private int iteration = 0;

	/**
	 * Stores the current particles
	 */
	private Solution[] particles;

	/**
	 * Stores the local best of each particle
	 */
	private Solution[] pbests;

	/**
	 * Stores the global best of the whole swarm
	 */
	private Solution gbest;

	/**
	 * Stores the location info of base stations
	 */
	private BaseStation[] BSList;

	/**
	 * Stores the accessing relationship between base stations and users for
	 * initializing swarm
	 */
	private ArrayList<Integer>[] userListOfBS;

	/**
	 * Stores the accessing relationship between users and base stations for
	 * initializing swarm
	 */
	private ArrayList<Integer>[] BSListOfUser;

	/**
	 * Stores a KDTree based on the latitude and longitude of base stations
	 */
	private KDTree<Integer> kdTree;

	/**
	 * Stores the size of candidates used to initialize swarm
	 */
	private int candiSize = (int) (ConstNum.nBaseStation * 0.05) > 10 ? (int) (ConstNum.nBaseStation * 0.05) : 10;

	/**
	 * Stores the minimum of inertia weight
	 */
	private double omegaMin = 0.1;

	/**
	 * Stores the maximum of inertia weight
	 */
	private double omegaMax = 0.5;

	/**
	 * Stores the inertia weight of PSO algorithm
	 */
	private double omega = omegaMax;

	/**
	 * Stores the acceleration constant of the cognitive component
	 */
	private double phi1 = 2;

	/**
	 * Stores the acceleration constant of the social component
	 */
	private double phi2 = 2;

	/**
	 * Stores the weight coefficients of the fitness function
	 */
	private double betaSquared = 1;

	public ESP_GS_nonAlignment(int swarmSize, int maxIterations, BaseStation[] BSList, KDTree<Integer> kdTree) {
		this.swarmSize = swarmSize;
		this.maxIterations = maxIterations;
		this.BSList = BSList;
		this.kdTree = kdTree;
		particles = new Solution[this.swarmSize];
		pbests = new Solution[this.swarmSize];
		gbest = new Solution();
	}

	public ESP_GS_nonAlignment(int swarmSize, int maxIterations, BaseStation[] BSList, KDTree<Integer> kdTree,
			ArrayList<Integer>[] BSListOfUser, ArrayList<Integer>[] userListOfBS) {
		this(swarmSize, maxIterations, BSList, kdTree);
		this.BSListOfUser = BSListOfUser;
		this.userListOfBS = userListOfBS;
	}

	public ESP_GS_nonAlignment(int swarmSize, int maxIterations, BaseStation[] BSList, KDTree<Integer> kdTree,
			ArrayList<Integer>[] BSListOfUser, ArrayList<Integer>[] userListOfBS, int candiSize) {
		this(swarmSize, maxIterations, BSList, kdTree, BSListOfUser, userListOfBS);
		this.candiSize = candiSize;
	}

	@Override
	public Result execute() throws CloneNotSupportedException {
		iteration = 0;
		double timeStart = System.currentTimeMillis();
		initSwarm();
		evaluteSwarm();
		updateBests();
		while (iteration < maxIterations) {
//			System.out.println("µÚ" + (iteration + 1) + "´Îµü´ú");
			updateSwarm();
			evaluteSwarm();
			updateBests();
			updateParams();
			iteration++;
		}
		double timeEnd = System.currentTimeMillis();

		Result result = new Result();
		result.setTimeConsumption(timeEnd - timeStart);
		HashSet<Integer> servers = getPositionByKDTree(gbest.getPosition());
		int[] decision = new int[ConstNum.nBaseStation];
		for (int i : servers) {
			decision[i] = 1;
		}
		result.setDecision(decision);
		result.calculate_CR(userListOfBS, BSListOfUser);
		result.calculateObjValue();

		return result;
	}

	/**
	 * Initialize the first generation of particle swarm
	 */
	private void initSwarm() {
		initSwarm_mixed();
	}

	@SuppressWarnings("unchecked")
	private void initSwarm_mixed() {
		// Initialize the particles randomly
		HashSet<Integer> selectedBS = new HashSet<>();
		for (int i = 0; i < particles.length / 3; i++) {
			while (selectedBS.size() < ConstNum.nServer) {
				selectedBS.add((int) (Math.random() * ConstNum.nBaseStation));
			}
			double[][] decision = new double[ConstNum.nServer][2];
			int j = 0;
			for (Integer index : selectedBS) {
				decision[j][0] = this.BSList[index].getLocation().getLat();
				decision[j][1] = this.BSList[index].getLocation().getLng();
				j++;
			}
			selectedBS.clear();
			particles[i] = new Solution(decision);
		}
		// Initialize the remain particles using a ARS method
		for (int i = particles.length / 3; i < particles.length; i++) {
			// Create temporary copy using deep copy
			ArrayList<Integer>[] tmpUserListOfBS = new ArrayList[ConstNum.nBaseStation];
			ArrayList<Integer>[] tmpBSListOfUser = new ArrayList[ConstNum.nUser];

			for (int j = 0; j < userListOfBS.length; j++) {
				tmpUserListOfBS[j] = new ArrayList<>();
				tmpUserListOfBS[j].addAll(userListOfBS[j]);
			}

			for (int j = 0; j < BSListOfUser.length; j++) {
				tmpBSListOfUser[j] = new ArrayList<>();
				tmpBSListOfUser[j].addAll(BSListOfUser[j]);
			}

			HashSet<Integer> tmpCandidate = new HashSet<>();
			// 1. Get several candidate base station randomly,
			//    and select one of them with the most number of users.
			int candIndex = -1;
			int maxCandiCover = -1;
			int flag = 0;
			while (flag < candiSize) {
				int rand = (int) (Math.random() * ConstNum.nBaseStation);
				if (tmpCandidate.contains(rand))
					continue;
				else
					tmpCandidate.add(rand);

				if (candIndex == -1 || maxCandiCover < userListOfBS[rand].size()) {
					candIndex = rand;
					maxCandiCover = userListOfBS[rand].size();
				}
				flag++;
			}
			selectedBS.add(candIndex);
			tmpCandidate.clear();

			// 2. Delete users covered by the selected base station
			ArrayList<Integer> deletedUsers = new ArrayList<Integer>();
			deletedUsers.addAll(tmpUserListOfBS[candIndex]);
			for (Integer userIndex : deletedUsers) {
				// Update the infomation of tmpBSListOfUser and tmpUserListOfBS
				for (Integer bsIndex : tmpBSListOfUser[userIndex]) {
					tmpUserListOfBS[bsIndex].remove(userIndex);
				}
				tmpBSListOfUser[userIndex].clear();
			}
			deletedUsers.clear();

			// 3. Repeat the steps above, and generate server placement scheme incrementally
			while (selectedBS.size() < ConstNum.nServer) {
				candIndex = -1;
				maxCandiCover = -1;
				flag = 0;
				while (flag < candiSize) {
					int rand = (int) (Math.random() * ConstNum.nBaseStation);
					if (tmpCandidate.contains(rand) || selectedBS.contains(rand))
						continue;
					else
						tmpCandidate.add(rand);

					if (candIndex == -1 || maxCandiCover < userListOfBS[rand].size()) {
						candIndex = rand;
						maxCandiCover = userListOfBS[rand].size();
					}
					flag++;
				}
				selectedBS.add(candIndex);
				tmpCandidate.clear();

				// Delete users covered by the selected base station
				deletedUsers.addAll(tmpUserListOfBS[candIndex]);
				for (Integer userIndex : deletedUsers) {
					// Update the infomation of tmpBSListOfUser and tmpUserListOfBS
					for (Integer bsIndex : tmpBSListOfUser[userIndex]) {
						tmpUserListOfBS[bsIndex].remove(userIndex);
					}
					tmpBSListOfUser[userIndex].clear();
				}
				deletedUsers.clear();
			}

			double[][] location = new double[ConstNum.nServer][2];
			int j = 0;
			for (Integer index : selectedBS) {
				location[j][0] = this.BSList[index].getLocation().getLat();
				location[j][1] = this.BSList[index].getLocation().getLng();
				j++;
			}
			selectedBS.clear();
			particles[i] = new Solution(location);

			// Initialize the velocity of the particle
			double[][] velocity = new double[ConstNum.nServer][2];
			j = 0;
			for (; j < velocity.length; j++) {
				velocity[j][0] = 0;
				velocity[j][1] = 0;
			}
			particles[i].setVelocity(velocity);
		}
	}

	private void evaluteSwarm() {
		for (int i = 0; i < particles.length; i++) {
			evalute(particles[i]);
		}
	}

	private void evalute(Solution particle) {
		// Get the position vector of particle
		HashSet<Integer> servers = getPosition(particle.getPosition());
		
		// Coverage
		HashMap<Integer, Integer> users = new HashMap<Integer, Integer>();
		for (Integer s : servers) {
			for (Integer u : userListOfBS[s]) {
				users.put(u, users.getOrDefault(u, 0) + 1);
			}
		}
		double c = 1.0 * users.size() / ConstNum.nUser;
		particle.setCoverage(c);

		// Robustness
		double r = 0;
		for (Integer u : users.keySet()) {
			r += 1 - 1.0 / users.get(u);
		}
		r = 1.0 * r / users.size();
		particle.setRobustness(r);

		// Fitness
		particle.setFitness((1 + betaSquared) * c * r / (betaSquared * c + r));
	}

	private HashSet<Integer> getPosition(double[][] position) {
		return getPositionByKDTree(position);
	}

	// Matching geographic  coordinates to nearest neighbour base station coordinates using KDTree
	private HashSet<Integer> getPositionByKDTree(double[][] position) {
		HashSet<Integer> servers = new HashSet<>();
		// Clear Taboo Table of KDTree
		kdTree.getTabu().clear();
		HashSet<Integer> unmatchedLoc = new HashSet<>();
		for (int i = 0; i < position.length; i++)
			unmatchedLoc.add(i);

		HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
		while (!unmatchedLoc.isEmpty()) {
			map.clear();
			// Search the nearnest neighbor of each geographic coordinate
			// (Nearest-neighbour matching conflicts for multiple geographic coordinates possiblly)
			for (Integer i : unmatchedLoc) {
				Integer index = kdTree.findNearestNeighbour(position[i][0], position[i][1]);
				if (map.containsKey(index)) {
					map.get(index).add(i);
				} else {
					ArrayList<Integer> arrayList = new ArrayList<>();
					arrayList.add(i);
					map.put(index, arrayList);
				}
			}

			// Handle the geographic coordinates with matching conflicts
			for (Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet()) {
				Integer key = entry.getKey();
				// Add to the taboo table and ignore these already matched points in the next round
				kdTree.getTabu().add(key);
				servers.add(key);
				ArrayList<Integer> val = entry.getValue();
				if (val.size() == 1) {
					unmatchedLoc.remove(val.get(0));
				} else {
					// Find the geographic coordinate which closest to base station key in val
					BaseStation bs = BSList[key];
					double minDist = Location.getDistance(bs.getLocation(),
							new Location(position[val.get(0)][0], position[val.get(0)][1]));
					int minLocIndex = val.get(0);
					for (int i = 1; i < val.size(); i++) {
						double tmpDist = Location.getDistance(bs.getLocation(),
								new Location(position[val.get(i)][0], position[val.get(i)][1]));
						if (minDist > tmpDist) {
							minDist = tmpDist;
							minLocIndex = val.get(i);
						}
					}
					unmatchedLoc.remove(minLocIndex);
				}
			}
		}

		return servers;
	}

	private void updateBests() throws CloneNotSupportedException {
		for (int i = 0; i < particles.length; i++) {
			if (pbests[i] == null || particles[i].compareTo(pbests[i]) == 1) {
//				if (localBests[i] != null) {
//					System.out.println("Update personal best:" + pbests[i].getFitness() + 
//						" -> " + particles[i].getFitness());
//				}
				pbests[i] = particles[i].clone();
				if (pbests[i].compareTo(gbest) == 1) {
//					System.out.println(iteration + ": weight:" + omega + "\tUpdate global best:" + gbest.getFitness()
//							+ " -> " + pbests[i].getFitness());
					gbest = pbests[i];
				}
			}
		}
	}

	private void updateSwarm() {
		double[][] globalD = gbest.getPosition();
		for (int i = 0; i < swarmSize; i++) {
			double[][] velocity = particles[i].getVelocity();
			double[][] position = particles[i].getPosition();
			double[][] localD = pbests[i].getPosition();
			for (int j = 0; j < velocity.length; j++) {
				velocity[j][0] = omega * particles[i].getVelocity()[j][0]
						+ phi1 * Math.random() * (localD[j][0] - position[j][0])
						+ phi2 * Math.random() * (globalD[j][0] - position[j][0]);

				velocity[j][1] = omega * particles[i].getVelocity()[j][1]
						+ phi1 * Math.random() * (localD[j][1] - position[j][1])
						+ phi2 * Math.random() * (globalD[j][1] - position[j][1]);

				position[j][0] += velocity[j][0];
				position[j][1] += velocity[j][1];
			}
			particles[i].setVelocity(velocity);
			particles[i].setPosition(position);
		}
	}

	private void updateParams() {
		omega = omegaMin + (omegaMax - omegaMin) * (maxIterations - iteration) / maxIterations;
	}

	public Solution getGlobalBest() {
		return gbest;
	}
}

