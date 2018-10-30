

/** 	Credit
 * @filename:       KnapsackProblem.java
 * @author:         Matthew Mayo
 * @modified:       2014-04-08
 * @description:    Creates a KnapsackProblem object based on user input,
 *                  attempts to solve using a genetic algorithm; outputs 
 *                  algorithm data step-by-step, generates list of optimal
 *                  items for problem, graphs mean fitness by generation; 
 *		    optional command line argument output_filename will 
 *		    redirect all algorithm details output to output_filename
 *		    in current directory, will overwrite output_filename 
 *		    contents if file exists
 * @usage:          java KnapsackProblem <output_filename>
 */


import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Random;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class GeneticAlgorithm {

    private int crossover_count = 0;
    private int clone_count = 0;
    private static int population_size = 30;
    private static int maximum_generations = 50;
    private int generation_counter = 1;
    private static double prob_crossover = 0.95;	// 0~1
    private static double prob_mutation = 0.01;	// 0~1
    private double total_fitness_of_generation = 0;
    private ArrayList<Double> fitness = new ArrayList<Double>();
    private ArrayList<Double> best_fitness_of_generation = new ArrayList<Double>();
    private ArrayList<Double> mean_fitness_of_generation = new ArrayList<Double>();
    private ArrayList<String> population = new ArrayList<String>();
    private ArrayList<String> breed_population = new ArrayList<String>();
    private ArrayList<String> best_solution_of_generation = new ArrayList<String>();
    private static int[][] sellers;
	private static int[][] buyers;	
	private static String objectFunction;
	private static int numberOfVariables;
	 

    /**  
     * Default constructor
     * @throws LpSolveException 
     */
    public GeneticAlgorithm(int[][] sellers, int[][] buyers) throws LpSolveException {
    	GeneticAlgorithm.buyers = buyers;
    	GeneticAlgorithm.sellers = sellers;
    	objectFunction = ProcessData.generateObjFuction(sellers, buyers);
    	numberOfVariables = ProcessData.getNumberOfVariables(sellers, buyers);
    }
    

    /**
     * Controls knapsack problem logic and creates first generation
     * @throws LpSolveException 
     */
    public void runGeneticAlgorithm() throws LpSolveException {

        // Generate initial random population (first generation)
        this.makePopulation();

        // Start printing out summary
//        System.out.println("\nInitial Generation:");
//        System.out.println("===================");
//        System.out.println("Population:");
//        for(int i = 0; i < this.population_size; i++) {
//            System.out.println((i + 1) + " - " + this.population.get(i));
//        }

        // Evaluate fitness of initial population members
        this.evalPopulation();

        // Output fitness summary
//        System.out.println("\nFitness:");
//        for(int i = 0; i < this.population_size; i++) {
//            System.out.println((i + 1) + " - " + this.fitness.get(i));
//        }

        // Find best solution of generation
        this.best_solution_of_generation.add(this.population.get(this.getBestSolution()));

        // Output best solution of generation
//        System.out.println("\nBest solution of initial generation: " + this.best_solution_of_generation.get(0));

        // Find mean solution of generation
        this.mean_fitness_of_generation.add(this.getMeanFitness());

	// 	Output mean solution of generation
//		System.out.println("Mean fitness of initial generation: " + this.mean_fitness_of_generation.get(0));

        // Compute fitness of best solution of generation
        this.best_fitness_of_generation.add(this.evalGene(this.population.get(this.getBestSolution())));

        // Output best fitness of generation
//        System.out.println("Fitness score of best solution of initial generation: " + this.best_fitness_of_generation.get(0));

        // If maximum_generations > 1, breed further generations
        if(this.maximum_generations > 1) {
            makeFurtherGenerations();
        }

    }


    /**
     * Makes further generations beyond first, if necessary
     * @throws LpSolveException 
     */
    private void makeFurtherGenerations() throws LpSolveException {

        // Breeding loops maximum_generation number of times at most
        for(int i = 1; i < this.maximum_generations; i++) {

	    // Check for stopping criterion
	    if((this.maximum_generations > 4) && (i > 4)) {

		// Previous 3 generational fitness values
		double a = this.mean_fitness_of_generation.get(i - 1);
		double b = this.mean_fitness_of_generation.get(i - 2);
		double c = this.mean_fitness_of_generation.get(i - 3);

		// If all are 3 equal, stop
		if(a == b && b == c) {
//		    System.out.println("\nStop criterion met");
		    maximum_generations = i;
		    break;
		}
	    }

            // Reset some counters
            this.crossover_count = 0;
            this.clone_count = 0;

            // Breed population
            for(int j = 0; j < this.population_size / 2; j++) {
                this.breedPopulation();
            }
   
            // Clear fitness values of previous generation
            this.fitness.clear();

            // Evaluate fitness of breed population members
            this.evalBreedPopulation();

            // Copy breed_population to population
            for(int k = 0; k < this.population_size; k++) {
                this.population.set(k, this.breed_population.get(k));
            }

//             Output population
//            System.out.println("\nGeneration " + (i + 1) + ":");
//            if((i + 1) < 10) {
//                System.out.println("=============");
//            }
//            if((i + 1) >= 10) {
//                System.out.println("==============");
//            }
//            if((i + 1) >= 100) {
//                System.out.println("===============");
//            }
//            System.out.println("Population:");
//            for(int l = 0; l < this.population_size; l++) {
//                System.out.println((l + 1) + " - " + this.population.get(l));
//            }

//             Output fitness summary
//            System.out.println("\nFitness:");
//            for(int m = 0; m < this.population_size; m++) {
//               System.out.println((m + 1) + " - " + this.fitness.get(m));
//            } 

            // Clear breed_population
            this.breed_population.clear();

            // Find best solution of generation
            this.best_solution_of_generation.add(this.population.get(this.getBestSolution()));

            // Output best solution of generation
//            System.out.println("\nBest solution of generation " + (i + 1) + ": " + this.best_solution_of_generation.get(i));

            // Find mean solution of generation
            this.mean_fitness_of_generation.add(this.getMeanFitness());

	    	// Output mean solution of generation
//	    	System.out.println("Mean fitness of generation: " + this.mean_fitness_of_generation.get(i));

            // Compute fitness of best solution of generation
            this.best_fitness_of_generation.add(this.evalGene(this.population.get(this.getBestSolution())));

            // Output best fitness of generation
//            System.out.println("Fitness score of best solution of generation " + (i + 1) + ": " + this.best_fitness_of_generation.get(i));

            // Output crossover/cloning summary
//            System.out.println("Crossover occurred " + this.crossover_count + " times");
//            System.out.println("Cloning occurred " + this.clone_count + " times");

        }
    }



    /**
     * Output Result summary
     */
    public String showOptimalList() {
    	
        // Output optimal list of items
//        System.out.println("\nOptimal list of buyers after auction: ");

        double best_fitness = 0;
        int best_gen = 0;

        // First, find best solution out of generational bests
        for(int z = 0; z < this.maximum_generations - 1; z++) {
            if(this.best_fitness_of_generation.get(z) > best_fitness) {
                best_fitness = this.best_fitness_of_generation.get(z);
                best_gen = z;
            }
        }

        // Then, go through that's generation's best solution and output items
        String optimal_list = this.best_solution_of_generation.get(best_gen);
        optimal_list = optimal_list.substring(0, buyers.length);
//        System.out.println(optimal_list);
        
        return optimal_list;
    }


    /**
     * Breeds current population to create a new generation's population
     */
    private void breedPopulation() {

        // 2 genes for breeding
        int gene_1;
        int gene_2;

        // Increase generation_counter
        generation_counter = generation_counter + 1;

        // If population_size is odd #, use elitism to clone best solution of previous generation
        if(population_size % 2 == 1) {
            breed_population.add(best_solution_of_generation.get(generation_counter - 1));
        }

        // Get positions of pair of genes for breeding
        gene_1 = selectGene();
        gene_2 = selectGene();
        
        // Crossover or cloning
        crossoverGenes(gene_1, gene_2);

    }


    /**
     * Performs mutation, if necessary
     */
    private void mutateGene() {
       
        // Decide if mutation is to be used
        double rand_mutation = Math.random();
        if(rand_mutation <= prob_mutation) {

            // If so, perform mutation
            String mut_gene;
            String new_mut_gene;
            Random generator = new Random();
            int mut_point = 0;
            double which_gene = Math.random() * 100;

	    // Mutate gene
            if(which_gene <= 50) {
                mut_gene = breed_population.get(breed_population.size() - 1);
                mut_point = generator.nextInt(buyers.length);
                if(mut_gene.substring(mut_point, mut_point + 1).equals("1")) {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "0" + mut_gene.substring(mut_point);
                    breed_population.set(breed_population.size() - 1, new_mut_gene);
                }
                if(mut_gene.substring(mut_point, mut_point + 1).equals("0")) {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "1" + mut_gene.substring(mut_point);
                    breed_population.set(breed_population.size() - 1, new_mut_gene);
                }
            }
            if(which_gene > 50) {
                mut_gene = breed_population.get(breed_population.size() - 2);
                mut_point = generator.nextInt(buyers.length);
                if(mut_gene.substring(mut_point, mut_point + 1).equals("1")) {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "0" + mut_gene.substring(mut_point);
                    breed_population.set(breed_population.size() - 1, new_mut_gene);
                }
                if(mut_gene.substring(mut_point, mut_point + 1).equals("0")) {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "1" + mut_gene.substring(mut_point);
                    breed_population.set(breed_population.size() - 2, new_mut_gene);
                }
            }           
        }
    }


    /**
     * Selects a gene for breeding
     * @return int - position of gene in population ArrayList to use for breeding
     */
    private int selectGene() {

        // Generate random number between 0 and total_fitness_of_generation
        double rand = Math.random() * total_fitness_of_generation;
        
        // Use random number to select gene based on fitness level
        for(int i = 0; i < population_size; i++) {
            if(rand <= fitness.get(i)) {
                return i;
            }
            rand = rand - fitness.get(i);
        }

	// Not reachable; default return value
	return 0;
    }


    /**
     * Performs either crossover or cloning
     */
    private void crossoverGenes(int gene_1, int gene_2) {
      
        // Strings to hold new genes
        String new_gene_1;
        String new_gene_2;

        // Decide if crossover is to be used
        double rand_crossover = Math.random();
        if(rand_crossover <= prob_crossover) {
            // Perform crossover
            crossover_count = crossover_count + 1;
            Random generator = new Random(); 
            int cross_point = generator.nextInt(buyers.length) + 1;

            // Cross genes at random spot in strings
            new_gene_1 = population.get(gene_1).substring(0, cross_point) + population.get(gene_2).substring(cross_point);
            new_gene_2 = population.get(gene_2).substring(0, cross_point) + population.get(gene_1).substring(cross_point);

            // Add new genes to breed_population
            breed_population.add(new_gene_1);
            breed_population.add(new_gene_2);
        }
        else {
            // Otherwise, perform cloning
            clone_count = clone_count + 1;
            breed_population.add(population.get(gene_1));
            breed_population.add(population.get(gene_2));
        }

        // Check if mutation is to be performed
        mutateGene();
    }


    /**
     * Gets best solution in population
     * @return int - position of best solution in population
     * @throws LpSolveException 
     */
    private int getBestSolution() throws LpSolveException {
        int best_position = 0;
        double this_fitness = 0;
        double best_fitness = 0;
        for(int i = 0; i < population_size; i++) {
            this_fitness = evalGene(population.get(i));
            if(this_fitness > best_fitness) {
                best_fitness = this_fitness;
                best_position = i;
            }
        }
        return best_position;
    }


    /**
     * Gets mean fitness of generation
     */
    private double getMeanFitness() {
        double total_fitness = 0;
   	double mean_fitness = 0;
        for(int i = 0; i < population_size; i++) {
	    total_fitness = total_fitness + fitness.get(i);
        }
	mean_fitness = total_fitness / population_size;
	return mean_fitness;
    }


    /**
     * Evaluates entire population's fitness, by filling fitness ArrayList
     * with fitness value of each corresponding population member gene
     * @throws LpSolveException 
     */
    private void evalPopulation() throws LpSolveException {       
        total_fitness_of_generation = 0;
        for(int i = 0; i < population_size; i++) {
            double temp_fitness = evalGene(population.get(i));
            fitness.add(temp_fitness);
            total_fitness_of_generation = total_fitness_of_generation + temp_fitness;
        }
    }


    /**
     * Evaluates entire breed_population's fitness, by filling breed_fitness ArrayList
     * with fitness value of each corresponding breed_population member gene
     * @throws LpSolveException 
     */
    private void evalBreedPopulation() throws LpSolveException {
        total_fitness_of_generation = 0;
        for(int i = 0; i < population_size; i++) {
            double temp_fitness = evalGene(breed_population.get(i));
            fitness.add(temp_fitness);
            total_fitness_of_generation = total_fitness_of_generation + temp_fitness;
        }
    }


    /**
     * Evaluates a single gene's fitness, by calculating the total_weight
     * of items selected by the gene
     * @return double - gene's total fitness value
     * @throws LpSolveException 
     */
    private double evalGene(String gene) throws LpSolveException {
    	
    	double defaultFitness = 0;
    	
//    	create linear object with its objective function
		LpSolve solver = LpSolve.makeLp(0, numberOfVariables);
		solver.strSetObjFn(objectFunction);
		
//		generate constraints from gene/solution and solve
//		fitness is the maximum output from the linear object
		ProcessData.generateConsts(solver, sellers, buyers, gene);
		solver.setMaxim();
		solver.setVerbose(LpSolve.IMPORTANT);
		solver.solve();
		
//		return fitness
		if(defaultFitness < solver.getObjective())
			return solver.getObjective();
		else
			return defaultFitness;
    }


    /**  
     * Makes a population by filling population ArrayList with strings of
     * length number_of_items, each element a gene of randomly generated
     * chromosomes (1s and 0s)
     */
    private void makePopulation() {
        for(int i = 0; i < population_size; i++) {
            // Calls makeGene() once for each element position
            population.add(makeGene());      
        }
    }


    /**  
     * Generates a single gene, a random String of 1s and 0s
     * @return String - a randomly generated gene
     */
    private String makeGene() {

        // Stringbuilder builds gene, one chromosome (1 or 0) at a time
        StringBuilder gene = new StringBuilder(buyers.length);

        // Each chromosome
        char c;

        // Loop creating gene
        for(int i = 0; i < buyers.length; i++) {
            c = '0';
            double rnd = Math.random(); 
            // If random number is greater than 0.5, chromosome is '1'
            // If random number is less than 0.5, chromosome is '0'
            if(rnd > 0.5) {
                c = '1';
            }
            // Append chromosome to gene
            gene.append(c);
        }
        // Stringbuilder object to string; return
        return gene.toString();
    }

    /**  
     * Determines if input string can be converted to integer
     * @param String - string to be checked
     * @return boolean - whether or not string can be converted
     */
    public static boolean isInteger(String str) {
        try { 
            Integer.parseInt(str); 
        } 
        catch(NumberFormatException e) { 
            return false; 
        }
        return true;
    }


    /**  
     * Determines if input string can be converted to double
     * @param String - string to be checked
     * @return boolean - whether or not string can be converted
     */
    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
        }
        catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

} 
