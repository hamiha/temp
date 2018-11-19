import java.util.Arrays;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class CalculateTradingPrice {
	private static boolean isGene;
	private static int[][] buyers;
	private static int[][] sellers;
	private static int[] alloction;
	private static int[][] newUnitAlloc;
	private static double[][] newWelfareAlloc;
	private	static double[] buyersTradingPrice;
	private	static double[] sellersTradingPrice;
	private static double[][] distribution;
	private static double[][] tradingPriceTable;
	private static double totalOfTradingPrice;
	private static double optimalWelfare;
	private static double welfareOfOthers;
	private static double welfareWithoutBuyeri;
	private static double welfareWithoutSelleri;
	
	private static double[][] buyerBase;
	private static double[][] sellerBase;
	
	public CalculateTradingPrice(int[][] sellers, int[][] buyers, int[] allocation, double optimalWelfare, boolean isGene) throws LpSolveException {
		this.sellers = ProcessData.cloneArray(sellers);
		this.buyers = ProcessData.cloneArray(buyers);
		this.alloction = Arrays.copyOf(allocation, allocation.length);
		this.optimalWelfare = optimalWelfare;
		this.isGene = isGene;
		this.tradingPriceTable = new double[buyers.length][sellers.length];
		this.totalOfTradingPrice = 0;
		setNewAlloc(sellers, buyers);
		
	}
	
	public static void BuyerBase() throws LpSolveException {
		System.out.println("---------Allocation------------");
		printTwoDimsArray(newUnitAlloc);
		System.out.println("-------------------------------");
		calculateTradingPrice();
//		printTwoDimsArray(newWelfareAlloc);
		getTradingPrice();
		
	}
	
	public static void SellerBase() throws LpSolveException {
//		printTwoDimsArray(newUnitAlloc);
		sellerTradingPrice();
//		printTwoDimsArray(newWelfareAlloc);
		showTradingPrice();
	}
	
//	set allocation of units and welfare in 2D array in buyer base
	public static void setNewAlloc(int[][] sellers, int[][] buyers) {
		newUnitAlloc = new int[buyers.length][sellers.length];
		newWelfareAlloc = new double[buyers.length][sellers.length];
		int buyeri = 0;
		for(int i=0; i<alloction.length; i++) {
			if(i % sellers.length == 0)
				buyeri++;
			newUnitAlloc[buyeri-1][i%sellers.length] = alloction[i];
			int diffPrice = (buyers[buyeri-1][1] - sellers[i%sellers.length][1]);
			newWelfareAlloc[buyeri-1][i%sellers.length] = alloction[i] * diffPrice;
//			System.out.println("Location:[" +(buyeri-1)+ "][" +(i%sellers.length)+ "] - diff price: " + diffPrice + " * unit: " +  alloction[i] + " = "+newWelfareAlloc[buyeri-1][i%sellers.length]);
		}
	}
	
//	---------------------SELLER BASE----------------------------------------------------
//	set allocation of units and welfare in 2D array in seller base
	
	public static void calculateWelfareWithoutSelleri(int selleri) throws LpSolveException {
		int[][] newSellers = ProcessData.cloneArray(sellers);
		newSellers[selleri][0] = 0;
		newSellers[selleri][1] = 0;
		welfareWithoutSelleri = 0;
		int numbOfRunning = 0;
		while(welfareWithoutSelleri < welfareOfOthers || welfareWithoutSelleri > optimalWelfare) {
//			System.out.println("Welfare w/o " + welfareWithoutSelleri +" is smaller than " + welfareOfOthers);
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
			
			welfareWithoutSelleri = roundDouble(solver.getObjective(), 3);
//			System.out.println("Welfare without seller i: " + welfareWithoutSelleri);
			
//			after calculating 3 times, break the loop
			numbOfRunning++;
			if(numbOfRunning>3) {
				if(welfareWithoutSelleri > optimalWelfare)
//					System.out.println("seller i > optimal");
					welfareWithoutSelleri = optimalWelfare;
				if(welfareWithoutSelleri < welfareOfOthers) {
					welfareWithoutSelleri = welfareOfOthers;
//					System.out.println("seller i < others");
				}
			}
		}
	}
	
//	get welfare of the others
	public static void getWelfareOfOthers(int sellerIndex) {
		int welfareOfSelleri = 0;
		for(int i=0; i<buyers.length; i++) {
			welfareOfSelleri += newWelfareAlloc[i][sellerIndex];
		}
		welfareOfOthers = roundDouble((optimalWelfare - welfareOfSelleri), 3) ;
//		System.out.println("welfare of seller i: " + welfareOfSelleri);
	}
	
//	calculate trading price
	public static void sellerTradingPrice() throws LpSolveException {
//		create new 2D array to store distribution
//		last element of each column is the distribution of seller relate to the row
		distribution = new double[buyers.length+1][sellers.length];
		for(int i=0; i<sellers.length; i++) {
			double totalContribution =  getTotalOfColumn(i, newWelfareAlloc);
			if(totalContribution > 0) {
//				calculate seller's distribution: welfare w/o i - others
//				System.out.println("seller i: " + i);
				getWelfareOfOthers(i);
				calculateWelfareWithoutSelleri(i);
				double discount = welfareWithoutSelleri - welfareOfOthers;
//				buyer's trading price: total contribution - seller's distribution
				distribution[buyers.length][i] = totalContribution - discount;
//				calculate seller's trading price
				for(int j=0; j<buyers.length; j++) {
					if(newWelfareAlloc[j][i] > 0) {
						distribution[j][i] = (double) newWelfareAlloc[j][i] / totalContribution * discount;
					}
				}
			}
		}
	}

//	get trading price of buyers and sellers
	public static void showTradingPrice() {
		
		for(int row=0; row<buyers.length; row++) {
			for(int col=0; col<sellers.length; col++) {
				double tradingPrice = buyers[row][1] * newUnitAlloc[row][col] - distribution[row][col];
				tradingPriceTable[row][col] = tradingPrice;
				totalOfTradingPrice += tradingPrice;
			}
		}
		printTwoDimsArray(tradingPriceTable);
		sellerBase = ProcessData.cloneArray(tradingPriceTable);
		System.out.println("Total of trading price: " + totalOfTradingPrice);
		
		buyersTradingPrice = new double[buyers.length];
		sellersTradingPrice = new double[sellers.length];
//		seller's trading price is the last element of the column related to seller's id
		for(int i=0; i<sellers.length; i++) {
			sellersTradingPrice[i] = roundDouble(distribution[buyers.length][i], 3);
		}
//		buyer's trading price is the total of elements in column related to buyer's id
		for(int i=0; i<buyers.length; i++) {
			buyersTradingPrice[i] = Arrays.stream(distribution[i]).sum();
			buyersTradingPrice[i] =  roundDouble(buyersTradingPrice[i], 3);
		}
//		Print sellers and buyers discount
		System.out.println("Buyers: " + Arrays.toString(buyersTradingPrice));
		System.out.println("Sellers: " + Arrays.toString(sellersTradingPrice));
	}
	
//	---------------------BUYER BASE--------------------------------------------------------------
//	calculate welfare of the others when i is present
	public static void calculateWelfareOfOthers(int buyerIndex) {
		int welfareOfBuyeri = 0;
		for(int i = 0; i<sellers.length; i++) {
			welfareOfBuyeri += newWelfareAlloc[buyerIndex][i];
//			System.out.println("welfare at [" + buyerIndex +"][" + i +"] is " + newWelfareAlloc[buyerIndex][i]);
		}
//		System.out.println("welfare of buyer " + buyerIndex +" is " + welfareOfBuyeri);
		welfareOfOthers = roundDouble((optimalWelfare - welfareOfBuyeri), 3);
//		System.out.println("\nOther welfare: " + welfareOfOthers);
	}
	
//	calculate max welfare if buyer i were absent
	public static void calculateWelfareWithoutBuyeri(int buyeri) throws LpSolveException {
		int[][] newBuyers = ProcessData.cloneArray(buyers);
		newBuyers[buyeri][0] = 0;
		newBuyers[buyeri][1] = 0;
		welfareWithoutBuyeri = 0;
		int numbOfRunning = 0;
		
		int numberOfVars = ProcessData.getNumberOfVariables(sellers, newBuyers);
		String ObjectFunction = ProcessData.generateObjFuction(sellers, newBuyers);
		
		while(welfareWithoutBuyeri < welfareOfOthers || welfareWithoutBuyeri > optimalWelfare) {
//			System.out.println("Welfare w/o " + welfareWithoutBuyeri +" is smaller than " + welfareOfOthers);
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
				BruteForce brute = new BruteForce(sellers, newBuyers);
	//		generate constraints from brute force algorithm and solve
				ProcessData.generateConsts(solver, sellers, newBuyers, BruteForce.getAlloc());			
			}

	//		generate constraints from gene/solution and solve
			solver.setMaxim();
			solver.setVerbose(LpSolve.IMPORTANT);
			solver.solve();
			
			welfareWithoutBuyeri = roundDouble(solver.getObjective(), 3);
//			System.out.println("Welfare without i: " + welfareWithoutBuyeri);
			
//			after calculating 3 times, break the loop			
			numbOfRunning++;
			if(numbOfRunning>3) {
				if(welfareWithoutBuyeri > optimalWelfare)
//					System.out.println("buyer i > optimal");
					welfareWithoutBuyeri = optimalWelfare;
				if(welfareWithoutSelleri < welfareOfOthers) {
//					System.out.println("buyer i < others");
					welfareWithoutBuyeri = welfareOfOthers;
				}
			}
		}
		
	}

