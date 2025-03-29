package model;

import java.util.ArrayList;
import java.util.HashSet;

public class Result {
	private double obj_value = 0;
	private double coverage = 0;
	private double robustness = 0;
	private double timeConsumption = 0;
	private int[] decision = new int[ConstNum.nBaseStation];

	public double betaSquared = 1;

	public Result() {}

	public Result(Result result) {
		this.decision = result.decision;
		this.obj_value = result.obj_value;
		this.coverage = result.coverage;
		this.robustness = result.robustness;
		this.timeConsumption = result.timeConsumption;
	}
	
	public static int getNumServers(Result result) {
		int sum = 0;
		for (int i = 0; i < result.getDecision().length; i++) {
			if (result.getDecision()[i] != 0) {
				sum++;
			}
		}
		return sum;
	}

	public void calculate_CR(ArrayList<Integer>[] userListOfBS, ArrayList<Integer>[] BSListOfUser) {
		HashSet<Integer> users = new HashSet<>();
		for (int i = 0; i < ConstNum.nBaseStation; i++) {
			if (this.decision[i] == 1) {
				users.addAll(userListOfBS[i]);
			}
		}
		
		this.coverage = 1.0 * users.size() / ConstNum.nUser;
		
		double robust = 0;
		for (Integer u:users) {
			int tmp = 0;
			for (int bs = 0; bs < BSListOfUser[u].size(); bs++) {
				if (decision[BSListOfUser[u].get(bs)] == 1) {
					tmp++;
				}
			}
			robust += 1 - 1.0 / tmp;
		}
		this.robustness = 1.0 * robust / users.size();
	}

	public void calculateObjValue() {
		this.obj_value = ((1 + betaSquared) * coverage * robustness / (betaSquared * coverage + robustness));
	}

	public int[] getDecision() {
		return decision;
	}

	public void setDecision(int[] decision) {
		this.decision = decision;
	}

	/**
	 * @return the coverage
	 */
	public double getCoverage() {
		return coverage;
	}

	/**
	 * @return the robustness
	 */
	public double getRobustness() {
		return robustness;
	}

	/**
	 * @return the objective's value
	 */
	public double getObjValue() {
		return obj_value;
	}
	
	public double getTimeConsumption() {
		return timeConsumption;
	}

	public void setTimeConsumption(double timeConsumption) {
		this.timeConsumption = timeConsumption;
	}
	
	public void setBetaSquared(double betaSquared) {
		this.betaSquared = betaSquared;
		
	}
	
	@Override
	public String toString() {
		return "Obj:" + obj_value + "\tCoverage:" + coverage + "\tRobustness:" + robustness + "\tTime:"+ timeConsumption;
	}
	
	
}
