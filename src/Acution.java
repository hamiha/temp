import java.util.Arrays;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Acution {
	
	public static double[][] runSimulate(int[][] sellers, int[][] buyers, boolean isGen) throws LpSolveException {
//    	create linear object with its objective function
		LpSolve solver = LpSolve.makeLp(0, ProcessData.getNumberOfVariables(sellers, buyers));
		solver.strSetObjFn(ProcessData.generateObjFuction(sellers, buyers));
		
		double startTime = System.nanoTime();
		
		if(isGen) {
//		run genetic algorithm to get optimal allocation
			GeneticAlgorithm gen = new GeneticAlgorithm(sellers, buyers);
			gen.runGeneticAlgorithm();
//		generate constraints from gene/solution and solve		
			ProcessData.generateConsts(solver, sellers, buyers, gen.showOptimalList());					
		}
		else {
//		run brute force algorithm to get optimal allocation
			bruteForce brute = new bruteForce(sellers, buyers);
//		generate constraints from brute force algorithm and solve
			ProcessData.generateConsts(solver, sellers, buyers, bruteForce.getAlloc());			
		}

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
	    
	    double stopTime = System.nanoTime();
	    double timeElapsed = (stopTime - startTime) / 100000000;
	    timeElapsed = roundAvoid(timeElapsed, 3);
	    
	    double[][] result = new double[1][2];
	    result[0][0] = solver.getObjective();
	    result[0][1] = timeElapsed;
	    
	    return result;
	}
	
	public static double roundAvoid(double value, int places) {
	    double scale = Math.pow(10, places);
	    return Math.round(value * scale) / scale;
	}

	
	public static void main (String[] args) throws LpSolveException {
//		input format: {units,  unit price}
//		auto generate data (number of sellers, number of buyers)

		double numberOfSimulation = 20.0;
		double trueTimes = 0.0;
		double meanGeneTimer = 0.0;
		double meanBruteTimmer = 0.0;
		
		int maxNumberOfSellers = 8;
		int diffNumberOfBuyerAndNumberOfSeller = 2;
		
		int plotIndex = 0;
		double[] GenTimer = new double[maxNumberOfSellers+2];
		double[] BruteTimer = new double[maxNumberOfSellers+2];
		double[] Accurary = new double[maxNumberOfSellers+2];
		
		for(int index=2;index<=maxNumberOfSellers; index++) {
			GenerateData data = new GenerateData(index,index+2);
			int[][] sellers = ProcessData.cloneArray(data.sellers);
			int[][] buyers = ProcessData.cloneArray(data.buyers);
			int i=0;
			trueTimes = 0.0;
			while(i<(int)numberOfSimulation) {
				System.out.println("Genetic:");
				double[][] genetic = runSimulate(sellers, buyers, true);
				meanGeneTimer += genetic[0][1]; 
				System.out.println("Length: " + buyers.length);
				System.out.println("Brute force:");
				double[][] brute = runSimulate(sellers, buyers, false);
				meanBruteTimmer += brute[0][1];
				if(genetic[0][0] - brute[0][0] < 0.0001)
					trueTimes+= 1.0;
				i++;
			}
			GenTimer[plotIndex] = meanGeneTimer/numberOfSimulation;
			BruteTimer[plotIndex] = meanBruteTimmer/numberOfSimulation;
			Accurary[plotIndex] = trueTimes/numberOfSimulation;
			
			plotIndex++;
			
			System.out.println("Mean time gen: " + meanGeneTimer/numberOfSimulation);
			System.out.println("Mean time brute: " + meanBruteTimmer/numberOfSimulation);
			System.out.println("Accuracy: " + trueTimes/numberOfSimulation);
		}
		
		System.out.println("Gene: " + Arrays.toString(GenTimer));
		System.out.println("Gene: " + Arrays.toString(BruteTimer));
		System.out.println("Gene: " + Arrays.toString(Accurary));
		
	}
}

