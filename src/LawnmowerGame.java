import java.util.ArrayList;

import processing.core.*;
import processing.video.Movie;

import java.io.File;
import processing.opengl.*;
import javax.media.opengl.*;

import codeanticode.glgraphics.*;

public class LawnmowerGame extends PApplet {
	Grid grid;
	ArrayList<Sprite> sprites;

	Mower mowerMan;

	int lastRoadDirection;
	RoadSegment roadToFollow;
	boolean followRoad;
	boolean freePan = false;

	PVector mouseDown;

	PVector wind;

	GL gl;
	GLGraphics renderer;
	PGraphicsOpenGL pgl;

	Movie moviePlayer;

	int layerIndices[];
	int topLayer = 0;
	ArrayList<Layerable>[] layers;
	static final int GROUND_LAYER = 0;
	static final int ROAD_LAYER = 1;
	static final int DRIVEWAY_LAYER = 2;
	static final int PLAYER_LAYER = 3;
	static final int BUILDING_LAYER = 4;
	static final int SKY_LAYER = 5;

	int liveRoads = 0;

	public void setup() {
		//size(480, 320);
		size(800,600, GLConstants.GLGRAPHICS);
		//size(1280,800, GLConstants.GLGRAPHICS);
		//size(1280,800, OPENGL);
		//smooth();
		grid  = new Grid(this, 4);
		colorMode(HSB);
		rectMode(CENTER);
		imageMode(CENTER);
		frameRate(60);
		this.sprites = new ArrayList<Sprite>();

		mowerMan = new Mower(this);
		mowerMan.closestCell = grid.zones.get(0).cells[0][0].subCells[6][6];
		//mowerMan.position =
		mouseDown = new PVector(0,0);

		followRoad = false;

		this.wind = new PVector(2,-5);

		this.layers = new ArrayList[8];
		this.layerIndices = new int[layers.length];
		for(int i=0; i<this.layers.length; i++) {
			this.layers[i] = new ArrayList<Layerable>();
			layerIndices[i] = 0;
		}
		//this.pgl = (PGraphicsOpenGL) g;
	    //this.gl = pgl.gl;
	    //ortho(0,width,0,height,-100,100);

	}

	public void draw() {
		background(60, 160, 198);
		this.renderer = (GLGraphics)g;
		this.gl = renderer.beginGL();
		gl.setSwapInterval(1);

	    // Begin opengl blending

		//gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_BLEND);
	    gl.glEnable(GL.GL_BLEND);
	    gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA); // Activate the multiply effect

		//gl.glPushMatrix();
		//gl.glScalef(10, 10, 10);
		//mowerMan.draw();
		//gl.glPopMatrix();


		grid.update();
		grid.beginTransform();

		float[] ambient = {1,1,1,1f};
		float[] diffuse = {50f,50f,50f,1f};
		float[] position = {0,0,512,1};

		gl.glEnable(GL.GL_LIGHTING);
		gl.glLightfv( GL.GL_LIGHT1, GL.GL_AMBIENT, ambient,0);
		gl.glLightfv( GL.GL_LIGHT1, GL.GL_DIFFUSE, diffuse,0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, position,0);
		gl.glEnable(GL.GL_LIGHT1);

		gl.glShadeModel(GL.GL_FLAT);
		gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
		gl.glEnable(GL.GL_COLOR_MATERIAL);

		grid.draw();

		mowerMan.update();
		if(!freePan) {
			grid.centerTarget = mowerMan.position.get();
			grid.rotationTarget = -mowerMan.angle;
		}
		else grid.rotationTarget = 0;

		for(int i=0; i<layers.length; i++) {
			if(i==PLAYER_LAYER) mowerMan.draw();
			for(int j=0; j<layers[i].size(); j++) {
				layers[i].get(j).draw();
			}
		}


		//println("Currently at: " + mowerMan.closestCell.getCoordinate().x + ", " + mowerMan.closestCell.getCoordinate().y);

		grid.endTransform();

		if(roadToFollow != null) {
			if(frameCount % 10 == 0) {
				if(roadToFollow.numberOfConnections() == 1 && !roadToFollow.grow) {
					// Choose another road at random
					lastRoadDirection = (lastRoadDirection + 3)%6;
				}

				// Move along this segment
				int direction;
				ArrayList<Integer> availableDirections = new ArrayList<Integer>();
				for(int i=0; i<6; i++) {
					if(roadToFollow.connections[i] != null && i != (lastRoadDirection+3)%6)
						availableDirections.add(i);
				}
				if(availableDirections.size() > 0 && followRoad) {
					direction = availableDirections.get((int)random(availableDirections.size()));
					roadToFollow = roadToFollow.connections[direction];
					grid.centerTarget = roadToFollow.parent.getPosition();
					//grid.rotationTarget = -direction * PI/3;
					lastRoadDirection = direction;
				}
			}
		}
		else {
			roadToFollow = grid.zones.get(0).cells[0][0].getRoad();
		}
		renderer.endGL();