//	calculate trading price buyer base
	public static void calculateTradingPrice() throws LpSolveException {
//		create new 2D array to store distribution
//		last element of each row is the distribution of buyer relate to the row
		distribution = new double[buyers.length][sellers.length+1];
		for(int i=0; i<buyers.length; i++) {
			double totalContribution = (double) Arrays.stream(newWelfareAlloc[i]).sum();
			if(totalContribution > 0) {
//				System.out.println("Buyer i: " + i);
//				calculate seller's distribution: welfare w/o i - others
				calculateWelfareOfOthers(i);
				calculateWelfareWithoutBuyeri(i);
				double discount = welfareWithoutBuyeri - welfareOfOthers;
//				buyer's trading price: total contribution - seller's distribution
				distribution[i][sellers.length] = totalContribution - discount;
//				calculate seller's trading price
				for(int j=0; j<sellers.length; j++) {
					if(newWelfareAlloc[i][j] > 0) {
						distribution[i][j] = newWelfareAlloc[i][j] / totalContribution * discount;
					}
				}
			}
		}
	}
	
//	calculate trading price table base of distribution table
	public static void getTradingPriceTable() {
		for(int row=0; row<buyers.length; row++) {
			for(int col=0; col<sellers.length; col++) {
				double tradingPrice = distribution[row][col] + newUnitAlloc[row][col] * sellers[col][1];
				tradingPriceTable[row][col] = tradingPrice;
				totalOfTradingPrice += tradingPrice;
			}
		}
		printTwoDimsArray(tradingPriceTable);
		buyerBase = ProcessData.cloneArray(tradingPriceTable);
		System.out.println("Total of trading price: " + totalOfTradingPrice);
	}

