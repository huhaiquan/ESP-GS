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

public class ESP_GS_nonARS implements Algorithm{

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
	 * Stores the accessing relationship between base stations and users for initializing swarm
	 */
	private ArrayList<Integer>[] userListOfBS;
	
	/**
	 * Stores the accessing relationship between users and base stations for initializing swarm
	 */
	private ArrayList<Integer>[] BSListOfUser;

	/**
	 * Stores a KDTree based on the latitude and longitude of base stations
	 */
	private KDTree<Integer> kdTree;
	
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

	public ESP_GS_nonARS(int swarmSize, int maxIterations, BaseStation[] BSList, KDTree<Integer> kdTree) {
		this.swarmSize = swarmSize;
		this.maxIterations = maxIterations;
		this.BSList = BSList;
		this.kdTree = kdTree;
		particles = new Solution[this.swarmSize];
		pbests = new Solution[this.swarmSize];
		gbest = new Solution();
	}
	
	public ESP_GS_nonARS (int swarmSize, int maxIterations, BaseStation[] BSList, KDTree<Integer> kdTree,
			ArrayList<Integer>[] BSListOfUser, ArrayList<Integer>[] userListOfBS) {
		this(swarmSize, maxIterations, BSList, kdTree);
		this.BSListOfUser = BSListOfUser;
		this.userListOfBS = userListOfBS;
	}
	
	@Override
	public Result execute() throws CloneNotSupportedException {
		iteration = 0;
		double timeStart = System.currentTimeMillis();
		initSwarm();
		positionAlignment();
		evaluteSwarm();
		updateBests();
		while(iteration < maxIterations) {
//			System.out.println("µÚ" + (iteration + 1) + "´Îµü´ú");
			updateSwarm();
			evaluteSwarm();
			updateBests();
			updateParams();
			iteration++;
		}
		double timeEnd = System.currentTimeMillis();
		
		Result result = new Result();
		result.setTimeConsumption(timeEnd-timeStart);
		HashSet<Integer> servers = getPositionByKDTree(gbest.getPosition());
		int[] decision = new int[ConstNum.nBaseStation];
		for (int i : servers) decision[i] = 1;
		result.setDecision(decision);
		result.calculate_CR(userListOfBS,BSListOfUser);
		result.calculateObjValue();
		
		return result;
	}
	
	/**
	 * Initialize the first generation of particle swarm
	 */
	private void initSwarm() {
		initSwarm_Random();
	}

	private void initSwarm_Random() {
		// Initialize the particles randomly
		HashSet<Integer> selectedBS = new HashSet<>();
		for (int i = 0; i < particles.length; i++) {
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
	}
	
	private void positionAlignment() {
		// Take the position vector of the first particle as the reference
		double[][] referPos = particles[0].getPosition();
		for (int i = 1; i < particles.length; i++) {
			double[][] currPos = particles[i].getPosition(); // Current position vector
			double[][] tmpPos = new double[ConstNum.nServer][2]; // Aligned position vector
			// Make the alignment state of the coordinate points in the position vector
			boolean[] flag = new boolean[referPos.length]; 
			for (int j = 0; j < referPos.length; j++) {
				double minDist = Double.MAX_VALUE;
				int index = -1;
				// Find the closest of the unaligned coordinate point to the corresponding reference
				for (int k = 0; k < currPos.length; k++) {
					if (flag[k]) // Ignore aligned coordinate points
						continue; 
					double dist = (referPos[j][0] - currPos[k][0]) * (referPos[j][0] - currPos[k][0])
							+ (referPos[j][1] - currPos[k][1]) * (referPos[j][1] - currPos[k][1]);
					if (minDist > dist) { // Update info if closer coordinate are found
						minDist = dist;
						index = k;
						if (dist == 0)
							break;
					}
				}
				// Mark the point as aligned
				flag[index] = true;
				// Store the aligned position vector
				tmpPos[j][0] = currPos[index][0];
				tmpPos[j][1] = currPos[index][1];
			}
			// Update the position vector
			particles[i].setPosition(tmpPos);
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
			if(pbests[i] == null || particles[i].compareTo(pbests[i]) == 1) {
//				if (localBests[i] != null) {
//					System.out.println("Update personal best:" + pbests[i].getFitness() + 
//						" -> " + particles[i].getFitness());
//				}
				pbests[i] = particles[i].clone();
				if (pbests[i].compareTo(gbest) == 1) {
//					System.out.println("Update global best:" + gbest.getFitness() + 
//							" -> " + pbests[i].getFitness());
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
				velocity[j][0] = omega * particles[i].getVelocity()[j][0] + 
						phi1 * Math.random() * (localD[j][0] - position[j][0]) + 
						phi2 * Math.random() * (globalD[j][0] - position[j][0]);
				
				velocity[j][1] = omega * particles[i].getVelocity()[j][1] + 
						phi1 * Math.random() * (localD[j][1] - position[j][1]) + 
						phi2 * Math.random() * (globalD[j][1] - position[j][1]);
				
				position[j][0] += velocity[j][0];
				position[j][1] += velocity[j][1];
			}
			particles[i].setVelocity(velocity);
			particles[i].setPosition(position);
		}
	}
	
	private void updateParams() {
		omega = omegaMin + (omegaMax - omegaMin) * (maxIterations - iteration)/maxIterations;
	}
	
	public Solution getGlobalBest() {
		return gbest;
	}
}
