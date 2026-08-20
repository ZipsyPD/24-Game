/*
 * The point of this class is so that we can evaluate string expressions.
 * This works by having a pointer that goes left to right in the expression.
 * That is the basis of our parser
*/
public class ExpressionEvaluator {
    public ExpressionEvaluator() {}

    public static void main(String[] args) {
        System.out.println(evaluate("2 + 3 * 4"));
        System.out.println(evaluate("6 / (1 - 3 / 4)"));
        System.out.println(evaluate("18       /       9"));
    }

    public static double evaluate(String expression) {
        StringParser sp = new StringParser(expression);
        return sp.parse();
    }

    private static class StringParser {
        private String expression;
        private int pos;

        StringParser(String expression){
            this.expression = expression;
            this.pos = 0;
        }

        public double parse() {
            double result = parseExpression();

            skipSpaces();

            if (pos != expression.length()) {
                throw new IllegalArgumentException(
                        "Unexpected character at position " + pos
                        );
            }
            
            return result;
        }
        // Skip white spaces       
        private void skipSpaces() {
            while (pos < expression.length() 
                    && Character.isWhitespace(expression.charAt(pos))) {
                pos++;
                    }
        }
        
        // Capture one single number by moving our "pos" pointer
        private double parseNumber() {
            skipSpaces();

            int start = pos; 

            while (pos < expression.length()
                    && Character.isDigit(expression.charAt(pos))) {
                pos++;
                    }

            if (start == pos) {
                throw new IllegalArgumentException("Expected a number");
            }

            String numberText = expression.substring(start,pos);

            return Double.parseDouble(numberText);
        }

        // The point of this is to handle the "+" and the "-"
        private double parseExpression() {
            double result = parseTerm();

            while(true) {
                skipSpaces();

                if (pos < expression.length()
                        && expression.charAt(pos) == '+') {
                    pos++;
                    result += parseTerm();

                        }
                else if (pos < expression.length()
                        && expression.charAt(pos) == '-') {
                    pos ++;
                    result -= parseTerm();
                        }
                else {
                    return result;
                }
            }
        }

        // The point of this is to handle the "/" and the "*"
        private double parseTerm() {
            double result = parseFactor();
            
            while (true){
                skipSpaces();

                if (pos < expression.length()
                        && expression.charAt(pos) == '*') {
                    pos ++;
                    result *= parseFactor();
                        }
                else if (pos < expression.length()
                        && expression.charAt(pos) == '/') {
                    pos ++;
                    double divisor = parseFactor();

                    if (Math.abs(divisor) < 0.00001) {
                        throw new ArithmeticException("Division by zero");
                    }
                    result /= divisor;
                        }
                else {
                    return result;
                }
            }
        }

        // Now we can teach the parser if a factor is a number/expression
        private double parseFactor() {
            skipSpaces();

            if (pos < expression.length()
                    && expression.charAt(pos) == '(') {
                pos ++;
                double result = parseExpression();
                skipSpaces();

                if (pos >= expression.length() ||
                        expression.charAt(pos) != ')') {
                    throw new IllegalArgumentException("Missing closing parenthesis");
                        }
                    

                pos++; // here we skip ')'
                return result;
            }

            return parseNumber();
        }
 
    }
}
