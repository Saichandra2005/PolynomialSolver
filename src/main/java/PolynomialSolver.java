import java.io.File;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PolynomialSolver {
    public static void main(String[] args) throws Exception {
        // Step 1: Load JSON file
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File("src/main/resources/input.json"));

        int k = root.get("keys").get("k").asInt(); // m+1 points needed

        // Step 2: Extract (x, y) pairs with BigInteger
        Map<Integer, BigInteger> points = new LinkedHashMap<>();
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (key.equals("keys")) continue;

            int x = Integer.parseInt(key);
            int base = root.get(key).get("base").asInt();
            String val = root.get(key).get("value").asText();

            // Parse y as BigInteger to handle huge values
            BigInteger y = new BigInteger(val, base);
            points.put(x, y);
        }

        // Step 3: Prepare matrices (convert BigInteger to double for solving)
        double[][] matrixData = new double[k][k];
        double[] result = new double[k];

        int row = 0;
        for (Map.Entry<Integer, BigInteger> entry : points.entrySet()) {
            if (row >= k) break; // only need first k points
            int x = entry.getKey();
            BigInteger yBig = entry.getValue();

            // Convert y to double (approximation for solving)
            double y = yBig.doubleValue();

            for (int col = 0; col < k; col++) {
                matrixData[row][col] = Math.pow(x, k - col - 1);
            }
            result[row] = y;
            row++;
        }

        // Step 4: Solve linear system
        RealMatrix A = MatrixUtils.createRealMatrix(matrixData);
        RealVector b = MatrixUtils.createRealVector(result);
        DecompositionSolver solver = new LUDecomposition(A).getSolver();
        RealVector solution = solver.solve(b);

        // Step 5: Get constant term only
        double constantC = solution.getEntry(k - 1);

        // Print nicely
        if (Math.abs(constantC - Math.round(constantC)) < 1e-6) {
            System.out.println("constant c: " + (int) Math.round(constantC));
        } else {
            System.out.printf("constant c: %.6f%n", constantC);
        }
    }
}
