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
import gg.group3.justgo.entities.SpikeEntity;

public class QuestionScreen extends Stage {
    // ... Interface definition remains the same ...
    public interface Answered {
        void onCorrect(Entity whoQuestionedThePlayer);
        void onWrong(Entity whoQuestionedThePlayer);
        void onCancel();
    }

    private final ShapeRenderer shapeRenderer;
    private final Answered answered;
    private final Skin skin;
    private final TextureRegion heartTexture; // NEW: Texture for hearts

    private Label enemyNameLabel;
    private Image enemyImage;
    private Label questionLabel;
    private final Array<TextButton> optionButtons;
    private Table enemyHeartTable; // NEW: Table for enemy health
    private Table playerHeartTable; // NEW: Table for player health

    private String correctAnswer;
    private boolean isVisible = false;
    private Entity whoQuestionedThePlayer = null;

    public QuestionScreen(Answered answered, TextureRegion heartTexture) { // MODIFIED: Accepts heart texture
        super(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();
        this.answered = answered;
        this.heartTexture = heartTexture;
        this.skin = new Skin(Gdx.files.internal("ui/question-screen.skin"));

        optionButtons = new Array<>();
        setupUI();
    }

    private void setupUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        // Top Header
        enemyNameLabel = new Label("BATTLE!", skin, "title");
        mainTable.add(enemyNameLabel).colspan(2).padTop(10).padBottom(10).row();

        // Left Side: Enemy Container
        Table enemyContainer = new Table();
        enemyImage = new Image();
        enemyImage.setScaling(Scaling.fit);
        enemyContainer.add(enemyImage).size(128, 128).row();

        // Enemy Hearts
        enemyHeartTable = new Table();
        enemyContainer.add(enemyHeartTable).padTop(5);
        mainTable.add(enemyContainer).pad(20);

        // Right Side: Options
        Table rightTable = new Table();
        questionLabel = new Label("???", skin, "question");
        questionLabel.setWrap(true);
        questionLabel.setAlignment(Align.center);
        rightTable.add(questionLabel).width(300).padBottom(20).row();

        Table optionsTable = new Table();
        for (int i = 0; i < 4; i++) {
            TextButton btn = new TextButton("", skin, "submit");
            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    checkAnswer(btn.getText().toString());
                }
            });
            optionButtons.add(btn);
            optionsTable.add(btn).size(120, 60).pad(10);
            if (i == 1) optionsTable.row();
        }
        rightTable.add(optionsTable);
        mainTable.add(rightTable).pad(20).row();

        // Bottom: Player Health Container
        Table playerContainer = new Table();
        playerContainer.add(new Label("PLAYER HP: ", skin)).padRight(10);
        playerHeartTable = new Table();
        playerContainer.add(playerHeartTable);
        mainTable.add(playerContainer).colspan(2).padTop(20);

        addActor(mainTable);
    }

    // Helper method to fill heart tables
    private void updateHeartDisplay(Table table, int currentHealth) {
        table.clearChildren();
        for (int i = 0; i < currentHealth; i++) {
            table.add(new Image(heartTexture)).width(24).height(24).pad(2);
        }
    }

    // UPDATED: Now accepts player health
    public void setQuestion(String question, String answer, Array<String> options, Entity target, int playerHealth) {
        this.correctAnswer = answer;
        this.whoQuestionedThePlayer = target;

        if (target instanceof SpikeEntity) {
            enemyNameLabel.setText("Get Spiked!");
        } else if (target.isEnemy()) {
            enemyNameLabel.setText("Battle!");
        } else {
            enemyNameLabel.setText("Door!");
        }
        questionLabel.setText(question);

        // Update Enemy Portrait
        if (target != null) {
            enemyImage.setDrawable(new TextureRegionDrawable(new TextureRegion(target)));
            updateHeartDisplay(enemyHeartTable, target.getHealth());
        }

        // Update Player Health display
        updateHeartDisplay(playerHeartTable, playerHealth);

        // Update Buttons
        for (int i = 0; i < 4; i++) {
            optionButtons.get(i).setText(options.get(i));
            optionButtons.get(i).setDisabled(false);
        }
    }

    // ... show, hide, checkAnswer methods remain largely the same ...
    private void checkAnswer(String selectedAnswer) {
        for (TextButton btn : optionButtons) btn.setDisabled(true);
        if (selectedAnswer.equals(correctAnswer)) {
            answered.onCorrect(whoQuestionedThePlayer);
        } else {
            answered.onWrong(whoQuestionedThePlayer);
        }
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

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void draw() {
        if (!isVisible) return;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.9f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        super.draw();
    }
}
