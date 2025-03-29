package model;

import java.util.ArrayList;

public class NodeWeight {

	private int stationIndex = -1;
	private double cr_cp = 0;
	private double coverage = 0;
	private double robustness = 0;
	private double betaSquared = 1;

	public NodeWeight() {

	}

	public NodeWeight(int stationIndex) {
		this.stationIndex = stationIndex;
	}

	public NodeWeight(NodeWeight node) {
		this.stationIndex = node.getStationIndex();
		this.cr_cp = node.getCR_CP();
		this.coverage = node.getCoverage();
		this.robustness = node.getRobustness();
	}

	public int getStationIndex() {
		return stationIndex;
	}

	public void setStationIndex(int stationIndex) {
		this.stationIndex = stationIndex;
	}

	/**
	 * @return the cr_cp
	 */
	public double getCR_CP() {
		return cr_cp;
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

	public void calculate_CR(ArrayList<Integer>[] userListOfBS, ArrayList<Integer>[] BSListOfUser) {
		this.coverage = 1.0 * userListOfBS[this.stationIndex].size() / ConstNum.nUser;
		double robust = 0;
		for (Integer u:userListOfBS[this.stationIndex]) {
			int tmp = 0;
			for (int bs = 0; bs < BSListOfUser[u].size(); bs++) {
				tmp++;
			}
			robust += 1 - 1.0 / tmp;
		}
		this.robustness = 1.0 * robust / userListOfBS[this.stationIndex].size();
	}

	public void calculateObjValue() {
		this.cr_cp = ((1 + betaSquared) * coverage * robustness / (betaSquared * coverage + robustness));
	}
}
