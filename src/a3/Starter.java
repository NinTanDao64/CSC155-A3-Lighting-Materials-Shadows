package a3;

import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.light.PositionalLight;
import graphicslib3D.shape.Sphere;

import java.io.File;
import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;

public class Starter extends JFrame implements GLEventListener
{	
	private GLCanvas myCanvas;
	private int rendering_program, shadow_rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[23];
	private float cameraX, cameraY, cameraZ;
	private float cubeLocX, cubeLocY, cubeLocZ;
	private float pyrLocX, pyrLocY, pyrLocZ;
	private GLSLUtils util = new GLSLUtils();
	private float aspect;
	private Camera camera = new Camera(0, 0, 12);
	private boolean drawAxes = true;
	
	private int counter = 0;
	
	private Material planet1_material = Material.SILVER;
	private Material planet2_material = Material.GOLD;
	private Material bomb_material = Material.BRONZE;
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	private	MatrixStack mvStack = new MatrixStack(20);
	private Sphere sun = new Sphere(24);
	private Sphere planet1 = new Sphere(24);
	private Sphere planet2 = new Sphere(24);
	
	//private ImportedModel bomb = new ImportedModel("Bomb.obj");
	
	//Light stuff
	private PositionalLight currentLight = new PositionalLight();
	private float lightX = 1.0f;
	private float lightY = -2.0f;
	private float lightZ = 1.0f;
	private Point3D lightLoc = new Point3D(lightX, lightY, lightZ);
	private float[] globalAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	
	//Shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();
	
	private int diamondTexture;
	private Texture joglDiamondTexture;
	
	private int sunTexture;
	private Texture joglSunTexture;
	
	private int planetTexture1, planet1_normals;
	private Texture joglPlanetTexture1, joglPlanetNormals1;
	
	private int planetTexture2, planet2_normals;
	private Texture joglPlanetTexture2, joglPlanetNormals2;
	
	private int redTexture;
	private Texture joglRedTexture;
	
	private int greenTexture;
	private Texture joglGreenTexture;
	
	private int blueTexture;
	private Texture joglBlueTexture;

