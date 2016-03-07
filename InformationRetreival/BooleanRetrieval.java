import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class BooleanRetrieval {

	HashMap<String, Set<Integer>> invIndex;
	int[][] docs;
	HashSet<String> vocab;
	HashMap<Integer, String> map; // int -> word
	HashMap<String, Integer> i_map; // inv word -> int map

	public BooleanRetrieval() throws Exception {
		invIndex = new HashMap<String, Set<Integer>>();
		DatasetFormatter formater = new DatasetFormatter();
		formater.textCorpusFormatter("./all.txt");
		docs = formater.getDocs();
		vocab = formater.getVocab();
		map = formater.getVocabMap();
		i_map = formater.getInvMap();
	}

	void createPostingList() {
		for (String s : vocab) {
			invIndex.put(s, new TreeSet<Integer>());
		}
		for (int i = 0; i < docs.length; i++) {
			for (int j = 0; j < docs[i].length; j++) {
				String w = map.get(docs[i][j]);
				Set<Integer> postings = invIndex.get(w);
				postings.add(i + 1);
				invIndex.put(w, postings);
			}
		}
	}

	Set<Integer> intersection(Set<Integer> a, Set<Integer> b) {
		ArrayList<Integer> PostingList_a = null;
		ArrayList<Integer> PostingList_b = null;
		if (a != null && b != null) {
			PostingList_a = new ArrayList<Integer>(a);
			PostingList_b = new ArrayList<Integer>(b);
		} else {
			PostingList_a = new ArrayList<Integer>();
			PostingList_b = new ArrayList<Integer>();
		}
		TreeSet<Integer> result = new TreeSet<Integer>();
		int i = 0;
		int j = 0;

		while (i != PostingList_a.size() && j != PostingList_b.size()) {
			if (compare(PostingList_a, PostingList_b, i, j)) {
				i = addElement(result, PostingList_a, i);
				j++;
			} else if (PostingList_a.get(i) < PostingList_b.get(j)) {
				i++;
			} else {
				j++;
			}

		}
		return result;
	}

	private boolean compare(ArrayList<Integer> postingList_a,
			ArrayList<Integer> postingList_b, int i, int j) {
		if (postingList_a.get(i).compareTo(postingList_b.get(j)) == 0)
			return true;
		return false;
	}

	Set<Integer> evaluateANDQuery(String a, String b) {
		return intersection(invIndex.get(a), invIndex.get(b));
	}

	Set<Integer> union(Set<Integer> a, Set<Integer> b) {
		TreeSet<Integer> result = new TreeSet<Integer>();
		ArrayList<Integer> PostingList_a = null;
		ArrayList<Integer> PostingList_b = null;
		PostingList_a = checkIfNull(a);
		PostingList_b = checkIfNull(b);
		int i = 0;
		int j = 0;
		int size_a = PostingList_a.size();
		int size_b = PostingList_b.size();

		while (i != size_a || j != size_b) {
			if (i < size_a && j < size_b) {
				if (compare(PostingList_a, PostingList_b, i, j)) {
					i = addElement(result, PostingList_a, i);
					j++;
				} else if (PostingList_a.get(i) < PostingList_b.get(j)) {
					i = addElement(result, PostingList_a, i);
				} else {
					j = addElement(result, PostingList_b, j);
				}
			} else if (i >= size_a) {
				j = addElement(result, PostingList_b, j);
			} else {
				i = addElement(result, PostingList_a, i);
			}
		}
		return result;
	}

	private ArrayList<Integer> checkIfNull(Set<Integer> a) {
		if (a != null) {
			return new ArrayList<Integer>(a);
		} else {
			return new ArrayList<Integer>();
		}
	}

	private int addElement(TreeSet<Integer> result,
			ArrayList<Integer> PostingList_b, int k) {
		result.add(PostingList_b.get(k));
		k++;
		return k;
	}

	Set<Integer> evaluateORQuery(String a, String b) {
		return union(invIndex.get(a), invIndex.get(b));
	}

	Set<Integer> not(Set<Integer> a) {
		TreeSet<Integer> result = new TreeSet<Integer>();
		ArrayList<Integer> PostingList_a = null;
		if (a != null) {
			PostingList_a = new ArrayList<Integer>(a);
			int total_docs = docs.length;
			addBeforeElements(result, PostingList_a);
			addInBetweenElements(result, PostingList_a, total_docs);
			addAfterElements(result, total_docs,
					PostingList_a.get(PostingList_a.size() - 1));
		} else {
			for (int i = 1; i <= docs.length; i++) {
				result.add(i);
			}
		}
		return result;
	}

	private void addInBetweenElements(TreeSet<Integer> result,
			ArrayList<Integer> PostingList_a, int total_docs) {
		int posting = 0;
		for (int i = 0; i < PostingList_a.size() - 1; i++) {
			int diff = PostingList_a.get(i + 1) - PostingList_a.get(i);
			posting = PostingList_a.get(i) + 1;
			while (diff != 1 && posting <= total_docs) {
				result.add(posting++);
				diff--;
			}
		}
	}

	private void addAfterElements(TreeSet<Integer> result, int total_docs,
			Integer last) {
		for (int i = last + 1; i <= total_docs; i++) {
			result.add(i);
		}
	}

	private void addBeforeElements(TreeSet<Integer> result,
			ArrayList<Integer> PostingList_a) {
		for (int i = 1; i < PostingList_a.get(0); i++) {
			result.add(i);
		}
	}

	Set<Integer> evaluateNOTQuery(String a) {
		return not(invIndex.get(a));
	}

	Set<Integer> evaluateAND_NOTQuery(String a, String b) {
		return intersection(invIndex.get(a), not(invIndex.get(b)));
	}

	public static void main(String[] args) throws Exception {

		BooleanRetrieval model = new BooleanRetrieval();
		model.createPostingList();
		writeOutputToFile(args, model);
	}

	private static void writeOutputToFile(String[] args, BooleanRetrieval model)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				args[args.length - 1])));

		switch (args[0]) {
		case "PLIST":
			writer.write(args[1] + " -> " + model.invIndex.get(args[1]));
			break;
		case "AND":
			writer.write(args[1] + " AND " + args[3] + " -> "
					+ model.evaluateANDQuery(args[1], args[3]));
			break;
		case "OR":
			writer.write(args[1] + " OR " + args[3] + " -> "
					+ model.evaluateORQuery(args[1], args[3]));
			break;
		case "AND-NOT":
			writer.write(args[1]
					+ " AND "
					+ args[3]
					+ " "
					+ args[4]
					+ " -> "
					+ model.evaluateAND_NOTQuery(args[1],
							args[4].replace(")", "")));
			break;

		default:
			System.out.println("Invalid Query Type");
			break;
		}

		writer.close();
	}

}