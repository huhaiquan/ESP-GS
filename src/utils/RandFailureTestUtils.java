package utils;

import java.util.ArrayList;
import java.util.HashSet;

import model.ConstNum;
import model.Result;

public class RandFailureTestUtils {
		
	public static double[] execute(Result result, ArrayList<Integer>[] userListOfBS, int times) {
		ArrayList<Integer> servers = new ArrayList<>();
		int[] decision = result.getDecision();
		for (int i = 0; i < decision.length; i++) {
			if (decision[i] == 1) {
				servers.add(i);
			}
		}
		
		double[] TestResult = new double[3];
		for (int i = 0; i < times; i++) {
			int[] tmp = getResult(servers, userListOfBS);
			TestResult[0] += tmp[0];
			TestResult[1] += tmp[1];
			TestResult[2] += tmp[2];
		}

		TestResult[0] = 1.0 * TestResult[0] / times;
		TestResult[1] = 1.0 * TestResult[1] / times;
		TestResult[2] = 1.0 * TestResult[2] / times;
		
		return TestResult;
	}
	
	private static int[] getResult(ArrayList<Integer> servers, ArrayList<Integer>[] userListOfBS) {
		// calculate the number of servers need to failure
		int failureNum = (int) (ConstNum.nServer * ConstNum.failureRate);
		ArrayList<Integer> failureServers = new ArrayList<>();
		
		// randomly select the failure servers
		while (failureServers.size() != failureNum) {
			int randIndex = (int)(Math.random() * servers.size());
			if (failureServers.contains(servers.get(randIndex))) continue;
			failureServers.add(servers.get(randIndex));
		}

		ArrayList<Integer> survivalServers = new ArrayList<>();
		survivalServers.addAll(servers);
		survivalServers.removeAll(failureServers);
		
		// the number of users corresponding to failure server
		HashSet<Integer> involvedUserList = new HashSet<>();
		for (Integer serverID : failureServers) {
			involvedUserList.addAll(userListOfBS[serverID]);
		}
		int involvedUserNum = involvedUserList.size();
		
		// calculate the number of user before server failuring
		HashSet<Integer> userListBefore = new HashSet<>();
		for (Integer serverID : servers) {
			userListBefore.addAll(userListOfBS[serverID]);
		}
		int userNumBefore = userListBefore.size();
		
		// calculate the number of user after server failuring
		HashSet<Integer> userListAfter = new HashSet<>();
		for (Integer serverID : survivalServers) {
			userListAfter.addAll(userListOfBS[serverID]);
		}
		int userNumAfter = userListAfter.size();

		// Result
		int[] result = new int[3];
		result[0] = userNumBefore;
		result[1] = userNumAfter;
		result[2] = involvedUserNum;
		
		return result;
	}
}
