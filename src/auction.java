import java.util.Arrays;

import lpsolve.*;

public class auction {
	
	private static String generateObjFuction(int[][] sellers, int[][] buyers) {
		String objFunction = "";
//		generate objective function
		for(int i=0; i<buyers.length; i++) {
			for(int j=0; j<sellers.length; j++) {
//				calculate objective function (bi - sj)
				int diff = (buyers[i][1] - sellers[j][1]);
				objFunction = objFunction + " " + diff;
			}
		}
		System.out.println("Objective Function: " + objFunction);
		return objFunction;
	}
	
	private static void generateConsts(LpSolve solver, int sellers[][], int buyers[][]) throws LpSolveException{
		//number of variables (number of sellers * number of buyers)
		int numberOfVariables = sellers.length * buyers.length;
//		System.out.println("length:" + numberOfVariables);		
//		generate buyers constraints
		for(int i=0; i<buyers.length; i++) {
			int buyerIndex;
			int[] buyerConstraint = new int[numberOfVariables];
			for(int j=0; j<sellers.length; j++) {
//				-------------------------------------------------------------
//				units sold to each buyers must be less or equal to their demand
				buyerIndex = i * sellers.length + j;
//				System.out.println("buyerID: [" + i + "] in array: " + buyerIndex);
				buyerConstraint[buyerIndex] = 1;
			}
//			add relation to constraint of each buyers------------
			String buyeri = Arrays.toString(buyerConstraint).replace("[", "").replace("]","").replace(",", " ");
			System.out.println("Buyer[" + i + "]: " + buyeri);
			solver.strAddConstraint(buyeri, LpSolve.LE, buyers[i][0]);
		}
		
//		generate sellers constraints
		for(int i=0; i<sellers.length; i++) {
			int sellerIndex;
			int[] sellerConstrant = new int[numberOfVariables];
			for(int j=0; j<buyers.length; j++) {
//				total units sold of each seller must be less or equal to their supply
				sellerIndex = i + j * sellers.length;
//				System.out.println("sellerID: [" + i + "] in array: " + sellerIndex);
				sellerConstrant[sellerIndex] = 1;				
			}
//			add relation to constraint of each sellers------------
			String selleri = Arrays.toString(sellerConstrant).replace("[", "").replace("]","").replace(",", " ");
			System.out.println("Buyer[" + i + "]: " + selleri);
			solver.strAddConstraint(selleri, LpSolve.LE, sellers[i][0]);

		}
//		set type int for each variable
		for(int i=0; i<numberOfVariables; i++) {
			solver.setInt((i+1), true);
		}
		
//		set bound for each variable
		int variableIndex = 1;
		for(int i=0; i<buyers.length; i++) {
			for(int j=0; j< sellers.length; j++) {
//				get MIN units between buyer i and seller j
				int temp = Math.min(buyers[i][0], sellers[j][0]);
//				System.out.println("variable[" + variableIndex + "]: " + temp);
				solver.setBounds(variableIndex, 0, temp);
				variableIndex++;
			}
		}
	} 
	
	public static int getNumberOfVariables(int[][] sellers, int[][] buyers) {
		System.out.println("Number of variables: " + (sellers.length * buyers.length));
		return (sellers.length * buyers.length);
	}
	
	public static int[][] cloneArray(int[][] src) {
	    int length = src.length;
	    int[][] target = new int[length][src[0].length];
	    for (int i=0; i<length; i++) {
	        System.arraycopy(src[i], 0, target[i], 0, src[i].length);
	    }
	    return target;
	}
	
	public static int[] arrayToInt(double[] source) {
		int[] target = new int[source.length];
		for(int i=0; i<source.length;i++) {
			target[i] = (int) source[i];
		}
		return target;
	}
	
	public static void recalculate(int[] allocation, int[][] sellers, int[][] buyers) {
		int[][] tempSellers = cloneArray(sellers);
		int[][] tempBuyers = cloneArray(buyers);
		
		for(int i=0; i<tempBuyers.length; i++) {
			int startIndex = i * tempSellers.length;
			int[] alloc = Arrays.copyOfRange(allocation, startIndex, (startIndex+tempSellers.length));
			if(tempBuyers[i][0] == Arrays.stream(alloc).sum()) {
				tempBuyers[i][0] = 0;
				tempBuyers[i][1] = 0;
			}
		}
		
		System.out.println("New buyers after 1st calculation: " + Arrays.deepToString(tempBuyers));
		
		for(int i=0; i<tempSellers.length; i++) {
			int[] alloc = new int[tempBuyers.length];
			for(int j=0; j<tempBuyers.length; j++) {
				int sellerIndex = i + j * tempSellers.length;
				alloc[j] = allocation[sellerIndex];
			}
			System.out.println("seller[" + i + "] = " + Arrays.toString(alloc));
			tempSellers[i][0] -= Arrays.stream(alloc).sum();
		}
		System.out.println("New seller after 1st calculation: " + Arrays.deepToString(tempSellers));
		
	}
	
	public static void main(String[] args) throws LpSolveException {
		int[][] sellers = new int[][]{	{10, 8}, {6,6}, {8,7}, {12,6}, {7, 5}};
		int[][] buyers = new int[][] {{10,10}, {8,7}, {5,9}, {15,6}};	
		
		LpSolve solver = LpSolve.makeLp(0, getNumberOfVariables(sellers, buyers));
		solver.strSetObjFn(generateObjFuction(sellers, buyers));
		
		generateConsts(solver, sellers, buyers);
		solver.setMaxim();
		solver.solve();
		
		System.out.println("Value of welfare: " + solver.getObjective());
	    int[] allocation = arrayToInt(solver.getPtrVariables());
	    int buyeri = 1;
	    for (int i = 0; i < allocation.length; i++) {
	    	if(i % sellers.length == 0) {
	    		System.out.println("--------- Buyer " + buyeri + " ---------");
	    		buyeri++;
	    	}
	        System.out.println("Seller [" + (i%sellers.length+1) + "] = " + allocation[i]);
	    }
	    recalculate(allocation, sellers, buyers);
		
	}
}
