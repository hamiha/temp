import lpsolve.*;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

import io.jenetics.BitGene;
import io.jenetics.DoubleGene;
import io.jenetics.MeanAlterer;
import io.jenetics.Mutator;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.DoubleRange;
import io.jenetics.util.ISeq;

public class demo {
	
	public static int[][] buyers = {{7,7}, {7,6} , {6,7}, {6,7}, {8,8}, {9,9}};
	public static int[][] sellers = {{5,6}, {5,5} , {5,7}, {6,4}};
	static ArrayList<String> alloc = new ArrayList<String>();	
	private static char[] set = {'0', '1'};
	
	private static double fitness(String alloc) throws LpSolveException {
		double defaultFitness = 0;
    	
//    	create linear object with its objective function
		LpSolve solver = LpSolve.makeLp(0, ProcessData.getNumberOfVariables(sellers, buyers));
		solver.strSetObjFn(ProcessData.generateObjFuction(sellers, buyers));
		
//		generate constraints from gene/solution and solve
//		fitness is the maximum output from the linear object
		ProcessData.generateConsts(solver, sellers, buyers, alloc);
		solver.setMaxim();
		solver.setVerbose(LpSolve.IMPORTANT);
		solver.solve();
		
//		return fitness
		if(defaultFitness < solver.getObjective())
			return solver.getObjective();
		else
			return defaultFitness;

	}
	
	static void CalculateWelfare() {
		 int n = set.length;
		 genPossibleOutCome(set, "", n, buyers.length);
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
	
	public static final class Buyer{
		private static final long serialVersionUID = 1L;

		private final double _size;
		private final double _value;
		
		private Buyer(final double size, final double value) {
			_size = size;
			_value = value;
		}
		
		public static Collector<Buyer, ?, Buyer> toSum() {
			return Collector.of(
				() -> new double[2],
				(a, b) -> {a[0] += b._size; a[1] += b._value;},
				(a, b) -> {a[0] += b[0]; a[1] += b[1]; return a;},
				r -> new Buyer(r[0], r[1])
			);
		}
	}
	

	
	

	public static void main(final String[] args) {
	
	}


}