	public Starter()
	{	setTitle("CSC155 - A2");
		setSize(1000, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		this.setVisible(true);
		
		JPanel contentPane = (JPanel) this.getContentPane();
		
		AbstractAction move_forward = new MoveForward(camera);
		AbstractAction move_backward = new MoveBackward(camera);
		AbstractAction move_left = new MoveLeft(camera);
		AbstractAction move_right = new MoveRight(camera);
		AbstractAction move_up = new MoveUp(camera);
		AbstractAction move_down = new MoveDown(camera);
		AbstractAction toggle_axes = new ToggleAxes(this);
		AbstractAction pan_left = new YawLeft(camera);
		AbstractAction pan_right = new YawRight(camera);
		AbstractAction look_up = new PitchUp(camera);
		AbstractAction look_down = new PitchDown(camera);
		AbstractAction light_move_left = new LightMoveLeft(this);
		AbstractAction light_move_right = new LightMoveRight(this);
		AbstractAction light_move_up = new LightMoveUp(this);
		AbstractAction light_move_down = new LightMoveDown(this);
		AbstractAction light_move_forward = new LightMoveForward(this);
		AbstractAction light_move_backward = new LightMoveBackward(this);

		int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
		InputMap imap = contentPane.getInputMap(mapName);
		ActionMap amap = contentPane.getActionMap();
		
		KeyStroke wKey = KeyStroke.getKeyStroke('w');
		imap.put(wKey, "Forward");
		amap.put("Forward", move_forward);
		
		KeyStroke sKey = KeyStroke.getKeyStroke('s');
		imap.put(sKey, "Backward");
		amap.put("Backward", move_backward);
		
		KeyStroke aKey = KeyStroke.getKeyStroke('a');
		imap.put(aKey, "Left");
		amap.put("Left", move_left);
		
		KeyStroke dKey = KeyStroke.getKeyStroke('d');
		imap.put(dKey, "Right");
		amap.put("Right", move_right);
		
		KeyStroke qKey = KeyStroke.getKeyStroke('q');
		imap.put(qKey, "Up");
		amap.put("Up", move_up);
		
		KeyStroke eKey = KeyStroke.getKeyStroke('e');
		imap.put(eKey, "Down");
		amap.put("Down", move_down);
		
		KeyStroke spaceKey = KeyStroke.getKeyStroke("SPACE");
		imap.put(spaceKey, "Axes");
		amap.put("Axes", toggle_axes);
		
		KeyStroke leftKey = KeyStroke.getKeyStroke("LEFT");
		imap.put(leftKey, "Pan Left");
		amap.put("Pan Left", pan_left);
		
		KeyStroke rightKey = KeyStroke.getKeyStroke("RIGHT");
		imap.put(rightKey, "Pan Right");
		amap.put("Pan Right", pan_right);
		
		KeyStroke upKey = KeyStroke.getKeyStroke("UP");
		imap.put(upKey, "Look Up");
		amap.put("Look Up", look_up);
		
		KeyStroke downKey = KeyStroke.getKeyStroke("DOWN");
		imap.put(downKey, "Look Down");
		amap.put("Look Down", look_down);
		
		KeyStroke jKey = KeyStroke.getKeyStroke('j');
		imap.put(jKey, "Light Left");
		amap.put("Light Left", light_move_left);
		
		KeyStroke lKey = KeyStroke.getKeyStroke('l');
		imap.put(lKey, "Light Right");
		amap.put("Light Right", light_move_right);
		
		KeyStroke iKey = KeyStroke.getKeyStroke('i');
		imap.put(iKey, "Light Up");
		amap.put("Light Up", light_move_up);

		KeyStroke pKey = KeyStroke.getKeyStroke('p');
		imap.put(pKey, "Light Down");
		amap.put("Light Down", light_move_down);
		
		KeyStroke oKey = KeyStroke.getKeyStroke('o');
		imap.put(oKey, "Light Forward");
		amap.put("Light Forward", light_move_forward);
		
		KeyStroke kKey = KeyStroke.getKeyStroke('k');
		imap.put(kKey, "Light Backward");
		amap.put("Light Backward", light_move_backward);
		
		FPSAnimator animator = new FPSAnimator(myCanvas, 50);
		animator.start();
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		counter++;
		if(counter >= 100) {
			System.out.println("Light source coordinates: x = " + lightLoc.getX() + ", y = " + lightLoc.getY() + ", z = " + lightLoc.getZ());
			counter = 0;
		}
		
		lightLoc.setX(lightX);
		lightLoc.setY(lightY);
		lightLoc.setZ(lightZ);
		currentLight.setPosition(lightLoc);
		
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts
		
		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
	}
	
	public void passOne() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glUseProgram(shadow_rendering_program);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		//Draw planet1
		m_matrix.setToIdentity();
		m_matrix.translate(0, 0, 0);
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		int shadow_location = gl.glGetUniformLocation(shadow_rendering_program, "shadowMVP");
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up planet1 vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		int planet1_numVerts = planet1.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, planet1_numVerts);
		
