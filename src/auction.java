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
	
	public static void main(String[] args) throws LpSolveException {
		int[][] sellers = new int[][]{	{10, 8}, {6,6}, {8,7}, {12,6}, {7, 5}};
		int[][] buyers = new int[][] {{10,10,100}, {8,7,56}, {5,10,45}, {15,6,90}};	
		
		LpSolve solver = LpSolve.makeLp(0, getNumberOfVariables(sellers, buyers));
		solver.strSetObjFn(generateObjFuction(sellers, buyers));
		
		generateConsts(solver, sellers, buyers);
		solver.setMaxim();
		solver.solve();
		
		System.out.println("Value of objective function: " + solver.getObjective());
	      double[] var = solver.getPtrVariables();
	      for (int i = 0; i < var.length; i++) {
	        System.out.println("Value of var[" + i + "] = " + (int) var[i]);
	      }
		
	}
}
