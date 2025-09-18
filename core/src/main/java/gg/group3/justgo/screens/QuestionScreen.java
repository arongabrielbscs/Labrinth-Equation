package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class QuestionScreen extends Stage {
    public interface Answered {
        void onCorrect();
        void onWrong();
        void onCancel();
    }

    private final ShapeRenderer shapeRenderer;
    private final Answered answered;
    private final Skin skin;
    private TextField answerField;
    private String correctAnswer;

    public QuestionScreen(Answered answered) {
        super(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();
        this.answered = answered;
        this.correctAnswer = "9"; // For the example question "3 + 6 = ?"

        // Create skin
        skin = new Skin(Gdx.files.internal("ui/question-screen.skin"));

        setupUI();
    }

    private Texture createColorTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void setupUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        // Title
        Label titleLabel = new Label("Question", skin, "title");

        // Question
        Label questionLabel = new Label("3 + 6 = ?", skin, "question");

        // Answer input field
        answerField = new TextField("", skin);
        answerField.setMessageText("Enter your answer...");
        answerField.setAlignment(1); // Center alignment

        // Buttons table
        Table buttonTable = new Table();

        TextButton submitButton = new TextButton("Submit", skin, "submit");
        TextButton cancelButton = new TextButton("Cancel", skin, "cancel");

        // Add button listeners
        submitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                checkAnswer();
            }
        });

        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                answered.onCancel();
            }
        });

        // Layout buttons horizontally
        buttonTable.add(cancelButton).pad(10);
        buttonTable.add(submitButton).pad(10);

        // Add all elements to main table
        mainTable.add(titleLabel).center().padBottom(50).row();
        mainTable.add(questionLabel).center().padBottom(30).row();
        mainTable.add(answerField).width(300).height(50).center().padBottom(30).row();
        mainTable.add(buttonTable).center();

        addActor(mainTable);

        // Set focus to text field
        setKeyboardFocus(answerField);
    }

    private void checkAnswer() {
        String userAnswer = answerField.getText().trim();

        if (userAnswer.equals(correctAnswer)) {
            answered.onCorrect();
        } else {
            answered.onWrong();
        }
    }

    // Method to set a new question
    public void setQuestion(String question, String answer) {
        this.correctAnswer = answer;
        // You'd need to store references to the labels to update them
        // Or recreate the UI - depends on your needs
    }

    @Override
    public void draw() {
        // Semi-transparent black background
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        super.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
        skin.dispose();
    }
}
