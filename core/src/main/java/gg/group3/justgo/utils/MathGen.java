package gg.group3.justgo.utils;

import java.util.Random;

public class MathGen {
    private final String question;
    private final String answer;

    private static final Random random = new Random();

    private MathGen(String answer, String question) {
        this.answer = answer;
        this.question = question;
    }

    public static MathGen generateBasicArithmetic(int maxNum) {
        // Randomly choose an operation (0=add, 1=subtract, 2=multiply, 3=divide)
        int operation = random.nextInt(4);

        String question;
        int result;
        int num1, num2;

        switch (operation) {
            case 0: // Addition
                // Generate numbers so that sum doesn't exceed maxNum
                num1 = random.nextInt(maxNum) + 1; // 1 to maxNum
                num2 = random.nextInt(maxNum) + 1; // 1 to (maxNum - num1)
                question = num1 + " + " + num2 + " = ?";
                result = num1 + num2;
                break;

            case 1: // Subtraction
                // Generate numbers so that result is positive and within range
                result = random.nextInt(maxNum) + 1; // 1 to maxNum
                num2 = random.nextInt(result) + 1; // 1 to result (ensures positive result)
                num1 = result + num2;
                question = num1 + " - " + num2 + " = ?";
                break;

            case 2: // Multiplication
                // Generate numbers so that both operands are <= maxNum
                num1 = random.nextInt(maxNum) + 1; // 1 to maxNum
                num2 = random.nextInt(maxNum) + 1; // 1 to maxNum
                question = num1 + " * " + num2 + " = ?";
                result = num1 * num2;
                break;

            case 3: // Division
                // Generate numbers so that both dividend and divisor are <= maxNum
                num2 = random.nextInt(maxNum) + 1; // divisor: 1 to maxNum
                int quotient = random.nextInt(maxNum) + 1; // quotient: 1 to maxNum
                num1 = Math.min(num2 * quotient, maxNum); // ensure dividend <= maxNum

                // If the calculated dividend exceeds maxNum, adjust
                if (num2 * quotient > maxNum) {
                    num1 = maxNum;
                    quotient = num1 / num2; // recalculate quotient
                    num1 = num2 * quotient; // adjust dividend to ensure whole division
                }

                question = num1 + " / " + num2 + " = ?";
                result = quotient;
                break;

            default:
                // Fallback to addition
                num1 = random.nextInt(maxNum) + 1;
                num2 = random.nextInt(maxNum - num1) + 1;
                question = num1 + " + " + num2 + " = ?";
                result = num1 + num2;
        }

        return new MathGen(String.valueOf(result), question);
    }

    public String getAnswer() {
        return answer;
    }

    public String getQuestion() {
        return question;
    }
}
