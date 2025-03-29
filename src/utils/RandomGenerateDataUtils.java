package utils;

import java.util.ArrayList;

import model.BaseStation;
import model.ConstNum;
import model.Location;
import model.User;

public class RandomGenerateDataUtils {
	
	public static User[] generateSubUser(BaseStation[] subBaseStations, ArrayList<User> UserList) {
		ArrayList<Integer> tmp = new ArrayList<>();
		for (int i = 0; i < UserList.size(); i++) {
			for (int j = 0; j < subBaseStations.length; j++) {
				if (Location.getDistance(subBaseStations[j].getLocation(), UserList.get(i).getLocation()) < subBaseStations[j].getRadius()) {
					tmp.add(i);
					break;
				}
			}
		}
		
		User[] subUserList = new User[ConstNum.nUser];
		int i = 0;
		if(tmp.size() <= ConstNum.nUser) {
			for (; i < tmp.size(); i++) {
				subUserList[i] = UserList.get(tmp.get(i));
			}
			
			while (i < ConstNum.nUser) {
				int rand = (int)(Math.random() * UserList.size());
				if(!tmp.contains(rand)) {
					subUserList[i] = UserList.get(rand);
					i++;
					tmp.add(rand);
				}
			}
		} else {
			while (i < ConstNum.nUser) {
				int rand = (int)(Math.random() * tmp.size());
				subUserList[i] = UserList.get(tmp.get(rand));
				tmp.remove(rand);
				i++;
			}
		}
		
		return subUserList;
	}

	public static BaseStation[] generateSubBaseStation_nearBS(ArrayList<Integer>[] BSGraph, ArrayList<BaseStation> BSList){
		ArrayList<Integer> BSIndexs = new ArrayList<>();
		ArrayList<Integer> BSCandidate = new ArrayList<>();
		int rand = (int)(Math.random() * ConstNum.nBaseStation);
		BSIndexs.add(rand);
		BSCandidate.addAll(BSGraph[rand]);
		
		while (true) {
			if (BSIndexs.size() + BSCandidate.size() <= ConstNum.nBaseStation) {
				BSIndexs.addAll(BSCandidate);
				ArrayList<Integer> tmp = new ArrayList<>();
				tmp.addAll(BSCandidate);
				BSCandidate.clear();
				for (int i = 0; i < tmp.size(); i++) {
					for (Integer index : BSGraph[tmp.get(i)]) {
						if (!BSCandidate.contains(index) && !BSIndexs.contains(index)) {
							BSCandidate.add(index);
						}
					}
				}
			} else {
				while(BSIndexs.size() < ConstNum.nBaseStation) {
					rand =  (int) (Math.random() * BSCandidate.size());
					BSIndexs.add(BSCandidate.get(rand));
					BSCandidate.remove(rand);
				}
				break;
			}
		}
				
		BaseStation[] subBSList = new BaseStation[ConstNum.nBaseStation];
		for (int i = 0; i < subBSList.length; i++) {
			subBSList[i] = BSList.get(BSIndexs.get(i));
		}
		return subBSList;
	}
	
	public static BaseStation[] generateSubBaseStation_RandAndOverlap(ArrayList<Integer>[] BSGraph, ArrayList<BaseStation> BSList) {
		ArrayList<Integer> BSIndexs = new ArrayList<>();
		ArrayList<Integer> BSCandidate = new ArrayList<>();
		int rand = (int)(Math.random() * ConstNum.nBaseStation);
		BSIndexs.add(rand);
		BSCandidate.addAll(BSGraph[rand]);
		
		while (BSIndexs.size() < ConstNum.nBaseStation) {
			rand = (int)(Math.random() * BSCandidate.size());
			BSIndexs.add(BSCandidate.get(rand));
			for (Integer candiIndex : BSGraph[BSCandidate.get(rand)]) {
				if(!BSCandidate.contains(candiIndex) && !BSIndexs.contains(candiIndex)) {
					BSCandidate.add(candiIndex);
				}
			}
			BSCandidate.remove(rand);
		}
		
		BaseStation[] subBSList = new BaseStation[ConstNum.nBaseStation];
		for (int i = 0; i < subBSList.length; i++) {
			subBSList[i] = BSList.get(BSIndexs.get(i));
		}
		return subBSList;
	}
}
