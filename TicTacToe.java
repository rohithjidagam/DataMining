package com.uh.cs.ai;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;

public class TicTacToe {
	// consider 6 most right digits only
	// 1341566 -> 7,6,3
	// 1370143 -> 1,1,7 -> 1,2,7
	// 1285397 -> 1,8,7
	// 1354349 -> 8,7,4
	// 1121212 -> 3,3,3 -> 3,4,5
	static int uhId = 354349;
	static boolean turn = true;
	static boolean intialTurn = false;
	static int xWins = 0;
	static int oWins = 0;
	static int draws = 0;
	static int bfsNode = 0;
	static int dfsNode = 0;
	static int DFSNodeCount = 0;

	public static void main(String[] args) {
		char[][] board = buildInitialState();
		Node root = new Node(board, countEmpties(board));
		recursiveCall(root, board, turn);

		System.out.println("Initial State");
		root.displayBoard();
		root.displayFinalStates(root);
		System.out.println("Final States::" + Node.count);
		System.out.println("Draws::" + draws);
		System.out.println("X Wins::" + xWins);
		System.out.println("O Wins::" + oWins);
		// dfs(root);
		 bfs(root);

		DFSNode(root);
	}

	public static int DFSNode(Node root) {
		if (root.getBranches() != null) {
			for (Node b : root.getBranches()) {
				if (checkWinner(b.getBoard(), 'X')
						|| checkWinner(b.getBoard(), 'O')) {
					b.displayBoard();
					System.exit(0);
					return DFSNodeCount;
					
				} else {
					DFSNodeCount++;
					DFSNode(b);
				}
			}
		}
		return DFSNodeCount;
	}

	public static void bfs(Node root) {

		List<Node> childrens = root.getBranches();
		int level = 1;
		int totalNodes = 1;

		while (!childrens.isEmpty()) {

			Node child = childrens.remove(0);
			totalNodes++;

			if (checkWinner(child.getBoard(), 'X')
					|| checkWinner(child.getBoard(), 'O')) {
				System.out.println("BFS final state at level" + level);
				System.out.println("Total nodes Traversed" + totalNodes);
				child.displayBoard();
				break;
			} else {
				List<Node> grandchildren = child.getBranches();
				level = (6 - grandchildren.size());
				childrens.addAll(grandchildren);
			}
		}

	}

	public static boolean dfs(Node root) {
		for (Node child : root.getBranches()) {
			if (checkWinner(child.getBoard(), 'X')
					|| checkWinner(child.getBoard(), 'O')) {
				child.displayBoard();
				System.exit(0);
			} else {
				dfs(child);
			}

		}
		return true;
	}

