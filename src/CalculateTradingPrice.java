import java.util.Arrays;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class CalculateTradingPrice {
	private static boolean isGene;
	private static int[][] buyers;
	private static int[][] sellers;
	private static int[] alloction;
	private static int[][] newUnitAlloc;
	private static int[][] newWelfareAlloc;
	private	static double[] buyersTradingPrice;
	private	static double[] sellersTradingPrice;
	private static double[][] distribution;
	private static int optimalWelfare;
	private static int welfareOfOthers;
	private static int welfareWithoutBuyeri;
	private static int welfareWitoutSelleri;
	
	public CalculateTradingPrice(int[][] sellers, int[][] buyers, int[] allocation, int optimalWelfare, boolean isGene) throws LpSolveException {
		this.sellers = ProcessData.cloneArray(sellers);
		this.buyers = ProcessData.cloneArray(buyers);
		this.alloction = Arrays.copyOf(allocation, allocation.length);
		this.optimalWelfare = optimalWelfare;
		this.isGene = isGene;
		setNewAlloc(sellers, buyers);
		calculateTradingPrice();
		getTradingPrice();
	}
	
//	set allocation of units and welfare in 2D array
	public static void setNewAlloc(int[][] sellers, int[][] buyers) {
		newUnitAlloc = new int[buyers.length][sellers.length];
		newWelfareAlloc = new int[buyers.length][sellers.length];
		int buyeri = 0;
		for(int i=0; i<alloction.length; i++) {
			if(i % sellers.length == 0)
				buyeri++;
			newUnitAlloc[buyeri-1][i%sellers.length] = alloction[i];
			newWelfareAlloc[buyeri-1][i%sellers.length] = alloction[i] * (buyers[buyeri-1][1] - sellers[i%sellers.length][1]);
		}
	}
	
//	calculate welfare of the others when i is present
	public void calculateWelfareOfOthers(int buyerIndex) {
		int welfareOfBuyeri = 0;
		for(int i = 0; i<sellers.length; i++) {
			welfareOfBuyeri += newWelfareAlloc[buyerIndex][i];
		}
		welfareOfOthers = optimalWelfare - welfareOfBuyeri;
//		System.out.println("\nOther welfare: " + welfareOfOthers);
	}
	
//	calculate max welfare if i were absent
	public void calculateWelfareWithoutBuyeri(int buyeri) throws LpSolveException {
		int[][] newBuyers = ProcessData.cloneArray(buyers);
		newBuyers[buyeri][0] = 0;
		newBuyers[buyeri][1] = 0;
		int numberOfVars = ProcessData.getNumberOfVariables(sellers, newBuyers);
		String ObjectFunction = ProcessData.generateObjFuction(sellers, newBuyers);
		
		while(welfareWithoutBuyeri < welfareOfOthers) {
	//    	create linear object with its objective function
			LpSolve solver = LpSolve.makeLp(0, numberOfVars);
			solver.strSetObjFn(ObjectFunction);
			
			if(isGene) {
	//		run genetic algorithm to get optimal allocation
				GeneticAlgorithm gen = new GeneticAlgorithm(sellers, newBuyers);
				gen.runGeneticAlgorithm();
	//		generate constraints from gene/solution and solve		
				ProcessData.generateConsts(solver, sellers, newBuyers, gen.showOptimalList());			
			}
			else {
	//		run brute force algorithm to get optimal allocation
				bruteForce brute = new bruteForce(sellers, newBuyers);
	//		generate constraints from brute force algorithm and solve
				ProcessData.generateConsts(solver, sellers, newBuyers, bruteForce.getAlloc());			
			}
	//		
			
	//		generate constraints from gene/solution and solve
			solver.setMaxim();
			solver.setVerbose(LpSolve.IMPORTANT);
			solver.solve();
			
			welfareWithoutBuyeri = (int) solver.getObjective();
	//		System.out.println("Welfare without i: " + welfareWithoutBuyeri);
		}
	}
	
	public int calculateWelfareWithoutSelleri(int selleri) throws LpSolveException {
		int[][] newSellers = ProcessData.cloneArray(sellers);
		newSellers[selleri][0] = 0;
		newSellers[selleri][1] = 0;
		
//		run genetic algorithm to get optimal allocation
		GeneticAlgorithm gen = new GeneticAlgorithm(newSellers, buyers);
		gen.runGeneticAlgorithm();
		
//    	create linear object with its objective function
		LpSolve solver = LpSolve.makeLp(0, ProcessData.getNumberOfVariables(newSellers, buyers));
		solver.strSetObjFn(ProcessData.generateObjFuction(newSellers, buyers));
		
//		generate constraints from gene/solution and solve
		ProcessData.generateConsts(solver, newSellers, buyers, gen.showOptimalList());
		solver.setMaxim();
		solver.setVerbose(LpSolve.IMPORTANT);
		solver.solve();
		
		welfareWitoutSelleri = (int) solver.getObjective();
		System.out.println("Welfare without seller i: " + welfareWitoutSelleri);
		
		return welfareWitoutSelleri;
	}
	
	public void calculateTradingPrice() throws LpSolveException {
//		create new 2D array to store distribution
//		last element of each row is the distribution of buyer relate to the row
		distribution = new double[buyers.length][sellers.length+1];
		for(int i=0; i<buyers.length; i++) {
			double totalContribution = (double) Arrays.stream(newWelfareAlloc[i]).sum();
			if(totalContribution > 0) {
//				calculate seller's distribution: welfare w/o i - others
				calculateWelfareOfOthers(i);
				calculateWelfareWithoutBuyeri(i);
				double discount = welfareWithoutBuyeri - welfareOfOthers;
//				buyer's trading price: total contribution - seller's distribution
				distribution[i][sellers.length] = totalContribution - discount;
//				calculate seller's trading price
				for(int j=0; j<sellers.length; j++) {
					if(newWelfareAlloc[i][j] > 0) {
						distribution[i][j] = (double) newWelfareAlloc[i][j] / totalContribution * discount;
					}
				}
			}
		}
	}
//	get trading price of buyers and sellers
	public void getTradingPrice() {
		buyersTradingPrice = new double[buyers.length];
		sellersTradingPrice = new double[sellers.length];
//		buyer's trading price is the last element of the row related to buyer id
		for(int i=0; i<buyers.length; i++) {
			buyersTradingPrice[i] = distribution[i][sellers.length];
		}
//		seller's trading price is the total of elements in column related to seller id
		for(int i=0; i<sellers.length; i++) {
			sellersTradingPrice[i] = getSellerDiscount(i);
		}
		System.out.println("Buyers: " + Arrays.toString(buyersTradingPrice));
		System.out.println("Sellers: " + Arrays.toString(sellersTradingPrice));
	}
	
//	get total discount for seller from distribution
	public double getSellerDiscount(int sellerIndex) {
		double discount = 0;
//		get total of column related to sellers id
		for(int i=0; i<buyers.length; i++) {
			discount += distribution[i][sellerIndex];
		}
		return discount;
	}
	
//	print 2D int array
	public static void printTwoDimsArray(int[][] a) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				System.out.printf("%d ", a[i][j]);
			}
			System.out.println();
		}
	}
//	print 2D double array
	public static void printTwoDimsArray(double[][] a) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				System.out.printf("%.2f ", a[i][j]);
			}
			System.out.println();
		}
	}
	
	
	
}
