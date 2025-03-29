package ComparisonExperiments;

import model.Algorithm;
import model.Result;

public class TestItem {
	public Algorithm algorithm;
	public double avgObj = 0;
	public double avgTime = 0;
	public double avgR = 0;
	public double avgC = 0;
	public double avgUserSurvivalRate = 0;
	public double avgServeredUserBefore = 0;
	public double avgServeredUserAfter = 0;
	public double avgInvolvedUsers = 0;
	public TestItem() {}

	public TestItem(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	public void addResult(int time, Result result, double serveredUserBefore, double serveredUserAfter, double involvedUsers) {
		addResult(time, result);
		this.avgServeredUserBefore = (this.avgServeredUserBefore * (time - 1) + serveredUserBefore) / time;
		this.avgServeredUserAfter = (this.avgServeredUserAfter * (time - 1) + serveredUserAfter) / time;
		this.avgInvolvedUsers = (this.avgInvolvedUsers * (time - 1) + involvedUsers) / time;
	}
	
	public void addResult(int time, Result result, double userSurvivalRate) {
		addResult(time, result);
		this.avgUserSurvivalRate = (this.avgUserSurvivalRate * (time - 1) + userSurvivalRate) / time;
	}
	
	public void addResult(int time, Result result) {
		this.avgObj = (this.avgObj * (time - 1) + result.getObjValue()) / time;
		this.avgTime = (this.avgTime * (time - 1) + result.getTimeConsumption()) / time;
		this.avgC = (this.avgC * (time - 1) + result.getCoverage()) / time;
		this.avgR = (this.avgR * (time - 1) + result.getRobustness()) / time;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
}
