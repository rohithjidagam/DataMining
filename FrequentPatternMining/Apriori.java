import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class Apriori {

	private static final int NoOFBranches = 5000;
	private static int count = 0;
	private static int min_sup;
	private static int input_k;
	private static int k;

	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis();

		min_sup = Integer.parseInt(args[0]);
		input_k = Integer.parseInt(args[1]);
		Map<String, Integer> stringToIntMap = new HashMap<String, Integer>();
		Map<Integer, Integer> itemCountMap = new HashMap<Integer, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(args[2]));
		List<List<Integer>> listOfTransactions = new ArrayList<List<Integer>>();

		readDataFromFile(stringToIntMap, itemCountMap, reader,
				listOfTransactions);
		String fileName = "out_s=" + min_sup + "_k=" + input_k + "+.txt";

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));

		List<Integer> frequent1 = calculateFrequent1Itemsets(itemCountMap,
				stringToIntMap, writer);

		calculateFrequentKItemsets(frequent1, listOfTransactions,
				stringToIntMap.entrySet(), writer);
		writer.close();

		System.out.println("Time Taken(sec)::"
				+ (System.currentTimeMillis() - startTime) / (1000));

	}

	private static void calculateFrequentKItemsets(List<Integer> frequent1,
			List<List<Integer>> listOfTransactions,
			Set<Entry<String, Integer>> entrySet, BufferedWriter writer)
			throws Exception {

		Map<List<Integer>, Integer> itemsets = new HashMap<List<Integer>, Integer>();
		AprioriTreeNode candidatesForKItemsets = null;
		k = 2;
		do {
			candidatesForKItemsets = generateCandidates(frequent1, itemsets,
					listOfTransactions);
			updateSupportCount(listOfTransactions, candidatesForKItemsets);
			computeFinalCandidates(candidatesForKItemsets, itemsets);
			if (k >= input_k) {
				writeOutputToFile(entrySet, itemsets, writer);
			}
			k++;
		} while (!itemsets.isEmpty());

	}

	private static void updateSupportCount(
			List<List<Integer>> listOfTransactions,
			AprioriTreeNode candidatesForKItemsets) {
		listOfTransactions.stream().forEach(transaction -> {
			if (transaction.size() >= k) {
				candidatesForKItemsets.updateSupportCount(transaction);
			}
		});

	}

	private static AprioriTreeNode generateCandidates(List<Integer> frequent1,
			Map<List<Integer>, Integer> itemsets,
			List<List<Integer>> listOfTransactions) throws Exception {
		AprioriTreeNode candidatesForKItemsets;
		if (k == 2) {
			candidatesForKItemsets = generateCandidatesFor2Itemsets(frequent1,
					listOfTransactions);
		} else {
			candidatesForKItemsets = generateCandidatesForKItemsets(itemsets, k);
		}
		return candidatesForKItemsets;
	}

	private static void writeOutputToFile(Set<Entry<String, Integer>> entrySet,
			Map<List<Integer>, Integer> itemsets, BufferedWriter writer)
			throws IOException {

		Set<Entry<List<Integer>, Integer>> set = itemsets.entrySet();
		for (Entry<List<Integer>, Integer> entry : set) {
			List<Integer> items = entry.getKey();
			for (Integer integer : items) {
				entrySet.stream().forEach(map -> {
					if (map.getValue() == integer) {
						try {
							writer.write(map.getKey() + " ");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			writer.write("(" + entry.getValue() + ")\n");
		}
	}

	private static void computeFinalCandidates(
			AprioriTreeNode candidatesForKItemsets,
			Map<List<Integer>, Integer> itemsets) {
		itemsets.clear();
		System.gc();
		for (int i = 0; i < NoOFBranches; i++) {
			LeafNode leafNode = (LeafNode) candidatesForKItemsets.root.childs[i];
			if (leafNode != null) {
				leafNode.mapCandidates
						.entrySet()
						.stream()
						.forEach(
								candidate -> {
									if (candidate.getValue() >= min_sup) {
										itemsets.put(candidate.getKey(),
												(int) candidate.getValue());
									}
								});
			}
		}
	}

	private static AprioriTreeNode generateCandidatesForKItemsets(
			Map<List<Integer>, Integer> itemsets, int k) {
		AprioriTreeNode candidatesKSizeTreeNode = new AprioriTreeNode(
				NoOFBranches);
		TreeSet<Integer> newItemset = null;
		Set<TreeSet<Integer>> finalItems = new HashSet<>();
		List<List<Integer>> candidates = new ArrayList<List<Integer>>();
		Set<Entry<List<Integer>, Integer>> entrySet = itemsets.entrySet();
		entrySet.stream().forEach(a -> {
			candidates.add(a.getKey());
		});
		for (int i = 0; i < candidates.size(); i++) {
			for (int j = i + 1; j < candidates.size(); j++) {
				newItemset = new TreeSet<Integer>();
				newItemset.addAll(candidates.get(i));
				newItemset.addAll(candidates.get(j));
				if (newItemset.size() == k && finalItems.add(newItemset)) {
					candidatesKSizeTreeNode
							.insertCandidates(new ArrayList<Integer>(newItemset));
				}
			}
		}
		return candidatesKSizeTreeNode;
	}

	private static AprioriTreeNode generateCandidatesFor2Itemsets(
			List<Integer> frequent1, List<List<Integer>> listOfTransactions) {
		AprioriTreeNode candidates2SizeTreeNode = new AprioriTreeNode(
				NoOFBranches);
		List<List<Integer>> lists = Collections
				.synchronizedList(new ArrayList<List<Integer>>());
		for (int i = 0; i < frequent1.size(); i++) {
			Integer item1 = frequent1.get(i);
			for (int j = i + 1; j < frequent1.size(); j++) {
				Integer item2 = frequent1.get(j);
				lists.add(Arrays.asList(item1, item2));
			}
		}
		frequent1.clear();
		System.gc();

		lists.stream().forEach(a -> {
			candidates2SizeTreeNode.insertCandidates(a);
		});
		return candidates2SizeTreeNode;
	}

	private static List<Integer> calculateFrequent1Itemsets(
			Map<Integer, Integer> itemCountMap,
			Map<String, Integer> stringToIntMap, BufferedWriter writer)
			throws IOException {
		List<Integer> frequent1 = new ArrayList<Integer>();
		itemCountMap.entrySet().stream().filter(k -> k.getValue() >= min_sup)
				.forEach(entry -> {
					frequent1.add(entry.getKey());
				});

		if (input_k == 1) {
			frequent1
					.stream()
					.forEach(
							integer -> {
								stringToIntMap
										.entrySet()
										.stream()
										.forEach(
												entry -> {
													if (entry.getValue() == integer) {
														try {
															writer.write(entry
																	.getKey()
																	+ " ("
																	+ itemCountMap
																			.get(integer)
																	+ ")\n");
														} catch (Exception e) {
															e.printStackTrace();
														}
													}
												});
							});
		}
		itemCountMap.clear();
		System.gc();
		return frequent1;
	}

	private static void readDataFromFile(Map<String, Integer> stringToIntMap,
			Map<Integer, Integer> itemCountMap, BufferedReader reader,
			List<List<Integer>> listOfTransactions) throws IOException {
		String eachLine = "";
		List<Integer> transaction = null;
		while ((eachLine = reader.readLine()) != null) {
			String[] splits = eachLine.split(" ");
			transaction = new ArrayList<Integer>();
			countNoOfOccurences(stringToIntMap, splits);
			convertStringsToInt(stringToIntMap, itemCountMap, splits,
					transaction);
			Collections.sort(transaction);
			listOfTransactions.add(transaction);
		}
		reader.close();
	}

	private static void convertStringsToInt(
			Map<String, Integer> stringToIntMap,
			Map<Integer, Integer> itemCountMap, String[] splits,
			List<Integer> transaction) {
		for (int i = 0; i < splits.length; i++) {
			transaction.add(stringToIntMap.get(splits[i]));
			if (!itemCountMap.containsKey(stringToIntMap.get(splits[i]))) {
				itemCountMap.put(stringToIntMap.get(splits[i]), 1);
			} else {
				itemCountMap.put(stringToIntMap.get(splits[i]),
						itemCountMap.get(stringToIntMap.get(splits[i])) + 1);
			}
		}
	}

	private static void countNoOfOccurences(
			Map<String, Integer> stringToIntMap, String[] splits) {
		for (String string : splits) {
			if (!stringToIntMap.containsKey(string)) {
				stringToIntMap.put(string, ++count);
			}
		}
	}

}
