package ComparisonExperiments;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import ESP_GS.ESP_GS;
import KDTreeUtils.*;
import model.*;
import utils.*;

public class Set_ServerBudget_main {

	private static int times = 2;
	private static int randFailureTestTimes = 50;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws CloneNotSupportedException, IOException {
		String metroBaseStationFile = "data/Metropolitan/melb_metro_station.csv";
		String metroUserFile = "data/Metropolitan/melb_metro_user.csv";
		ArrayList<BaseStation> metroBaseStations = new ArrayList<>();
		metroBaseStations = TestUtils.readBaseStationData(metroBaseStationFile);
		ArrayList<User> metroUsers = new ArrayList<>();
		metroUsers = TestUtils.readUserData(metroUserFile);
		KDTree<Integer> kdTree = new KDTree<>();

		for (int nServer = 60; nServer <= 180; nServer += 20) {

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("ComparisonResult_ServerBudget-" + nServer + ".csv", true)));

			ConstNum.nBaseStation = 600; // 600(400-1000)
			ConstNum.nServer = nServer; // 100(60-180)
			ConstNum.nUser = 6000; // 6000(4000-10000)
			System.out.println(ConstNum.nBaseStation + "\t" + ConstNum.nServer + "\t" + ConstNum.nUser);

			int swarmSize = 60;
			int maxIterations = 400;

			TestItem item = new TestItem();

			double ART_rate = 1;
			int time = 0;
			while (time < times) {
//				System.out.println("第" + (time + 1) + "次重复试验");
				// generate data set
				BaseStation[] subBaseStations = TestUtils.generateBSList(metroBaseStations);
				User[] subUsers = RandomGenerateDataUtils.generateSubUser(subBaseStations, metroUsers);
				// Store the user list indexed by base station and the base station list indexed by user
				ArrayList<Integer>[] userListOfBS = new ArrayList[ConstNum.nBaseStation];
				ArrayList<Integer>[] BSListOfUser = new ArrayList[ConstNum.nUser];
				for (int i = 0; i < BSListOfUser.length; i++)
					BSListOfUser[i] = new ArrayList<>();

				for (int i = 0; i < subBaseStations.length; i++) {
					userListOfBS[i] = new ArrayList<Integer>();
					for (int j = 0; j < subUsers.length; j++) {
						if (Location.getDistance(subBaseStations[i].getLocation(),
								subUsers[j].getLocation()) < subBaseStations[i].getRadius() * ART_rate) {
							userListOfBS[i].add(j);
							BSListOfUser[j].add(i);
						}
					}
				}

				// KD-Tree
				List<Node<Integer>> nodeList = new ArrayList<>();
				for (int i = 0; i < subBaseStations.length; i++) {
					Location loc = subBaseStations[i].getLocation();
					nodeList.add(new Node<Integer>(i, loc.getLat(), loc.getLng()));
				}
				kdTree.buildTree(nodeList);

				item.setAlgorithm(new ESP_GS(swarmSize, maxIterations, subBaseStations, kdTree, BSListOfUser, userListOfBS));
				bw.append(time + ",");
				
				// Solve ESP Problem
				Result result = item.algorithm.execute();

				// Random Failure Test
				double[] testResult = RandFailureTestUtils.execute(result, userListOfBS, randFailureTestTimes);
				bw.append(item.getAlgorithm().getClass().getSimpleName() + "," + result.getObjValue() + ","
							+ result.getCoverage() + "," + result.getRobustness() + "," + result.getTimeConsumption()
							+ "," + testResult[0] + "," + testResult[1] + "," + testResult[2] + ",");
				item.addResult(time + 1, result, testResult[0], testResult[1], testResult[2]);
				
				bw.newLine();
				bw.flush();
				time++;
			}
			
			bw.append("Algorithm,Obj,Coverage,Robustness,Time,ServeredUserBefore,ServeredUserAfter,InvolvedUsers");
			bw.newLine();
			bw.append(item.getAlgorithm().getClass().getSimpleName() + "," + item.avgObj + "," + item.avgC
						+ "," + item.avgR + "," + item.avgTime + "," + item.avgServeredUserBefore
						+ "," + item.avgServeredUserAfter + "," + item.avgInvolvedUsers);
			bw.newLine();
			bw.close();
		}
	}
}
