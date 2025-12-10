package gg.group3.justgo.utils;

import com.badlogic.gdx.utils.Array;
import java.util.Random;

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

    public String getAnswer() { return answer; }
    public String getQuestion() { return question; }
    public Array<String> getOptions() { return options; }
}
