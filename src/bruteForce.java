import java.util.ArrayList;
import java.util.Arrays;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class bruteForce {
	
	static ArrayList<String> alloc = new ArrayList<String>();
	private static int[][] sellers;
	private static int[][] buyers;		
	private static char[] set = {'0', '1'};
	private static String finalAlloc = "";
	
	public bruteForce(int[][] sellers, int[][] buyers) throws LpSolveException {
		this.sellers = sellers;
		this.buyers = buyers;
		CalculateWelfare();
	}
	
	static void CalculateWelfare() throws LpSolveException { 	
		double max = 0;
	    int n = set.length;
	    genPossibleOutCome(set, "", n, buyers.length); 
	    for(int i=0; i<alloc.size(); i++) {
	    	LpSolve solver = LpSolve.makeLp(0, ProcessData.getNumberOfVariables(sellers, buyers));
			solver.strSetObjFn(ProcessData.generateObjFuction(sellers, buyers));
//			generate constraints from gene/solution and solve
			ProcessData.generateConsts(solver, sellers, buyers, alloc.get(i));
			solver.setMaxim();
			solver.setVerbose(LpSolve.IMPORTANT);
			solver.solve();
			if(solver.getObjective() > max) {
				max = solver.getObjective();
				finalAlloc = "" + alloc.get(i);
			}				
//			System.out.println("Opt: " + max + " ; Alloc: " + finalAlloc);
	    }
	} 
	
	public static String getAlloc() {
		return finalAlloc;
	}

	static void genPossibleOutCome(char[] set,  String prefix, int n, int k) {  
	    if (k == 0){ 
	        alloc.add(prefix);
	        return; 
	    } 
	    for (int i = 0; i < n; ++i){ 
	        String newPrefix = prefix + set[i];   
	        genPossibleOutCome(set, newPrefix, n, k - 1);  
	    } 
	} 
	
	public static void print2DArrays(int array[][]){ 
        for (int[] row : array) 
        	System.out.print(Arrays.toString(row)); 
    } 
	
}
