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

    Button btnAdd, btnMinus, btnMultiply, btnDecimal, btnCancel, btn0, btnDivide;

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
        setOperatorButton(R.id.btnmutiply, "*");
        setOperatorButton(R.id.btndevide, "/");

        // Equals and Cancel buttons
        Button btnEquals = findViewById(R.id.btnequals);
        Button btnCancel = findViewById(R.id.btncuncel);

        btnEquals.setOnClickListener(v -> calculate());
        btnCancel.setOnClickListener(v -> {
            currentInput = "";
            input.setText("");
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

    private void setNumberButton(int id, String value) {
        Button button = findViewById(id);
        button.setOnClickListener(v -> {
            // Prevent multiple decimal points in a number
            if (value.equals(".")) {
                String[] parts = currentInput.split("[-+*/]");
                if (parts.length > 0 && parts[parts.length - 1].contains(".")) {
                    return; // Don't add another decimal point
                }
            }
            currentInput += value;
            input.setText(currentInput);
        });
    }

    private void calculate() {

        try {
            if (currentInput.isEmpty()) return;

            double result = eval(currentInput);
            input.setText(String.valueOf(result));
            currentInput = String.valueOf(result);
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

        if (numbers.isEmpty()) return 0;
        return numbers.pop();
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    private Double applyOp(Double a, Double b, char op) {
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
            default: return 0.0;
        }
    }
}