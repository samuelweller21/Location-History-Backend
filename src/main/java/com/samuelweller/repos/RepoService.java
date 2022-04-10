//package com.samuelweller.repos;
//
//import java.util.List;
//
//import org.apache.commons.csv.CSVRecord;
//import org.springframework.beans.factory.annotation.Autowired;
//
//public class RepoService {
//
//	@Autowired
//	TestRepo repo;
//	
//	public RepoService(TestRepo repo) {
//		this.repo = repo;
//	}
//	
//	public void addToDB(String userID, List<CSVRecord> data) {
//		for (int i = 1; i < data.size(); i++) {  
////			List row = data.get(i).toList();
//			try {
////				repo.save(new Location(userID, 
////						Double.parseDouble(row.get(3).toString()),
////						Double.parseDouble(row.get(2).toString()),
////						Long.parseLong(row.get(1).toString())));
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("3 - " + row.get(3).toString());
//				System.out.println("2 - " + row.get(2).toString());
//				System.out.println("1 - " + row.get(1).toString());
//				break;
//			}
//		}
//	}
//	
//}
