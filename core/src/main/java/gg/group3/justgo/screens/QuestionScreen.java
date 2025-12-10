package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
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

    // UI Elements
    private Label enemyNameLabel;
    private Image enemyImage;
    private Label questionLabel;
    private final Array<TextButton> optionButtons; // CHANGED: Replaced TextField

    private String correctAnswer;
    private boolean isVisible = false;
    private Entity whoQuestionedThePlayer = null;

    public QuestionScreen(Answered answered) {
        super(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();
        this.answered = answered;
        this.skin = new Skin(Gdx.files.internal("ui/question-screen.skin"));

        optionButtons = new Array<>();
        setupUI();
    }

    private void setupUI() {
        // Main Container
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        // mainTable.setDebug(true); // Uncomment to see layout lines

        // --- TOP HEADER: Enemy Name ---
        // Uses the 'title' LabelStyle from the skin
        enemyNameLabel = new Label("Enemy Encounter!", skin, "title");
        enemyNameLabel.setAlignment(Align.center);
        mainTable.add(enemyNameLabel).colspan(2).padTop(20).padBottom(20).row();

        // --- LEFT SIDE: Enemy Portrait ---
        enemyImage = new Image();
        enemyImage.setScaling(Scaling.fit);
        mainTable.add(enemyImage).size(128, 128).pad(20);

        // --- RIGHT SIDE: Question & Options ---
        Table rightTable = new Table();

        // Question Text
        // Uses the 'question' LabelStyle from the skin
        questionLabel = new Label("???", skin, "question");
        questionLabel.setWrap(true);
        questionLabel.setAlignment(Align.center);
        rightTable.add(questionLabel).width(300).padBottom(20).row();

        // Options Grid (2x2)
        Table optionsTable = new Table();
        for (int i = 0; i < 4; i++) {
            // FIX: Explicitly using the 'submit' TextButtonStyle from the skin
            TextButton btn = new TextButton("", skin, "submit");
            btn.getLabel().setFontScale(1.2f);

            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    checkAnswer(btn.getText().toString());
                }
            });

            optionButtons.add(btn);
            optionsTable.add(btn).size(120, 60).pad(10);

            if (i == 1) optionsTable.row(); // Break after 2nd button
        }

        rightTable.add(optionsTable);
        mainTable.add(rightTable).pad(20);

        addActor(mainTable);
    }

    public void setQuestion(String question, String answer, Array<String> options, Entity target) {
        this.correctAnswer = answer;
        this.whoQuestionedThePlayer = target;

        // 1. Update Labels
        questionLabel.setText(question);
        enemyNameLabel.setText(target != null ? "BATTLE!" : "TRAP!");

        // 2. Update Enemy Image
        if (target != null) {
            TextureRegion region = new TextureRegion(target);
            enemyImage.setDrawable(new TextureRegionDrawable(region));
        }

        // 3. Update Buttons
        for (int i = 0; i < 4; i++) {
            if (i < options.size) {
                optionButtons.get(i).setText(options.get(i));
                optionButtons.get(i).setVisible(true);
                // Re-enable button in case it was disabled previously
                optionButtons.get(i).setDisabled(false);
            } else {
                optionButtons.get(i).setVisible(false);
            }
        }
    }

    private void checkAnswer(String selectedAnswer) {
        // Disable all buttons to prevent double-clicking
        for (TextButton btn : optionButtons) {
            btn.setDisabled(true);
        }

        if (selectedAnswer.equals(correctAnswer)) {
            answered.onCorrect(whoQuestionedThePlayer);
        } else {
            answered.onWrong(whoQuestionedThePlayer);
        }
        hide();
    }

    public void show() {
        if (!isVisible) {
            isVisible = true;
            Gdx.input.setInputProcessor(this);
        }
    }

    public void hide() {
        if (isVisible) {
            isVisible = false;
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void draw() {
        if (!isVisible) return;

        // Dark Background Overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f); // Darker background
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
