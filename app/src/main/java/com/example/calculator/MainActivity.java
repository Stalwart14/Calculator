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

        // Equals and Clear buttons
        Button btnEquals = findViewById(R.id.btnequals);
        Button btnClear = findViewById(R.id.btnClear);

        btnEquals.setOnClickListener(v -> calculate());

        btnClear.setOnClickListener(v -> {
            currentInput = "";
            input.setText("0");
        });
    }

    private void setNumberButton(int id, String value) {
        Button button = findViewById(id);
        button.setOnClickListener(v -> {
            if (value.equals(".")) {
                String[] parts = currentInput.split("[-+*/]");
                if (parts.length > 0 && parts[parts.length - 1].contains(".")) {
                    return; // Prevent multiple decimals in a number
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

    private void calculate() {
        try {
            if (currentInput.isEmpty()) return;

            double result = eval(currentInput);
            if (Double.isNaN(result)) {
                input.setText("Error");
                currentInput = "";
                return;
            }

            // Remove .0 if result is whole number
            String formatted = (result == (long) result) ? String.format("%d", (long) result) : String.valueOf(result);
            input.setText(formatted);
            currentInput = formatted;

        } catch (Exception e) {
            input.setText("Error");
            currentInput = "";
            Toast.makeText(this, "Calculation error", Toast.LENGTH_SHORT).show();
        }
    }

    private double eval(String expr) {
        expr = expr.replaceAll("\\s+", ""); // Remove whitespace
        String[] tokens = expr.split("(?<=[-+*/])|(?=[-+*/])");

        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (token.matches("[0-9.]+")) {
                numbers.push(Double.parseDouble(token));
            } else if (token.matches("[+\\-*/]")) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token.charAt(0))) {
                    double b = numbers.pop();
                    double a = numbers.pop();
                    char op = operators.pop();
                    numbers.push(applyOp(a, b, op));
                }
                operators.push(token.charAt(0));
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
        return (op == '+' || op == '-') ? 1 : 2;
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
            default: return 0;
        }
    }
}
