package model;

public class Solution implements Comparable<Solution>{
	/**
	 * Stores the decision variable (location) of this particle
	 */
	private double[][] position = new double[ConstNum.nServer][2];
	
	/**
	 * Stores the velocity of this particle
	 */
	private double[][] velocity = new double[ConstNum.nServer][2];
	
	/**
	 * Stores the user coverage of this particle (obj1)
	 */
	private double coverage = 0.0;
	
	/**
	 * Stores the network robustness of this particle (obj2)
	 */
	private double robustness = 0.0;
	
	/**
	 * Stores the comprehensive objective value of two objectives
	 */
	private double fitness = 0.0;
	
	public Solution() {}
	
	public Solution(double[][] position) {
		this.position = position;
	}
	
	public double getCoverage() {
		return coverage;
	}

	public double getRobustness() {
		return robustness;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public void setRobustness(double robustness) {
		this.robustness = robustness;
	}

	public double[][] getPosition() {
		return position;
	}

	public void setPosition(double[][] position) {
		this.position = position;
	}

	public double[][] getVelocity() {
		return velocity;
	}

	public void setVelocity(double[][] velocity) {
		this.velocity = velocity;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	@Override
	public String toString() {
		return "Fitness:" + this.fitness + "\tCoverage:" + this.coverage + "\tRobustness:" + this.robustness;
	}

	@Override
	public int compareTo(Solution s) {
		if(this.fitness > s.getFitness()) {
			return 1;
		}else if (this.fitness < s.getFitness()) {
			return -1;
		}else {
			return 0;
		}
	}

	@Override
	public Solution clone() throws CloneNotSupportedException {
		Solution s = new Solution();
		double[][] position = new double[this.position.length][2];
		for (int i = 0; i < position.length; i++) {
			position[i][0] = this.position[i][0];
			position[i][1] = this.position[i][1];
		}
		s.setPosition(position);
		s.setCoverage(coverage);
		s.setRobustness(robustness);
		s.setFitness(fitness);
		s.setVelocity(velocity);
		return s;
	}
	
}
