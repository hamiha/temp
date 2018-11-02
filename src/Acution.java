import java.util.Arrays;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Acution {
	
	private static double optWelfare = 0;
	private static int[] alloc;
	
	public static double[][] runSimulate(int[][] sellers, int[][] buyers, boolean isGen, boolean isBuyerBase) throws LpSolveException {
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
			BruteForce brute = new BruteForce(sellers, buyers);
//		generate constraints from brute force algorithm and solve
			ProcessData.generateConsts(solver, sellers, buyers, brute.getAlloc());			
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
	    
	    double optimalWelfare = solver.getObjective();
	    CalculateTradingPrice calculation = new CalculateTradingPrice(sellers, buyers, ProcessData.arrayToInt(solver.getPtrVariables()), optimalWelfare, isGen);
	    
	    optWelfare = optimalWelfare;
	    alloc = ProcessData.arrayToInt(solver.getPtrVariables());
	    
	    if(isBuyerBase)
	    	calculation.BuyerBase();
	    else
	    	calculation.SellerBase();
	    
	    double stopTime = System.nanoTime();
	    double timeElapsed = (stopTime - startTime) / 1000000000;
	    timeElapsed = roundDouble(timeElapsed, 3);
	    
	    double[][] result = new double[1][2];
	    result[0][0] = roundDouble(solver.getObjective(), 3);
	    result[0][1] = timeElapsed;
	    
	    return result;
	}
	
	public static double roundDouble(double value, int places) {
	    double scale = Math.pow(10, places);
	    return Math.round(value * scale) / scale;
	}

	public static void runToGetTimeAndWelfare(int numberOfSimulation, int maxNumberOfBuyers, int diffNumberOfBuyerAndSellers, int buyerStartFrom) throws LpSolveException {
		
		if(numberOfSimulation <= 0) {
			System.out.println("Number of simulation must be greater than 0");
			return;
		}
		if(buyerStartFrom > maxNumberOfBuyers) {
			System.out.println("Max number of buyers must be greater or equal to start of buyer's index");
			return;
		}
		if(buyerStartFrom <= diffNumberOfBuyerAndSellers) {
			System.out.println("Start buyer's index must be greater than different number of buyers and sellers");
			return;
		}
		
		
		double numSim = (double) numberOfSimulation;
		double meanGeneTimer = 0.0;
		double meanBruteTimmer = 0.0;
		double meanGeneWelfare = 0.0;
		double meanBruteWelfare = 0.0;
		
		int plotIndex = 0;
		int lengthOfPlot = maxNumberOfBuyers-buyerStartFrom+1;
		double[] GenTimer = new double[lengthOfPlot];
		double[] BruteTimer = new double[lengthOfPlot];
		double[] GeneWelfare = new double[lengthOfPlot];
		double[] BruteWelfare = new double[lengthOfPlot];
		
		for(int i=buyerStartFrom;i<=maxNumberOfBuyers; i++) {
			GenerateData data = new GenerateData(i-diffNumberOfBuyerAndSellers,i);
			int[][] sellers = ProcessData.cloneArray(data.sellers);
			int[][] buyers = ProcessData.cloneArray(data.buyers);
			int simIndex = 0;
			while(simIndex < numberOfSimulation) {
				System.out.println("Length: " + buyers.length);
				
				System.out.println("Genetic:");
				double[][] genetic = runSimulate(sellers, buyers, true, true);
				meanGeneWelfare += genetic[0][0];
				meanGeneTimer += genetic[0][1]; 
				
				System.out.println("Brute force:");
				double[][] brute = runSimulate(sellers, buyers, false, true);
				meanBruteWelfare += brute[0][0];
				meanBruteTimmer += brute[0][1];
				
				simIndex++;
			}
			
			GenTimer[plotIndex] = roundDouble(meanGeneTimer/numSim, 3);
			BruteTimer[plotIndex] = roundDouble(meanBruteTimmer/numSim, 3);
			GeneWelfare[plotIndex] = roundDouble(meanGeneWelfare/numSim, 3);
			BruteWelfare[plotIndex] = roundDouble(meanBruteWelfare/numSim, 3);
			
			System.out.println("Mean welfare genetic: " + GeneWelfare[plotIndex]);
			System.out.println("Mean welfare brute: " + BruteWelfare[plotIndex]);
			System.out.println("Mean time gene: " + GenTimer[plotIndex]);
			System.out.println("Mean time brute: " + BruteTimer[plotIndex]);
		
			plotIndex++;
			
		}
		System.out.println("-----SUMMARY------");
		System.out.println("Mean welfare genetic: " + Arrays.toString(GeneWelfare));
		System.out.println("Mean welfare brute: " + Arrays.toString(BruteWelfare));
		System.out.println("Mean time gene: " + Arrays.toString(GenTimer));
		System.out.println("Mean time brute: " + Arrays.toString(BruteTimer));
	}
	
	
	public static void main (String[] args) throws LpSolveException {
//		input format: {units,  unit price}
//		auto generate data (number of sellers, number of buyers)
		
		GenerateData data = new GenerateData(6, 8);
		System.out.println("-----BUYER BASE------");
		runSimulate(data.sellers, data.buyers, true, true);
		
		System.out.println("-----SELLER BASE------");
		CalculateTradingPrice sellerCal = new CalculateTradingPrice(data.sellers, data.buyers, alloc, optWelfare, true);
		sellerCal.SellerBase();
		
//		run simulation and get mean welfare and mean running time of genetic and brute force
//		runToGetTimeAndWelfare(numberOfSimulation, maxNumberOfBuyers, diffNumberOfBuyerAndSellers, buyerStartFrom);
//		runToGetTimeAndWelfare(3, 8, 2, 6);
				
	}
}