		if()

		for(int i=0; i<layers.length; i++) {
			this.layers[i].clear();
		}
		//this.topLayer = 0;

		// If all roads are dead, randomly find one and make it live again
		if(this.liveRoads == 0) {
			RoadSegment toMakeLive = null;
			while(toMakeLive == null) {
				Zone z = grid.zones.get((int)random(grid.zones.size()));
				SuperCell c = z.cells[(int)random(z.cells.length)][(int)random(z.cells[0].length)];
				if(c.hasRoad()) toMakeLive = c.getRoad();
			}
			toMakeLive.age = 0;
			toMakeLive.grow = true;

		}
		this.liveRoads = 0;

	}

	public static void main(String args[]) {
		PApplet.main(new String[] {"LawnmowerGame" });
	}

	public void keyPressed() {
		if(key == CODED) {
			switch (keyCode) {
				case UP:
					freePan = false;
					mowerMan.setSpeed(0.1f);
					//grid.center.add(new PVector(0,10/grid.zoom));
					break;
				case DOWN:
					freePan = false;
					mowerMan.setSpeed(-0.1f);
					//grid.center.add(new PVector(0,-10/grid.zoom));
					break;
				case LEFT:
					//grid.center.add(new PVector(10/grid.zoom,0));
					break;
				case RIGHT:
					//grid.center.add(new PVector(-10/grid.zoom,0));
					break;
			}
		}
		else {
			switch (key) {
			case 'r':
				freePan = true;
				followRoad = !followRoad;
				break;
			case 'w':
				freePan = false;
				mowerMan.setSpeed(0.1f);
				break;
			case 's':
				freePan = false;
				mowerMan.setSpeed(-0.1f);
				break;
			case 'a':
				freePan = false;
				mowerMan.turnLeft();
				break;
			case 'd':
				freePan = false;
				mowerMan.turnRight();
				break;
			case 'm':
				mowerMan.toggleMowing();
				break;
			case 't':
				println(mowerMan.direction%6);
				if(mowerMan.closestCell.getAdjacent(mowerMan.direction) != null) {
					Tipi t = new Tipi(mowerMan.closestCell.getAdjacent(mowerMan.direction), this, mowerMan.direction);
					//t.parent.addContent(t);
				}
			}
		}
	}

	public void keyReleased() {
		mowerMan.setSpeed(0);
	}

	public void mousePressed() {
		println(grid.screenToMap(new PVector(mouseX, mouseY)));
		this.mouseDown = new PVector(mouseX, mouseY);
	}
	public void mouseReleased() {
		mowerMan.setSpeed(0);
	}
	public void mouseDragged() {
		if(mouseButton == LEFT) {
			freePan = true;
			PVector dP = PVector.sub(new PVector(mouseX, mouseY), this.mouseDown);
			//mowerMan.setSpeed(-dP.y/1000f);
			/*
			if(dP.x > 50) {
				if(mowerMan.turnRight())
					mouseDown.x = mouseX;
			}
			if(dP.x < -50) {
				if(mowerMan.turnLeft())
					mouseDown.x = mouseX;
			}
			*/
			grid.centerTarget.add(new PVector((mouseX-pmouseX)/grid.zoom, (mouseY-pmouseY)/grid.zoom));
			//grid.centerTarget.add(dP);
		}
		else
			grid.setZoom(grid.zoom + (pmouseY - mouseY) * 0.05f);
			//grid.rotationTarget += (pmouseY - mouseY) * 0.05f;
	}

	Sprite lookupSprite(String filename) {
		// Find a Sprite object reference by its filename
		for(int i=0; i<sprites.size(); i++) {
			if(sprites.get(i).filename.equals(filename)) {
				return sprites.get(i);
			}
		}

		println("Couldn't find file... trying to load it now: " + filename);

		// If none exists, try loading it
		File f = new File("art/64/" + filename);
		if(f.exists() || true) {
			Sprite s = new Sprite(this, filename);
			sprites.add(s);
			if(s != null)
				return s;
			else {
				println("Can't make that sprite.");
				return null;
			}
		}
		else {
			println("can't find file: " + filename);
		}

		return null;
	}

	public int getNewLayer() {
		this.topLayer++;
		return this.topLayer;
	}

	public void insertSubLayer(int layer, Layerable l) {
		int insertAt = 0;
		for(int i=layers[layer].size()-1; i>=0;  i--) {
			if(l.getLayer() > layers[layer].get(i).getLayer()) {
				insertAt = i+1;
				break;
			}
		}
		layers[layer].add(insertAt, l);
	}

}

