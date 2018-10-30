import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Acution {
	public static void runSimulate(int[][] sellers, int[][] buyers, boolean isGen) throws LpSolveException {
//    	create linear object with its objective function
		LpSolve solver = LpSolve.makeLp(0, ProcessData.getNumberOfVariables(sellers, buyers));
		solver.strSetObjFn(ProcessData.generateObjFuction(sellers, buyers));
		
		long startTime = System.nanoTime();
		
////		run genetic algorithm to get optimal allocation
//		GeneticAlgorithm gen = new GeneticAlgorithm(sellers, buyers);
//		gen.runGeneticAlgorithm();
////		generate constraints from gene/solution and solve		
//		ProcessData.generateConsts(solver, sellers, buyers, gen.showOptimalList());		
		
//		run brute force algorithm to get optimal allocation
		bruteForce brute = new bruteForce(sellers, buyers);
//		generate constraints from brute force algorithm and solve
		ProcessData.generateConsts(solver, sellers, buyers, bruteForce.getAlloc());

		solver.setMaxim();
		solver.setVerbose(LpSolve.IMPORTANT);
		solver.solve();

		System.out.println("Value of welfare: " + solver.getObjective());
//	    int[] allocation = ProcessData.arrayToInt(solver.getPtrVariables());
//	    int buyeri = 1;
//	    for (int i = 0; i < allocation.length; i++) {
//	    	if(i % sellers.length == 0) {
//	    		System.out.println("--------- Buyer " + buyeri + " ---------");
//	    		buyeri++;
//	    	}
//	    	if(allocation[i] > 0)
//	    		System.out.println("Seller [" + (i%sellers.length+1) + "] = " + allocation[i]);
//	    }
	    
	    int optimalWelfare = (int) solver.getObjective();
	    CalculateTradingPrice calculation = new CalculateTradingPrice(sellers, buyers, ProcessData.arrayToInt(solver.getPtrVariables()), optimalWelfare, isGen);
	    
	    long stopTime = System.nanoTime();
	    long timeElapsed = stopTime - startTime;
	    System.out.println("Execution time in milliseconds : " + timeElapsed / 1000000);

	}
	
	public static void main (String[] args) throws LpSolveException {
//		input format: {units,  unit price}
//		auto generate data (number of sellers, number of buyers)
		GenerateData data = new GenerateData(10,12);
		
		int[][] sellers = ProcessData.cloneArray(data.sellers);
		int[][] buyers = ProcessData.cloneArray(data.buyers);
	    System.out.println("Genetic:");
		runSimulate(sellers, buyers, true);
		System.out.println("Brute force:");
		runSimulate(sellers, buyers, false);
		
	}
}