//	get trading price of buyers and sellers
	public static void getTradingPrice() {
//		call getTradingPriceTable function
		getTradingPriceTable();
		
		buyersTradingPrice = new double[buyers.length];
		sellersTradingPrice = new double[sellers.length];
//		buyer's trading price is the last element of the row related to buyer id
		for(int i=0; i<buyers.length; i++) {
			buyersTradingPrice[i] = roundDouble(distribution[i][sellers.length], 3);
		}
//		seller's trading price is the total of elements in column related to seller id
		for(int i=0; i<sellers.length; i++) {
			sellersTradingPrice[i] = getSellerDiscount(i);
		}
//		Print sellers and buyers discount
		System.out.println("Buyers: " + Arrays.toString(buyersTradingPrice));
		System.out.println("Sellers: " + Arrays.toString(sellersTradingPrice));
	}
	
//	get total discount for seller from distribution
	public static double getSellerDiscount(int sellerIndex) {
		double discount = 0;
//		get total of column related to sellers id
		for(int i=0; i<buyers.length; i++) {
			discount += distribution[i][sellerIndex];
		}
		return roundDouble(discount, 3);
	}
//----------------support functions-------------------------------------
//	print 2D int array
	public static void printTwoDimsArray(int[][] a) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				System.out.printf("|%d\t ", a[i][j]);
			}
			System.out.println();
		}
	}
//	print 2D double array
	public static void printTwoDimsArray(double[][] a) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				System.out.printf("|%.2f \t", a[i][j]);
			}
			System.out.println();
		}
	}
	
	public static double[][] getBuyerBase(){
		return buyerBase;
	}
	
	public static double[][] getSellerBase(){
		return sellerBase;
	}
	
//	round number
	public static double roundDouble(double value, int places) {
	    double scale = Math.pow(10, places);
	    return Math.round(value * scale) / scale;
	}
//	get total of column in 2D array
	public static int getTotalOfColumn(int colId, double[][] source) {
		int total = 0;
		for(int i=0; i<source.length;i++) {
			total += source[i][colId];
		}
		return total;
	}
	
}
