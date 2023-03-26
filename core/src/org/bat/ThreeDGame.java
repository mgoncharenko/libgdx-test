package org.bat;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class ThreeDGame implements ApplicationListener {

  private PerspectiveCamera cam;
  private ModelBatch modelBatch;
  private Environment environment;

  private AssetManager assets;
  private Array<GameObject> instances = new Array<>();
  private Array<GameObject> blocks = new Array<>();
  private Array<GameObject> invaders = new Array<>();
  private GameObject ship;
  private GameObject space;
  private boolean loading;

  private CameraInputController camController;

  private Stage stage;
  private Label label;
  private BitmapFont font;
  private StringBuilder stringBuilder;

  private Vector3 position = new Vector3();

  private int visibleCount;

  @Override
  public void create() {
    stage = new Stage();
    font = new BitmapFont();
    label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
    stage.addActor(label);
    stringBuilder = new StringBuilder();

    environment = new Environment();
    environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
    environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));





    modelBatch = new ModelBatch();

    /*
    ModelBuilder modelBuilder = new ModelBuilder();
    model = modelBuilder.createBox(5f, 5f, 5f,
        new Material(ColorAttribute.createDiffuse(Color.GREEN)),
        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    instance = new ModelInstance(model);
    */

    /*
    ModelLoader loader = new ObjLoader();
    model = loader.loadModel(Gdx.files.internal("data/ship.obj"));
    instance = new ModelInstance(model);
    */

    assets = new AssetManager();
    assets.load("scene/invaderscene.g3db", Model.class);
    loading = true;

    cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.position.set(10f, 10f, 10f);
    cam.lookAt(0,0,0);
    cam.near = 0.01f;
    cam.far = 300f;
    cam.update();

    camController = new CameraInputController(cam);
    Gdx.input.setInputProcessor(camController);
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  private void doneLoading() {
    Model model = assets.get("scene/invaderscene.g3db", Model.class);
    for (int i = 0; i < model.nodes.size; i++) {
      String id = model.nodes.get(i).id;
      GameObject instance = new GameObject(model, id, true);

      if (id.equals("space")) {
        space = instance;
        continue;
      }

      instances.add(instance);

      if (id.equals("ship")) {
        ship = instance;
      } else if (id.startsWith("block")) {
        blocks.add(instance);
      } else if (id.startsWith("invader")) {
        invaders.add(instance);
      }
    }

    loading = false;

  }

  @Override
  public void render() {
    if (loading) {
      if (assets.update()) {
        doneLoading();
      } else {
        return;
      }
    }

    camController.update();

    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    modelBatch.begin(cam);
    visibleCount = 0;
    for (final GameObject instance : instances) {
      if (isVisible(cam, instance)) {
        modelBatch.render(instance, environment);
        visibleCount++;
      }
    }
    if (space != null) {
      modelBatch.render(space);
    }
    modelBatch.end();

    stringBuilder.setLength(0);
    stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
    stringBuilder.append(" Visible: ").append(visibleCount);
    label.setText(stringBuilder);
    stage.draw();
  }

  protected boolean isVisible(final Camera cam, final GameObject instance) {
    instance.transform.getTranslation(position);
    position.add(instance.center);
    return cam.frustum.sphereInFrustum(position, instance.radius);
  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void dispose() {
    modelBatch.dispose();
    instances.clear();
    assets.dispose();
    stage.dispose();
  }
}
