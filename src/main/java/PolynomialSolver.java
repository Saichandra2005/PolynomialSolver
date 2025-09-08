import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PolynomialSolve {

    // Simple rational class using BigInteger
    static class Rational {
        BigInteger num; // numerator
        BigInteger den; // denominator > 0

        Rational(BigInteger n, BigInteger d) {
            if (d.signum() == 0) throw new ArithmeticException("Zero denominator");
            if (d.signum() < 0) { n = n.negate(); d = d.negate(); }
            BigInteger g = n.gcd(d);
            this.num = n.divide(g);
            this.den = d.divide(g);
        }

        Rational add(Rational other) {
            BigInteger n = this.num.multiply(other.den).add(other.num.multiply(this.den));
            BigInteger d = this.den.multiply(other.den);
            return new Rational(n, d);
        }

        Rational mul(BigInteger k) {
            return new Rational(this.num.multiply(k), this.den);
        }

        @Override
        public String toString() {
            if (den.equals(BigInteger.ONE)) return num.toString();
            return num + "/" + den;
        }
    }

    public static void main(String[] args) throws Exception {
        // Load JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File("src/main/resources/input.json"));
        int k = root.get("keys").get("k").asInt(); // use first k points

        // Read first k points (x, y)
        int[] xs = new int[k];
        BigInteger[] ys = new BigInteger[k];
        int idx = 0;

        Map<Integer, BigInteger> points = new LinkedHashMap<>();
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if ("keys".equals(key)) continue;
            int x = Integer.parseInt(key);
            int base = root.get(key).get("base").asInt();
            String val = root.get(key).get("value").asText();
            BigInteger y = new BigInteger(val, base);
            points.put(x, y);
        }

        for (Map.Entry<Integer, BigInteger> e : points.entrySet()) {
            if (idx >= k) break;
            xs[idx] = e.getKey();
            ys[idx] = e.getValue();
            idx++;
        }

        // Compute exact constant term P(0) using Lagrange
        Rational result = new Rational(BigInteger.ZERO, BigInteger.ONE);

        for (int i = 0; i < k; i++) {
            BigInteger xi = BigInteger.valueOf(xs[i]);
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                BigInteger xj = BigInteger.valueOf(xs[j]);
                numerator = numerator.multiply(xj.negate()); // * (-x_j)
                denominator = denominator.multiply(xi.subtract(xj)); // * (x_i - x_j)
            }

            Rational term = new Rational(ys[i].multiply(numerator), denominator);
            result = result.add(term);
        }

        // Print only constant term
        System.out.println("constant c: " + result);
    }
}

