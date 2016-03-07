import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AprioriTreeNode {

	private int noOfBranches;
	int candidateCount;
	ChildNode root;

	public AprioriTreeNode(int noOfBranches) {
		this.noOfBranches = noOfBranches;
		root = new ChildNode();
	}

	public void insertCandidates(List<Integer> candidate) {
		candidateCount++;
		insertCandidates(root, candidate, 0);
	}

	private void insertCandidates(ChildNode node, List<Integer> candidate,
			int index) {
		byte b = (byte) 0;
		int branchIndex = candidate.get(index) % noOfBranches;
		LeafNode nextNode = (LeafNode) (node).childs[branchIndex];
		if (nextNode == null) {
			nextNode = new LeafNode();
			(node).childs[branchIndex] = (LeafNode) nextNode;
			nextNode.mapCandidates.put(candidate, b);
		} else {
			nextNode.mapCandidates.put(candidate, b);
		}
	}

	public void updateSupportCount(List<Integer> transaction) {
		updateSupportCount(transaction, root);
	}

	private void updateSupportCount(List<Integer> transaction, ChildNode node) {

		for (int i = 0; i < transaction.size(); i++) {
			int itemI = transaction.get(i);
			int branchIndex = itemI % noOfBranches;
			Node nextNode = node.childs[branchIndex];
			if (nextNode == null) {
			} else {
				LeafNode leafNode = (LeafNode) nextNode;
				Set<Entry<List<Integer>, Byte>> entrySet = leafNode.mapCandidates
						.entrySet();
				if (entrySet != null) {
					for (Entry<List<Integer>, Byte> entry : entrySet) {
						if (transaction.subList(i, transaction.size())
								.containsAll(entry.getKey())) {
							leafNode.mapCandidates.compute(entry.getKey(), (a,
									b) -> (byte) (b + 1));

						}
					}
				}
			}
		}
	}

}

class Node {
}

class ChildNode extends Node {
	Node childs[] = new Node[5000];
}

class LeafNode extends Node {
	Map<List<Integer>, Byte> mapCandidates = new ConcurrentHashMap<List<Integer>, Byte>();
	LeafNode nextNode = null;
}
