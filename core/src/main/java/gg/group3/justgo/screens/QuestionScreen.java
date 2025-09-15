package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
        skin = createSkin();

        setupUI();
    }

    private Skin createSkin() {
        Skin skin = new Skin();

        // Generate fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // Title font
        parameter.size = 100;
        BitmapFont titleFont = generator.generateFont(parameter);
        skin.add("title-font", titleFont);

        // Question font
        parameter.size = 56;
        BitmapFont questionFont = generator.generateFont(parameter);
        skin.add("question-font", questionFont);

        // UI font
        parameter.size = 32;
        BitmapFont uiFont = generator.generateFont(parameter);
        skin.add("ui-font", uiFont);

        generator.dispose();

        // Create button textures
        Texture buttonTexture = createColorTexture(200, 60, new Color(0.2f, 0.6f, 0.2f, 1f)); // Green
        Texture buttonPressedTexture = createColorTexture(200, 60, new Color(0.1f, 0.5f, 0.1f, 1f)); // Darker green
        Texture cancelButtonTexture = createColorTexture(200, 60, new Color(0.6f, 0.2f, 0.2f, 1f)); // Red
        Texture cancelButtonPressedTexture = createColorTexture(200, 60, new Color(0.5f, 0.1f, 0.1f, 1f)); // Darker red

        // Create text field texture
        Texture textFieldTexture = createColorTexture(300, 50, new Color(0.3f, 0.3f, 0.3f, 1f)); // Dark gray
        Texture textFieldFocusTexture = createColorTexture(300, 50, new Color(0.4f, 0.4f, 0.4f, 1f)); // Lighter gray

        // Label styles
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        skin.add("title", titleStyle);

        Label.LabelStyle questionStyle = new Label.LabelStyle(questionFont, Color.WHITE);
        skin.add("question", questionStyle);

        Label.LabelStyle defaultStyle = new Label.LabelStyle(uiFont, Color.WHITE);
        skin.add("default", defaultStyle);

        // Button styles
        TextButton.TextButtonStyle submitButtonStyle = new TextButton.TextButtonStyle();
        submitButtonStyle.font = uiFont;
        submitButtonStyle.fontColor = Color.WHITE;
        submitButtonStyle.up = new TextureRegionDrawable(buttonTexture);
        submitButtonStyle.down = new TextureRegionDrawable(buttonPressedTexture);
        skin.add("submit", submitButtonStyle);

        TextButton.TextButtonStyle cancelButtonStyle = new TextButton.TextButtonStyle();
        cancelButtonStyle.font = uiFont;
        cancelButtonStyle.fontColor = Color.WHITE;
        cancelButtonStyle.up = new TextureRegionDrawable(cancelButtonTexture);
        cancelButtonStyle.down = new TextureRegionDrawable(cancelButtonPressedTexture);
        skin.add("cancel", cancelButtonStyle);

        // TextField style
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = uiFont;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = new TextureRegionDrawable(textFieldTexture);
        textFieldStyle.focusedBackground = new TextureRegionDrawable(textFieldFocusTexture);
        textFieldStyle.cursor = new TextureRegionDrawable(createColorTexture(2, 30, Color.WHITE));
        skin.add("default", textFieldStyle);

        return skin;
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