		//Draw planet2
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(-2.5, 1, -2.5);

		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		int planet2_numVerts = planet2.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, planet2_numVerts);
		
		/*
		//Draw bomb
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(2.5, 1.5, -2.5);

		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, bomb.getNumVertices());*/
	}
	
	public void passTwo() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glUseProgram(rendering_program);
		
		int mv_location = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		int proj_location = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		int n_location = gl.glGetUniformLocation(rendering_program, "normalMat");
		int shadow_location = gl.glGetUniformLocation(rendering_program, "shadowMVP");
		
		//Draw the lines for x,y,z Axes
		if(drawAxes) {
			m_matrix.setToIdentity();
			m_matrix.translate(0, 0, 0);
			
			v_matrix.setToIdentity();
			v_matrix.concatenate(camera.getView());
			
			currentLight.setPosition(lightLoc);
			
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);
			
			gl.glDisable(GL_CULL_FACE);
			
			//Red x-axis
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
		
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
		
			gl.glActiveTexture(GL_TEXTURE1);
			gl.glBindTexture(GL_TEXTURE_2D, redTexture);
		
			gl.glDrawArrays(GL_TRIANGLES, 0, 9);
			
			//Green y-axis
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
		
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
		
			gl.glActiveTexture(GL_TEXTURE1);
			gl.glBindTexture(GL_TEXTURE_2D, greenTexture);
		
			gl.glDrawArrays(GL_TRIANGLES, 0, 9);
			
			//Blue z-axis
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
		
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);
		
			gl.glActiveTexture(GL_TEXTURE1);
			gl.glBindTexture(GL_TEXTURE_2D, blueTexture);
		
			gl.glDrawArrays(GL_TRIANGLES, 0, 9);
		}
		
		//Draw planet1
		m_matrix.setToIdentity();
		m_matrix.translate(0, 0, 0);
		
		//  build the VIEW matrix
		v_matrix.setToIdentity();
		v_matrix.concatenate(camera.getView());
		
		installLights(v_matrix, planet1_material);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up torus normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, planet1_normals); // normal

		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, planetTexture1); // texture

		int planet1_numVerts = planet1.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, planet1_numVerts);
		
		//Draw planet2	
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(-2.5, 1, -2.5);
		
		installLights(v_matrix, planet2_material);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, planet2_normals); // normal

		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, planetTexture2); // texture

		int planet2_numVerts = planet2.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, planet2_numVerts);
		
		/*
		//Draw bomb	
		installLights(v_matrix, bomb_material);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(2.5, 1.5, -2.5);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, bomb.getNumVertices());
		*/
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		createShaderPrograms();
		setupVertices();
		setupShadowBuffers();
		
		b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		cameraX = 0.0f; cameraY = 0.0f; cameraZ = 12.0f;
		cubeLocX = 0.0f; cubeLocY = 0.0f; cubeLocZ = 0.0f;
		pyrLocX = 0.0f; pyrLocY = 0.0f; pyrLocZ = 0.0f;
		
		joglDiamondTexture = loadTexture("Diamond Texture.jpg");
		diamondTexture = joglDiamondTexture.getTextureObject();
		
		joglSunTexture = loadTexture("Trask.png");
		sunTexture = joglSunTexture.getTextureObject();
		
		joglPlanetTexture1 = loadTexture("Serendip.jpg");
		planetTexture1 = joglPlanetTexture1.getTextureObject();
		
		// apply mipmapping and anisotropic filtering to planet1 surface texture
		gl.glBindTexture(GL_TEXTURE_2D, planetTexture1);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
		
		joglPlanetNormals1 = loadTexture("Serendip_normals.jpg"); //Planet1 normals
		planet1_normals = joglPlanetNormals1.getTextureObject();
		
		// apply mipmapping and anisotropic filtering to planet1 normal texture
		gl.glBindTexture(GL_TEXTURE_2D, planet1_normals);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
		
		joglPlanetTexture2 = loadTexture("Telos.png");
		planetTexture2 = joglPlanetTexture2.getTextureObject();
		
		// apply mipmapping and anisotropic filtering to planet2 surface texture
		gl.glBindTexture(GL_TEXTURE_2D, planetTexture2);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
		
		joglPlanetNormals2 = loadTexture("Telos_normals.jpg"); //Planet1 normals
		planet2_normals = joglPlanetNormals1.getTextureObject();
		
		// apply mipmapping and anisotropic filtering to planet1 normal texture
		gl.glBindTexture(GL_TEXTURE_2D, planet2_normals);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
		
		joglRedTexture = loadTexture("Red.jpg");
		redTexture = joglRedTexture.getTextureObject();
		
		joglGreenTexture = loadTexture("Green.jpg");
		greenTexture = joglGreenTexture.getTextureObject();
		
		joglBlueTexture = loadTexture("Blue.jpg");
		blueTexture = joglBlueTexture.getTextureObject();
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		float[] diamond_positions =
			{ -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 2.5f, 0.0f,
			  1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 0.0f, 2.5f, 0.0f,
			  1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, 0.0f, 2.5f, 0.0f,
			  -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 2.5f, 0.0f,
			  0.0f, -2.5f, 0.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f,
			  0.0f, -2.5f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f,
			  0.0f, -2.5f, 0.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f,
			  0.0f, -2.5f, 0.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, -1.0f
			};
		
		float[] diamond_texCoords =
			{ 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			  0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			  0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			  0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			  0.05f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			  0.05f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			  0.05f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			  0.05f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			};
		
		float[] xLine_positions =
			{
				0.0f, 0.0f, 0.0f, 100.0f, 0.0f, 0.0f, 0.0f, 0.05f, 0.0f	
			};
		
		float[] red_texCoords =
			{
				0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f	
			};
		
		float[] yLine_positions =
			{
				0.0f, 0.0f, 0.0f, 0.05f, 0.0f, 0.0f, 0.0f, 100.0f, 0.0f
			};
		
		float[] green_texCoords =
			{
				0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f	
			};
		
		float[] zLine_positions =
			{
				0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 100.0f, 0.0f, -0.05f, 0.0f
			};
		
		float[] blue_texCoords =
			{
				0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f	
			};
		
		Vertex3D[] sun_vertices = sun.getVertices();
		int[] sun_indices = sun.getIndices();
		
		float[] sun_pvalues = new float[sun_indices.length*3];
		float[] sun_tvalues = new float[sun_indices.length*2];
		float[] sun_nvalues = new float[sun_indices.length*3];
		
		for (int i=0; i<sun_indices.length; i++)
		{	sun_pvalues[i*3] = (float) (sun_vertices[sun_indices[i]]).getX();
			sun_pvalues[i*3+1] = (float) (sun_vertices[sun_indices[i]]).getY();
			sun_pvalues[i*3+2] = (float) (sun_vertices[sun_indices[i]]).getZ();
			sun_tvalues[i*2] = (float) (sun_vertices[sun_indices[i]]).getS();
			sun_tvalues[i*2+1] = (float) (sun_vertices[sun_indices[i]]).getT();
			sun_nvalues[i*3] = (float) (sun_vertices[sun_indices[i]]).getNormalX();
			sun_nvalues[i*3+1]= (float)(sun_vertices[sun_indices[i]]).getNormalY();
			sun_nvalues[i*3+2]=(float) (sun_vertices[sun_indices[i]]).getNormalZ();
		}
		
		Vertex3D[] planet1_vertices = planet1.getVertices();
		int[] planet1_indices = planet1.getIndices();
		
		float[] planet1_pvalues = new float[planet1_indices.length*3];
		float[] planet1_tvalues = new float[planet1_indices.length*2];
		float[] planet1_nvalues = new float[planet1_indices.length*3];
		float[] planet1_TANvalues = new float[planet1_indices.length*3];
		
		for (int i=0; i<planet1_indices.length; i++)
		{	planet1_pvalues[i*3] = (float) (planet1_vertices[planet1_indices[i]]).getX();
			planet1_pvalues[i*3+1] = (float) (planet1_vertices[planet1_indices[i]]).getY();
			planet1_pvalues[i*3+2] = (float) (planet1_vertices[planet1_indices[i]]).getZ();
			planet1_tvalues[i*2] = (float) (planet1_vertices[planet1_indices[i]]).getS();
			planet1_tvalues[i*2+1] = (float) (planet1_vertices[planet1_indices[i]]).getT();
			planet1_nvalues[i*3] = (float) (planet1_vertices[planet1_indices[i]]).getNormalX();
			planet1_nvalues[i*3+1]= (float)(planet1_vertices[planet1_indices[i]]).getNormalY();
			planet1_nvalues[i*3+2]=(float) (planet1_vertices[planet1_indices[i]]).getNormalZ();
			
			planet1_TANvalues[i*3] = (float) (planet1_vertices[planet1_indices[i]]).getTangent().getX();
			planet1_TANvalues[i*3+1] = (float) (planet1_vertices[planet1_indices[i]]).getTangent().getY();
			planet1_TANvalues[i*3+2] = (float) (planet1_vertices[planet1_indices[i]]).getTangent().getZ();
		}
		
		Vertex3D[] planet2_vertices = planet2.getVertices();
		int[] planet2_indices = planet2.getIndices();
		
		float[] planet2_pvalues = new float[planet2_indices.length*3];
		float[] planet2_tvalues = new float[planet2_indices.length*2];
		float[] planet2_nvalues = new float[planet2_indices.length*3];
		float[] planet2_TANvalues = new float[planet2_indices.length*3];
		
		for (int i=0; i<planet2_indices.length; i++)
		{	planet2_pvalues[i*3] = (float) (planet2_vertices[planet2_indices[i]]).getX();
			planet2_pvalues[i*3+1] = (float) (planet2_vertices[planet2_indices[i]]).getY();
			planet2_pvalues[i*3+2] = (float) (planet2_vertices[planet2_indices[i]]).getZ();
			planet2_tvalues[i*2] = (float) (planet2_vertices[planet2_indices[i]]).getS();
			planet2_tvalues[i*2+1] = (float) (planet2_vertices[planet2_indices[i]]).getT();
			planet2_nvalues[i*3] = (float) (planet2_vertices[planet2_indices[i]]).getNormalX();
			planet2_nvalues[i*3+1]= (float)(planet2_vertices[planet2_indices[i]]).getNormalY();
			planet2_nvalues[i*3+2]=(float) (planet2_vertices[planet2_indices[i]]).getNormalZ();
			
			planet2_TANvalues[i*3] = (float) (planet2_vertices[planet2_indices[i]]).getTangent().getX();
			planet2_TANvalues[i*3+1] = (float) (planet2_vertices[planet2_indices[i]]).getTangent().getY();
			planet2_TANvalues[i*3+2] = (float) (planet2_vertices[planet2_indices[i]]).getTangent().getZ();
		}
		
		/*
		//Bomb definition
		Vertex3D[] bomb_vertices = bomb.getVertices();
		int numBombVertices = bomb.getNumVertices();

		float[] bomb_vertex_positions = new float[numBombVertices*3];
		float[] bomb_normals = new float[numBombVertices*3];
		float[] bomb_TANvalues = new float[numBombVertices*3];

		for (int i=0; i<numBombVertices; i++)
		{	bomb_vertex_positions[i*3]   = (float) (bomb_vertices[i]).getX();			
			bomb_vertex_positions[i*3+1] = (float) (bomb_vertices[i]).getY();
			bomb_vertex_positions[i*3+2] = (float) (bomb_vertices[i]).getZ();
			
			bomb_normals[i*3]   = (float) (bomb_vertices[i]).getNormalX();
			bomb_normals[i*3+1] = (float) (bomb_vertices[i]).getNormalY();
			bomb_normals[i*3+2] = (float) (bomb_vertices[i]).getNormalZ();
			
			bomb_TANvalues[i*3]   = (float) (bomb_vertices[i]).getTangent().getX();
			bomb_TANvalues[i*3+1] = (float) (bomb_vertices[i]).getTangent().getY();
			bomb_TANvalues[i*3+2] = (float) (bomb_vertices[i]).getTangent().getZ();
		}*/

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		//-----------  VBO's for vertices  -----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer sun_pBuffer = Buffers.newDirectFloatBuffer(sun_pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sun_pBuffer.limit()*4, sun_pBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer planet1_pBuffer = Buffers.newDirectFloatBuffer(planet1_pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, planet1_pBuffer.limit()*4, planet1_pBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[2]);
		FloatBuffer planet2_pBuffer = Buffers.newDirectFloatBuffer(planet2_pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, planet2_pBuffer.limit()*4, planet2_pBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer diamond_pBuffer = Buffers.newDirectFloatBuffer(diamond_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, diamond_pBuffer.limit()*4, diamond_pBuffer, GL_STATIC_DRAW);
		
		//----------- VBO's for textures -----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer sun_texBuffer = Buffers.newDirectFloatBuffer(sun_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sun_texBuffer.limit()*4, sun_texBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer planet1_texBuffer = Buffers.newDirectFloatBuffer(planet1_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, planet1_texBuffer.limit()*4, planet1_texBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer planet2_texBuffer = Buffers.newDirectFloatBuffer(planet2_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, planet2_texBuffer.limit()*4, planet2_texBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer diamond_texBuffer = Buffers.newDirectFloatBuffer(diamond_texCoords);
		gl.glBufferData(GL_ARRAY_BUFFER, diamond_texBuffer.limit()*4, diamond_texBuffer, GL_STATIC_DRAW);
		
		//----------- VBO's for normals -----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer sun_normBuffer = Buffers.newDirectFloatBuffer(sun_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sun_normBuffer.limit()*4, sun_normBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer planet1_normBuffer = Buffers.newDirectFloatBuffer(planet1_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, planet1_normBuffer.limit()*4, planet1_normBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer planet2_normBuffer = Buffers.newDirectFloatBuffer(planet2_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, planet2_normBuffer.limit()*4, planet2_normBuffer, GL_STATIC_DRAW);
		
		//VBO's for x,y,z colroed axes (positions + texture coordinates)
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer xLine_pBuffer = Buffers.newDirectFloatBuffer(xLine_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, xLine_pBuffer.limit()*4, xLine_pBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer red_texBuffer = Buffers.newDirectFloatBuffer(red_texCoords);
		gl.glBufferData(GL_ARRAY_BUFFER, red_texBuffer.limit()*4, red_texBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer yLine_pBuffer = Buffers.newDirectFloatBuffer(yLine_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, yLine_pBuffer.limit()*4, yLine_pBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer green_texBuffer = Buffers.newDirectFloatBuffer(green_texCoords);
		gl.glBufferData(GL_ARRAY_BUFFER, green_texBuffer.limit()*4, green_texBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		FloatBuffer zLine_pBuffer = Buffers.newDirectFloatBuffer(zLine_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, zLine_pBuffer.limit()*4, zLine_pBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		FloatBuffer blue_texBuffer = Buffers.newDirectFloatBuffer(blue_texCoords);
		gl.glBufferData(GL_ARRAY_BUFFER, blue_texBuffer.limit()*4, blue_texBuffer, GL_STATIC_DRAW);
		
		//----------- VBO's for tangents -----------
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		FloatBuffer planet1_tanBuffer = Buffers.newDirectFloatBuffer(planet1_TANvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, planet1_tanBuffer.limit()*4, planet1_tanBuffer, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
		FloatBuffer planet2_tanBuffer = Buffers.newDirectFloatBuffer(planet2_TANvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, planet2_tanBuffer.limit()*4, planet2_tanBuffer, GL_STATIC_DRAW);
		
		//----------- VBO's for Bomb -----------
		/*
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		FloatBuffer bombVertBuf = Buffers.newDirectFloatBuffer(bomb_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, bombVertBuf.limit()*4, bombVertBuf, GL_STATIC_DRAW);
		
		// load the pyramid normal coordinates into the fourth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		FloatBuffer bombNorBuf = Buffers.newDirectFloatBuffer(bomb_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, bombNorBuf.limit()*4, bombNorBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
		FloatBuffer bomb_tanBuffer = Buffers.newDirectFloatBuffer(bomb_TANvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, bomb_tanBuffer.limit()*4, bomb_tanBuffer, GL_STATIC_DRAW);
		*/
	}
	
	public void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
	}
	
	private void installLights(Matrix3D v_matrix, Material thisMat)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = thisMat;
		
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		float [] currLightPos = new float[] { (float) lightPv.getX(), (float) lightPv.getY(), (float) lightPv.getZ() };

		// set the current globalAmbient settings
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);
	
		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");
		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}
	
	public void toggleAxes() {
		drawAxes = !drawAxes;
	}

	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		return r;
	}

	public static void main(String[] args) { new Starter(); }
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		setupShadowBuffers();
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		String shadow_vshaderSource[] = util.readShaderSource("a3/shadowVert.shader");
		String vshaderSource[] = util.readShaderSource("a3/vert.shader");
		String fshaderSource[] = util.readShaderSource("a3/frag.shader");
		//String vshaderSource[] = util.readShaderSource("C:" + File.separator + "Users" +  File.separator + "ninta" + File.separator + 
			//"workspace" + File.separator + "CSC 155 - A2" + File.separator + "src" + File.separator + "a2" + File.separator + "vert.shader");
		//String fshaderSource[] = util.readShaderSource("C:" + File.separator + "Users" +  File.separator + "ninta" + File.separator + 
			//"workspace" + File.separator + "CSC 155 - A2" + File.separator + "src" + File.separator + "a2" + File.separator + "frag.shader");

		int shadow_vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(shadow_vShader, shadow_vshaderSource.length, shadow_vshaderSource, null, 0);
		gl.glShaderSource(vertexShader2, vshaderSource.length, vshaderSource, null, 0);
		gl.glShaderSource(fragmentShader2, fshaderSource.length, fshaderSource, null, 0);

		gl.glCompileShader(shadow_vShader);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);

		shadow_rendering_program = gl.glCreateProgram();
		rendering_program = gl.glCreateProgram();

		gl.glAttachShader(shadow_rendering_program, shadow_vShader);
		gl.glAttachShader(rendering_program, vertexShader2);
		gl.glAttachShader(rendering_program, fragmentShader2);

		gl.glLinkProgram(shadow_rendering_program);
		gl.glLinkProgram(rendering_program);
	}
	
	public Texture loadTexture(String textureFileName)
	{	Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
	
	private Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y)
	{	Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}
	
	public void lightMoveLeft() {
		lightX = lightX - 0.125f;
	}
	
	public void lightMoveRight() {
		lightX = lightX + 0.125f;
	}
	
	public void lightMoveUp() {
		lightY = lightY + 0.125f;
	}
	
	public void lightMoveDown() {
		lightY = lightY - 0.125f;
	}
	
	public void lightMoveForward() {
		lightZ = lightZ - 0.125f;
	}
	
	public void lightMoveBackward() {
		lightZ = lightZ + 0.125f;
	}
}
