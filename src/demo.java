import lpsolve.*;

public class demo {
	public static void main(String[] args) {
    try {
      // Create a problem with 4 variables and 0 constraints
      LpSolve solver = LpSolve.makeLp(0, 20);

      // set objective function
      solver.strSetObjFn("2  4.0  3.0  4.0  5.0  -1.0  1.0  0.0  1.0  2.0  1.0  3.0  2.0  3.0  4.0  -2.0  0.0  -1.0  0.0  1.0");
      for(int i=1;i<=20;i++) {
    	  solver.setInt(i, true);
    	  
      }
      solver.setBounds(1, 0, 10);
      solver.setBounds(2, 0, 6);
      solver.setBounds(3, 0, 8);
      solver.setBounds(4, 0, 10);
      solver.setBounds(5, 0, 7);
      solver.setBounds(6, 0, 8);
      solver.setBounds(7, 0, 6);
      solver.setBounds(8, 0, 8);
      solver.setBounds(9, 0, 8);
      solver.setBounds(10, 0, 7);
      solver.setBounds(11, 0, 5);
      solver.setBounds(12, 0, 5);
      solver.setBounds(13, 0, 5);
      solver.setBounds(14, 0, 5);
      solver.setBounds(15, 0, 5);
      solver.setBounds(16, 0, 10);
      solver.setBounds(17, 0, 6);
      solver.setBounds(18, 0, 8);
      solver.setBounds(19, 0, 12);
      solver.setBounds(20, 0, 7);

      
      // solve the problem
      
      // add constraints
      solver.strAddConstraint("1  1.0  1.0  1.0  1.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0", LpSolve.LE, 10);
      solver.strAddConstraint("0  0.0  0.0  0.0  0.0  1.0  1.0  1.0  1.0  1.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0", LpSolve.LE, 8);
      solver.strAddConstraint("0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  1.0  1.0  1.0  1.0  1.0  0.0  0.0  0.0  0.0  0.0", LpSolve.LE, 5);
      solver.strAddConstraint("0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  0.0  1.0  1.0  1.0  1.0  1.0", LpSolve.LE, 15);
      solver.strAddConstraint("1  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0", LpSolve.LE, 10);
      solver.strAddConstraint("0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0", LpSolve.LE, 6);
      solver.strAddConstraint("0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0", LpSolve.LE, 8);
      solver.strAddConstraint("0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0", LpSolve.LE, 12);
      solver.strAddConstraint("0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0  0.0  0.0  0.0  0.0  1.0", LpSolve.LE, 7);
      solver.setMaxim();
      solver.solve();

      // print solution
      System.out.println("Value of objective function: " + solver.getObjective());
      double[] var = solver.getPtrVariables();
      for (int i = 0; i < var.length; i++) {
        System.out.println("Value of var[" + i + "] = " + var[i]);
      }

      // delete the problem and free memory
      solver.deleteLp();
    }
    catch (LpSolveException e) {
       e.printStackTrace();
    }
  }

}
