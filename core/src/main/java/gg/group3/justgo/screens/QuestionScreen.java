package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import gg.group3.justgo.entities.Entity;

public class QuestionScreen extends Stage {
    public interface Answered {
        void onCorrect(Entity whoQuestionedThePlayer);
        void onWrong(Entity whoQuestionedThePlayer);
        void onCancel();
    }

    private final ShapeRenderer shapeRenderer;
    private final Answered answered;
    private final Skin skin;
    private TextField answerField;
    private Label questionLabel;
    private String correctAnswer;
    private boolean isVisible = false;

    private Entity whoQuestionedThePlayer = null;

    public QuestionScreen(Answered answered) {
        super(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();
        this.answered = answered;
        this.correctAnswer = "9"; // For the example question "3 + 6 = ?"

        // Create skin
        skin = new Skin(Gdx.files.internal("ui/question-screen.skin"));

        setupUI();
    }

    private void setupUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        // Title
        Label titleLabel = new Label("Question", skin, "title");

        // Question
        questionLabel = new Label("", skin, "question");

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
                hide();
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
    }

    public void show() {
        if (!isVisible) {
            isVisible = true;
            // Store the current input processor
            // Set this stage as the input processor
            Gdx.input.setInputProcessor(this);
            // Set focus to text field
            setKeyboardFocus(answerField);
        }
    }

    public void hide() {
        if (isVisible) {
            isVisible = false;
            // Restore the previous input processor
            Gdx.input.setInputProcessor(null);
        }
    }

    private void checkAnswer() {
        String userAnswer = answerField.getText().trim();

        if (userAnswer.equals(correctAnswer)) {
            answered.onCorrect(this.whoQuestionedThePlayer);
            hide();
        } else {
            answered.onWrong(this.whoQuestionedThePlayer);
        }
    }

    public void setQuestion(String question, String answer, Entity whoQuestionedThePlayer) {
        this.correctAnswer = answer;
        questionLabel.setText(question);
        answerField.setText(""); // Clear the answer field
        this.whoQuestionedThePlayer = whoQuestionedThePlayer;
    }

    public void setQuestion(String question, String answer) {
        setQuestion(question, answer, null);
    }

    @Override
    public void draw() {
        if (!isVisible) {
            return; // Don't draw if not visible
        }

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
    public void act(float delta) {
        if (!isVisible) {
            return; // Don't update if not visible
        }
        super.act(delta);
    }

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
        skin.dispose();
    }
}
