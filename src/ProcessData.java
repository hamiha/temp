import java.util.Arrays;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class ProcessData{
	
	public static String generateObjFuction(int[][] sellers, int[][] buyers) {
		String objFunction = "";
//		generate objective function
		for(int i=0; i<buyers.length; i++) {
			for(int j=0; j<sellers.length; j++) {
//				calculate objective function by (bi - sj)
				int diff = (buyers[i][1] - sellers[j][1]);
				objFunction = objFunction + " " + diff;
			}
		}
//		System.out.println("Objective Function: " + objFunction);
		return objFunction;
	}
	
	public static void generateConsts(LpSolve solver, int sellers[][], int buyers[][], String solution) throws LpSolveException{
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
//			System.out.println("Buyer[" + i + "]: " + buyeri);
			if(solution.charAt(i) == '0') {
				solver.strAddConstraint(buyeri, LpSolve.EQ, 0);
			}
			else {
				solver.strAddConstraint(buyeri, LpSolve.EQ, buyers[i][0]);				
			}
			
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
//			System.out.println("Buyer[" + i + "]: " + selleri);
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
	
	public static int getNumberOfVariables(int[][] sellers, int[][] buyers) throws LpSolveException{
//		System.out.println("Number of variables: " + (sellers.length * buyers.length));
		return (sellers.length * buyers.length);
	}
	
	public static int[][] cloneArray(int[][] source) {
	    int length = source.length;
	    int[][] target = new int[length][source[0].length];
	    for (int i=0; i<length; i++) {
	        System.arraycopy(source[i], 0, target[i], 0, source[i].length);
	    }
	    return target;
	}
	
	public static double[][] cloneArray(double[][] source) {
	    int length = source.length;
	    double[][] target = new double[length][source[0].length];
	    for (int i=0; i<length; i++) {
	        System.arraycopy(source[i], 0, target[i], 0, source[i].length);
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
}
