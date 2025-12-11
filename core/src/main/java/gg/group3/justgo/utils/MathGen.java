package gg.group3.justgo.utils;

import java.util.Random;

import com.badlogic.gdx.utils.Array;

public class MathGen {
    private final String question;
    private final String answer;
    private final Array<String> options; // Holds correct answer + 3 wrong ones

    private static final Random random = new Random();

    private MathGen(String answer, String question, Array<String> options) {
        this.answer = answer;
        this.question = question;
        this.options = options;
    }

    public static MathGen generateBasicArithmetic(int maxNum) {
        int operation = random.nextInt(4);
        String question;
        int result;
        int num1, num2;

        switch (operation) {
            case 0: // Addition
                num1 = random.nextInt(maxNum) + 1;
                num2 = random.nextInt(maxNum) + 1;
                question = num1 + " + " + num2 + " = ?";
                result = num1 + num2;
                break;
            case 1: // Subtraction
                result = random.nextInt(maxNum) + 1;
                num2 = random.nextInt(result) + 1;
                num1 = result + num2;
                question = num1 + " - " + num2 + " = ?";
                break;
            case 2: // Multiplication
                num1 = random.nextInt(maxNum) + 1;
                num2 = random.nextInt(maxNum) + 1;
                question = num1 + " * " + num2 + " = ?";
                result = num1 * num2;
                break;
            case 3: // Division
                num2 = random.nextInt(maxNum) + 1;
                int quotient = random.nextInt(maxNum) + 1;
                num1 = Math.min(num2 * quotient, maxNum);
                if (num2 * quotient > maxNum) {
                    num1 = maxNum;
                    quotient = num1 / num2;
                    num1 = num2 * quotient;
                }
                question = num1 + " / " + num2 + " = ?";
                result = quotient;
                break;
            default:
                num1 = 1; num2 = 1; result = 2; question = "1 + 1 = ?";
        }

        // --- Generate Distractors (Wrong Answers) ---
        Array<String> options = new Array<>();
        options.add(String.valueOf(result)); // Add correct answer first

        while (options.size < 4) {
            int offset = random.nextInt(10) - 5;
            if (offset == 0) offset = 1;

            int wrongAnswer = result + offset;
            if (wrongAnswer < 0) wrongAnswer = 0;

            String wrongStr = String.valueOf(wrongAnswer);

            if (!options.contains(wrongStr, false)) {
                options.add(wrongStr);
            }
        }

        options.shuffle();

        return new MathGen(String.valueOf(result), question, options);
    }

    public static MathGen generateFindX(int maxNum) {
        int operation = random.nextInt(4);
        String question;
        int x; // The value of X (correct answer)
        int num1, num2;
        
        switch (operation) {
            case 0: // X + a = b  →  X = b - a
                num1 = random.nextInt(maxNum) + 1;
                num2 = random.nextInt(maxNum) + 1;
                x = num2;
                int sum = num1 + num2;
                question = "X + " + num1 + " = " + sum;
                break;
                
            case 1: // X - a = b  →  X = a + b
                num1 = random.nextInt(maxNum) + 1;
                num2 = random.nextInt(maxNum) + 1;
                x = num1 + num2;
                question = "X - " + num1 + " = " + num2;
                break;
                
            case 2: // a * X = b  →  X = b / a
                num1 = random.nextInt(maxNum) + 1;
                x = random.nextInt(maxNum) + 1;
                int product = num1 * x;
                question = num1 + " * X = " + product;
                break;
                
            case 3: // X / a = b  →  X = a * b
                num1 = random.nextInt(maxNum) + 1;
                num2 = random.nextInt(maxNum) + 1;
                x = num1 * num2;
                question = "X / " + num1 + " = " + num2;
                break;
                
            default:
                x = 5;
                question = "X + 3 = 8";
        }
        
        // --- Generate Distractors (Wrong Answers) ---
        Array<String> options = new Array<>();
        options.add(String.valueOf(x)); // Add correct answer first
        
        while (options.size < 4) {
            int offset = random.nextInt(10) - 5;
            if (offset == 0) offset = 1;
            int wrongAnswer = x + offset;
            if (wrongAnswer < 1) wrongAnswer = 1;
            
            String wrongStr = String.valueOf(wrongAnswer);
            if (!options.contains(wrongStr, false)) {
                options.add(wrongStr);
            }
        }
        
        options.shuffle();
        return new MathGen(String.valueOf(x), question, options);
    }

    public static MathGen generatePercentage(int maxNum) {
        int percent = random.nextInt(90) + 10; // random 10% to 100%
        int base = random.nextInt(maxNum) + 1; // number to get percentage of

        // Compute correct answer
        double exact = (percent / 100.0) * base;

        // If whole number, keep integer. Otherwise, keep one decimal.
        String correctAnswer;
        if (exact == Math.floor(exact)) {
            correctAnswer = String.valueOf((int) exact);
        } else {
            correctAnswer = String.format("%.1f", exact);
        }

        String question = "What is " + percent + "% of " + base + "?";

        // --- Generate Options ---
        Array<String> options = new Array<>();
        options.add(correctAnswer);

        while (options.size < 4) {
            // Generate wrong answers by slightly altering the value
            double offset = random.nextInt(21) - 10; // -10 to +10
            if (offset == 0) offset = 1;

            double wrong = exact + offset;

            if (wrong < 0) wrong = Math.abs(wrong);

            String wrongStr;
            if (wrong == Math.floor(wrong)) {
                wrongStr = String.valueOf((int) wrong);
            } else {
                wrongStr = String.format("%.1f", wrong);
            }

            if (!options.contains(wrongStr, false)) {
                options.add(wrongStr);
            }
        }

        options.shuffle();
        return new MathGen(correctAnswer, question, options);
    }


    public String getAnswer() { return answer; }
    public String getQuestion() { return question; }
    public Array<String> getOptions() { return options; }
}
