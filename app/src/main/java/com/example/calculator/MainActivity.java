package com.example.calculator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    // UI Components
    TextView input;
    String currentInput = "";

    // Tracks whether the next bracket should be opening '(' or closing ')'
    boolean isOpeningBracket = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize the display TextView
        input = findViewById(R.id.input);

        // ======================
        // SET UP NUMBER BUTTONS
        // ======================
        setNumberButton(R.id.btn0, "0");
        setNumberButton(R.id.btn1, "1");
        setNumberButton(R.id.btn2, "2");
        setNumberButton(R.id.btn3, "3");
        setNumberButton(R.id.btn4, "4");
        setNumberButton(R.id.btn5, "5");
        setNumberButton(R.id.btn6, "6");
        setNumberButton(R.id.btn7, "7");
        setNumberButton(R.id.btn8, "8");
        setNumberButton(R.id.btn9, "9");
        setNumberButton(R.id.btndecimal, ".");

        // ========================
        // SET UP OPERATOR BUTTONS
        // ========================
        setOperatorButton(R.id.btnadd, "+");
        setOperatorButton(R.id.btnminus, "-");
        setOperatorButton(R.id.btnmultiply, "*");
        setOperatorButton(R.id.btndivide, "/");

        // =====================
        // BRACKET BUTTON LOGIC
        // =====================
        Button btnBracket = findViewById(R.id.btnOpenBracket);
        btnBracket.setOnClickListener(v -> {

            // Alternate between opening and closing brackets
            if (isOpeningBracket) {
                currentInput += "(";
            } else {
                currentInput += ")";
            }
            isOpeningBracket = !isOpeningBracket;
            input.setText(currentInput);
        });

        // ===========================
        // PLUS/MINUS TOGGLE BUTTON
        // ===========================
        Button btnPlusMinus = findViewById(R.id.btnPlusMinus);
        btnPlusMinus.setOnClickListener(v -> toggleNegative());

        // ====================
        // PERCENTAGE BUTTON
        // ====================
        Button btnPercent = findViewById(R.id.btnPercent);
        btnPercent.setOnClickListener(v -> {
            if (!currentInput.isEmpty() && !endsWithOperator(currentInput)) {
                try {
                    double value = eval(currentInput);

                    // Convert to percentage
                    double percent = value / 100;

                    // Format to remove decimal if it's a whole number
                    currentInput = (percent == (long) percent) ?
                            String.format("%d", (long) percent) :
                            String.valueOf(percent);
                    input.setText(currentInput);
                } catch (Exception e) {
                    input.setText("Error");
                    currentInput = "";
                }
            }
        });

        // ======================
        // EQUALS BUTTON (CALCULATE)
        // ======================
        Button btnEquals = findViewById(R.id.btnequals);
        btnEquals.setOnClickListener(v -> calculate());

        // =================
        // CLEAR BUTTON
        // =================
        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> {
            currentInput = "";
            input.setText("0");
            isOpeningBracket = true;
        });
    }

    /**
     * Helper method to configure number buttons
     * @param id The button's resource ID
     * @param value The numeric value this button represents
     */
    private void setNumberButton(int id, String value) {
        Button button = findViewById(id);
        button.setOnClickListener(v -> {
            if (value.equals(".")) {
                String[] parts = currentInput.split("[-+*/()]");
                if (parts.length > 0 && parts[parts.length - 1].contains(".")) {
                    return;
                }
            }
            currentInput += value;
            input.setText(currentInput);
        });
    }

    /**
     * Helper method to configure operator buttons
     * @param buttonId The button's resource ID
     * @param operator The operator symbol this button represents
     */
    private void setOperatorButton(int buttonId, String operator) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            if (!currentInput.isEmpty() && !endsWithOperator(currentInput)) {
                currentInput += operator;
                input.setText(currentInput);
            }
        });
    }

    /**
     * Checks if the input string ends with an operator
     * @param input The string to check
     * @return true if the string ends with +, -, *, or /
     */
    private boolean endsWithOperator(String input) {
        if (input.isEmpty()) return false;
        char lastChar = input.charAt(input.length() - 1);
        return lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/';
    }

    /**
     * Toggles the sign of the current number (positive/negative)
     */
    private void toggleNegative() {
        if (currentInput.isEmpty()) return;

        // Find the position of the last operator
        int lastOperatorIndex = Math.max(
                Math.max(currentInput.lastIndexOf('+'), currentInput.lastIndexOf('-')),
                Math.max(currentInput.lastIndexOf('*'), currentInput.lastIndexOf('/'))
        );

        // Extract the number after the last operator
        String number = currentInput.substring(lastOperatorIndex + 1);
        String prefix = currentInput.substring(0, lastOperatorIndex + 1);

        // Toggle the sign
        if (number.startsWith("-")) {
            number = number.substring(1);  // Remove negative sign
        } else {
            number = "-" + number;         // Add negative sign
        }

        currentInput = prefix + number;
        input.setText(currentInput);
    }

    /**
     * Calculates and displays the result of the current expression
     */
    private void calculate() {
        try {
            if (currentInput.isEmpty()) return;

            double result = eval(currentInput);

            // Handle division by zero and other math errors
            if (Double.isNaN(result)) {
                input.setText("Error");
                currentInput = "";
                return;
            }

            // Format to remove decimal if it's a whole number
            String formatted = (result == (long) result) ?
                    String.format("%d", (long) result) : String.valueOf(result);
            input.setText(formatted);
            currentInput = formatted;
            isOpeningBracket = true;

        } catch (Exception e) {
            input.setText("Error");
            currentInput = "";
            Toast.makeText(this, "Calculation error", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Evaluates a mathematical expression using the Shunting-yard algorithm
     * @param expr The expression string to evaluate
     * @return The numeric result of the evaluation
     */
    private double eval(String expr) {
        expr = expr.replaceAll("\\s+", "");
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int i = 0;  // Current position in the expression string

        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (c == '(') {
                operators.push(c);
                i++;
            }
            // Handle numbers (including decimals)
            else if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                numbers.push(Double.parseDouble(sb.toString()));
            }
            // Handle closing bracket
            else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    numbers.push(applyOp(numbers.pop(), numbers.pop(), operators.pop()));
                }
                operators.pop();
                i++;
            }
            // Handle standard operators
            else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    numbers.push(applyOp(numbers.pop(), numbers.pop(), operators.pop()));
                }
                operators.push(c);
                i++;
            } else {
                i++;
            }
        }

        // Process any remaining operators
        while (!operators.isEmpty()) {
            numbers.push(applyOp(numbers.pop(), numbers.pop(), operators.pop()));
        }

        return numbers.isEmpty() ? 0 : numbers.pop();
    }

    /**
     * Determines operator precedence for evaluation order
     * @param op The operator character
     * @return Precedence level (higher number = higher precedence)
     */
    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        else if (op == '*' || op == '/') return 2;
        return 0;
    }

    /**
     * Applies an arithmetic operation to two numbers
     * @param a The first operand
     * @param b The second operand
     * @param op The operator (+, -, *, /)
     * @return The result of the operation
     * @throws ArithmeticException for division by zero
     */
    private double applyOp(double a, double b, char op) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) {
                    Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show();
                    return Double.NaN;
                }
                return a / b;
            default: return 0;
        }
    }
}