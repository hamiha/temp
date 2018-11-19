
import java.text.DecimalFormat;
import java.util.Arrays;

import org.apache.commons.math3.distribution.NormalDistribution;

public class GenerateData {

	protected static int numberOfSellers = 10;
	protected static int numberOfBuyers = 15;
	protected static int[][] sellers;
	protected static int[][] buyers;
	
	protected static double meanBuyersPrice = 8, defaultBuyersPriceDeviation = 1;
	protected static double meanBuyersUnits = 8, defaultBuyersUnitsDeviation = 1;
	protected static double meanSellersPrice = 6, defaultSellersPriceDeviation = 1;
	protected static double meanSellersUnits = 6, defaultSellersUnitsDeviation = 1;
	
	protected static int minUnit = 1;

	public GenerateData(){
		generateData(0, 0, 0, 0);
	}
	
	public GenerateData(int numberOfSellers, int numberOfBuyers) {
		this.numberOfBuyers = numberOfBuyers;
		this.numberOfSellers = numberOfSellers;
		generateData(0, 0, 0, 0);
	}
	
	public static void generateData(double SellersPriceDeviation, double SellersUnitsDeviation, double BuyersPriceDeviation, double BuyersUnitDeviation) {
		sellers = new int[numberOfSellers][2];
		buyers = new int[numberOfBuyers][2];			
		
//		set buyers unit
		if(BuyersUnitDeviation<=0) {
			NormalDistribution disPrice = new NormalDistribution(meanBuyersUnits, defaultBuyersUnitsDeviation);
			for (int i = 0; i < numberOfBuyers; i++) {
				buyers[i][0] = (int) disPrice.sample();
				if(buyers[i][0] <= 0) buyers[i][0] = minUnit;
			}
		}
		else {
			NormalDistribution disPrice = new NormalDistribution(meanBuyersUnits, BuyersUnitDeviation);
			for (int i = 0; i < numberOfBuyers; i++) {
				buyers[i][0] = (int) disPrice.sample();
				if(buyers[i][0] <= 0) buyers[i][0] = minUnit;
			}
		}
		
//		set buyers price
		if(BuyersPriceDeviation<=0) {
			NormalDistribution disPrice = new NormalDistribution(meanBuyersPrice, defaultBuyersPriceDeviation);
			for (int i = 0; i < numberOfBuyers; i++) {
				buyers[i][1] = (int) disPrice.sample();
				if(buyers[i][1] <= 0) buyers[i][0] = minUnit;
			}
		}
		else {
			NormalDistribution disPrice = new NormalDistribution(meanBuyersPrice, BuyersPriceDeviation);
			for (int i = 0; i < numberOfBuyers; i++) {
				buyers[i][1] = (int) disPrice.sample();
				if(buyers[i][1] <= 0) buyers[i][0] = 0;
			}
		}
		
		
//		set sellers unit
		if(SellersUnitsDeviation<=0) {
			NormalDistribution disPrice = new NormalDistribution(meanSellersUnits, defaultSellersUnitsDeviation);
			for (int i = 0; i < numberOfSellers; i++) {
				sellers[i][0] = (int) disPrice.sample();
				if(sellers[i][0] <= 0) sellers[i][0] = minUnit;
			}
		}
		else {
			NormalDistribution disPrice = new NormalDistribution(meanSellersUnits, SellersUnitsDeviation);
			for (int i = 0; i < numberOfSellers; i++) {
				sellers[i][0] = (int) disPrice.sample();
				if(sellers[i][0] <= 0) sellers[i][0] = minUnit;
			}
		}
		
//		set sellers price
		if(SellersPriceDeviation<=0) {
			NormalDistribution disPrice = new NormalDistribution(meanSellersPrice, defaultSellersPriceDeviation);
			for (int i = 0; i < numberOfSellers; i++) {
				sellers[i][1] = (int) disPrice.sample();
				if(sellers[i][1] <= 0) sellers[i][1] = 0;
			}
		}
		else {
			NormalDistribution disPrice = new NormalDistribution(meanSellersPrice, SellersPriceDeviation);
			for (int i = 0; i < numberOfSellers; i++) {
				sellers[i][1] = (int) disPrice.sample();
				if(sellers[i][1] <= 0) sellers[i][1] = 0;
			}
		}
		
		System.out.println("buyers: " + getMeanBiddingPrice(buyers));
		print2DArrays(buyers);
//
		System.out.println("\nsellers: " + getMeanBiddingPrice(sellers));
		print2DArrays(sellers);
		
		System.out.println();
	}
	
	public static void print2DArrays(int array[][]){ 
        for (int[] row : array) 
        	System.out.print(Arrays.toString(row)); 
    } 
	
	private static double getMeanBiddingPrice(int array[][]) {
		double mean = 0;
		for(int[] row : array) {
			mean += (double) row[1];
		}
		return (mean / array.length);
	}

}