	private static char[][] buildInitialState() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(0, "00");
		map.put(1, "01");
		map.put(2, "02");
		map.put(3, "10");
		map.put(4, "11");
		map.put(5, "12");
		map.put(6, "20");
		map.put(7, "21");
		map.put(8, "22");
		Locale locale = new Locale("en", "UK");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		symbols.setGroupingSeparator('-');
		String pattern = "#,##";
		DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
		String number = decimalFormat.format(uhId);
		StringTokenizer stringTokenizer = new StringTokenizer(number, "-");
		char board[][] = new char[3][3];
		List<Integer> integers = new ArrayList<Integer>();
		while (stringTokenizer.hasMoreTokens()) {
			int num = Integer.parseInt(stringTokenizer.nextToken()) % 9;
			if (integers.contains(num)) {
				if (integers.contains(num + 1))
					integers.add(num + 2);
				else
					integers.add(num + 1);
			} else {
				integers.add(num);
			}
		}
		integers.forEach(integer -> {
			String str = map.get(integer);
			placeInitialState(board, str);
		});
		return board;
	}

	private static void placeInitialState(char[][] board, String str) {
		if (!intialTurn) {
			board[Integer.parseInt(tochar(str.charAt(0)).toString())][Integer
					.parseInt(tochar(str.charAt(1)).toString())] = 'X';
			intialTurn = true;
		} else {
			board[Integer.parseInt(tochar(str.charAt(0)).toString())][Integer
					.parseInt(tochar(str.charAt(1)).toString())] = 'O';
			intialTurn = false;
		}

	}

	private static void recursiveCall(Node parentNode, char[][] currentBoard,
			boolean nextTurn) {
		List<Node> childNodes = null;
		childNodes = new ArrayList<Node>();
		char[][] tempBoard = null;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tempBoard = copyBoard(currentBoard);
				if (nextTurn && tempBoard[i][j] == '\0') {
					placeMove(nextTurn, childNodes, tempBoard, i, j, 'O');
				} else if (!nextTurn && tempBoard[i][j] == '\0') {
					placeMove(nextTurn, childNodes, tempBoard, i, j, 'X');
				}
			}
		}
		parentNode.setBranches(childNodes);
	}

	private static void placeMove(boolean nextTurn, List<Node> childNodes,
			char[][] tempBoard, int i, int j, char ch) {
		tempBoard[i][j] = ch;
		Node childNode = new Node(tempBoard, countEmpties(tempBoard));
		boolean checkWinner = checkWinner(tempBoard, ch);
		if (checkWinner) {
			childNode.setFinal(true);
		}
		childNodes.add(childNode);
		checkDraws(childNode, checkWinner);

		checkWins(nextTurn, tempBoard, childNode, checkWinner);
	}

	private static void checkWins(boolean nextTurn, char[][] tempBoard,
			Node childNode, boolean checkWinner) {
		if (!checkWinner) {
			recursiveCall(childNode, tempBoard, !nextTurn);
		}
	}

	private static void checkDraws(Node childNode, boolean checkWinner) {
		if (childNode.getCountRemaining() == 0 && !checkWinner) {
			childNode.setFinal(true);
			draws++;
		}
	}

	private static boolean checkWinner(char[][] board, char ch) {
		Character c = new Character(ch);
		// check rows
		int i = 0;
		for (int j = 0; j < 3; j++) {
			if (tochar(board[j][i]).equals(c)
					&& tochar(board[j][i + 1]).equals(c)
					&& tochar(board[j][i + 2]).equals(c)) {
				if (ch == 'X') {
					return incrementXwins();
				} else if (ch == 'O') {
					return incrementOwins();
				}
			}
		}
		// check columns
		int k = 0;
		int j = 2;
		while (j >= 0) {
			if (tochar(board[k][j]).equals(c)
					&& tochar(board[k + 1][j]).equals(c)
					&& tochar(board[k + 2][j]).equals(c)) {
				if (ch == 'X') {
					return incrementXwins();
				} else if (ch == 'O') {
					return incrementOwins();
				}
			}
			j--;
		}
		// check daigonals
		if (tochar(board[0][0]).equals(c) && tochar(board[1][1]).equals(c)
				&& tochar(board[2][2]).equals(c)) {
			if (ch == 'X') {
				return incrementXwins();
			} else if (ch == 'O') {
				return incrementOwins();
			}
		}
		if (tochar(board[0][2]).equals(c) && tochar(board[1][1]).equals(c)
				&& tochar(board[2][0]).equals(c)) {
			if (ch == 'X') {
				return incrementXwins();
			} else if (ch == 'O') {
				return incrementOwins();
			}
		}
		return false;
	}

	private static boolean incrementOwins() {
		oWins++;
		return true;
	}

	private static boolean incrementXwins() {
		xWins++;
		return true;
	}

	private static Character tochar(char c) {
		return new Character(c);
	}

	private static char[][] copyBoard(char[][] old) {
		char[][] newB = new char[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				newB[i][j] = old[i][j];
			}
		}
		return newB;
	}

	private static int countEmpties(char[][] board) {
		int count = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board[i][j] == '\0') {
					count++;
				}
			}
		}
		return count;
	}
}
