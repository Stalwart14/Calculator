package com.example.calculator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    TextView input;
    String currentInput = "";
    boolean isOpeningBracket = true; // tracks if next bracket should be opening

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        input = findViewById(R.id.input);

        // Number buttons
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

        // Operator buttons
        setOperatorButton(R.id.btnadd, "+");
        setOperatorButton(R.id.btnminus, "-");
        setOperatorButton(R.id.btnmultiply, "*");
        setOperatorButton(R.id.btndivide, "/");

        // Bracket Button
        Button btnBracket = findViewById(R.id.btnOpenBracket);
        btnBracket.setOnClickListener(v -> {
            if (isOpeningBracket) {
                currentInput += "(";
            } else {
                currentInput += ")";
            }
            isOpeningBracket = !isOpeningBracket;
            input.setText(currentInput);
        });

        // Plus/Minus Button
        Button btnPlusMinus = findViewById(R.id.btnPlusMinus);
        btnPlusMinus.setOnClickListener(v -> toggleNegative());

        // Percentage Button
        Button btnPercent = findViewById(R.id.btnPercent);
        btnPercent.setOnClickListener(v -> {
            if (!currentInput.isEmpty() && !endsWithOperator(currentInput)) {
                try {
                    double value = eval(currentInput);
                    double percent = value / 100;
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

        // Equals and Clear buttons
        Button btnEquals = findViewById(R.id.btnequals);
        Button btnClear = findViewById(R.id.btnClear);

        btnEquals.setOnClickListener(v -> calculate());

        btnClear.setOnClickListener(v -> {
            currentInput = "";
            input.setText("0");
            // reseting bracket
            isOpeningBracket = true;
        });
    }

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

    private void setOperatorButton(int buttonId, String operator) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            if (!currentInput.isEmpty() && !endsWithOperator(currentInput)) {
                currentInput += operator;
                input.setText(currentInput);
            }
        });
    }

    private boolean endsWithOperator(String input) {
        if (input.isEmpty()) return false;
        char lastChar = input.charAt(input.length() - 1);
        return lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/';
    }

    private void toggleNegative() {
        if (currentInput.isEmpty()) return;

        int lastOperatorIndex = Math.max(
                Math.max(currentInput.lastIndexOf('+'), currentInput.lastIndexOf('-')),
                Math.max(currentInput.lastIndexOf('*'), currentInput.lastIndexOf('/'))
        );

        String number = currentInput.substring(lastOperatorIndex + 1);
        String prefix = currentInput.substring(0, lastOperatorIndex + 1);

        if (number.startsWith("-")) {
            number = number.substring(1);
        } else {
            number = "-" + number;
        }

        currentInput = prefix + number;
        input.setText(currentInput);
    }

    private void calculate() {
        try {
            if (currentInput.isEmpty()) return;

            double result = eval(currentInput);
            if (Double.isNaN(result)) {
                input.setText("Error");
                currentInput = "";
                return;
            }

            String formatted = (result == (long) result) ? String.format("%d", (long) result) : String.valueOf(result);
            input.setText(formatted);
            currentInput = formatted;
            // reseting bracket
            isOpeningBracket = true;

        } catch (Exception e) {
            input.setText("Error");
            currentInput = "";
            Toast.makeText(this, "Calculation error", Toast.LENGTH_SHORT).show();
        }
    }

    private double eval(String expr) {
        expr = expr.replaceAll("\\s+", "");
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int i = 0;

        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (c == '(') {
                operators.push(c);
                i++;
            } else if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                numbers.push(Double.parseDouble(sb.toString()));
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    double b = numbers.pop();
                    double a = numbers.pop();
                    char op = operators.pop();
                    numbers.push(applyOp(a, b, op));
                }
                operators.pop();
                i++;
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    double b = numbers.pop();
                    double a = numbers.pop();
                    char op = operators.pop();
                    numbers.push(applyOp(a, b, op));
                }
                operators.push(c);
                i++;
            } else {
                i++;
            }
        }

        while (!operators.isEmpty()) {
            double b = numbers.pop();
            double a = numbers.pop();
            char op = operators.pop();
            numbers.push(applyOp(a, b, op));
        }

        return numbers.isEmpty() ? 0 : numbers.pop();
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        else if (op == '*' || op == '/') return 2;
        return 0;
    }

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
        }
        return 0;
    }
}
