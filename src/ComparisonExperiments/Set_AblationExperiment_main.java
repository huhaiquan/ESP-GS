package ComparisonExperiments;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import ESP_GS.ESP_GS;
import ESP_GS.ESP_GS_nonARS;
import ESP_GS.ESP_GS_nonAlignment;
import ESP_GS.ESP_GS_nonKDTree;
import KDTreeUtils.KDTree;
import KDTreeUtils.Node;
import model.BaseStation;
import model.ConstNum;
import model.Location;
import model.Result;
import model.User;
import utils.RandomGenerateDataUtils;
import utils.TestUtils;

public class Set_AblationExperiment_main {

	private static int times = 2;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws CloneNotSupportedException, IOException {
		String metroBaseStationFile = "data/Metropolitan/melb_metro_station.csv";
		String metroUserFile = "data/Metropolitan/melb_metro_user.csv";
		ArrayList<BaseStation> metroBaseStations = new ArrayList<>();
		metroBaseStations = TestUtils.readBaseStationData(metroBaseStationFile);
		ArrayList<User> metroUsers = new ArrayList<>();
		metroUsers = TestUtils.readUserData(metroUserFile);
		KDTree<Integer> kdTree = new KDTree<>();
		
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("AblationExperiment_600-100-6000.csv", true)));

		ConstNum.nBaseStation = 600;
		ConstNum.nServer = 100;
		ConstNum.nUser = 6000;
		int swarmSize = 60;
		int maxIterations = 400;
		
		TestItem[] items = new TestItem[4];
		for (int i = 0; i < items.length; i++) {
			items[i] = new TestItem();
		}
		
		int time = 0;
		while (time < times) {
			System.out.println("第" + (time + 1) + "次重复试验");
			// generate data set
			BaseStation[] subBaseStations = TestUtils.generateBSList(metroBaseStations);
			User[] subUsers = RandomGenerateDataUtils.generateSubUser(subBaseStations, metroUsers);

			// 保存ART初始化所需的整个环境
			ArrayList<Integer>[] userListOfBS = new ArrayList[ConstNum.nBaseStation];
			ArrayList<Integer>[] BSListOfUser = new ArrayList[ConstNum.nUser];
			for (int i = 0; i < BSListOfUser.length; i++)
				BSListOfUser[i] = new ArrayList<>();

			for (int i = 0; i < subBaseStations.length; i++) {
				userListOfBS[i] = new ArrayList<Integer>();
				for (int j = 0; j < subUsers.length; j++) {
					if (Location.getDistance(subBaseStations[i].getLocation(),
							subUsers[j].getLocation()) < subBaseStations[i].getRadius()) {
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

			items[0].setAlgorithm(new ESP_GS(swarmSize, maxIterations, subBaseStations, kdTree, BSListOfUser, userListOfBS));
			items[1].setAlgorithm(new ESP_GS_nonARS(swarmSize, maxIterations, subBaseStations, kdTree, BSListOfUser, userListOfBS));
			items[2].setAlgorithm(new ESP_GS_nonAlignment(swarmSize, maxIterations, subBaseStations, kdTree, BSListOfUser, userListOfBS));
			items[3].setAlgorithm(new ESP_GS_nonKDTree(swarmSize, maxIterations, subBaseStations, BSListOfUser, userListOfBS));
			
			bw.append(time + ",");
			for (int j = 0; j < items.length; j++) {
				Result result = items[j].algorithm.execute();
				bw.append(items[j].getAlgorithm().getClass().getSimpleName() + "," + result.getObjValue() + "," +
						result.getCoverage() + "," + result.getRobustness() + "," + result.getTimeConsumption() + ",");
				bw.flush();
				items[j].addResult(time + 1, result);
			}
			bw.newLine();
			bw.flush();
			time++;
		}
		
		// Store the result at csv files
		bw.append("Algorithm,Obj,Coverage,Robustness,Time");
		bw.newLine();
		for (int i = 0; i < items.length; i++) {
			bw.append(items[i].getAlgorithm().getClass().getSimpleName()+ "," + items[i].avgObj + "," 
					+ items[i].avgC + "," + items[i].avgR + "," + items[i].avgTime);
			bw.newLine();
		}
		bw.close();
	}
}
