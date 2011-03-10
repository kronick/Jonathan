import processing.core.*;
import java.util.*;

public class Grid {
	float rotation;
	float rotationTarget;
	float scale;
	PVector center;
	PVector centerTarget;

	float zoom;
	LawnmowerGame canvas;

	int zoneSize = 5;
	ArrayList<Zone> zones;
	Zone centerZone;

	ArrayList<SuperCell> cells;

	private float[] clipLines;
	static final int CLIP_LEFT = 0;
	static final int CLIP_RIGHT = 1;
	static final int CLIP_TOP = 2;
	static final int CLIP_BOTTOM = 3;

	public Grid(LawnmowerGame canvas, float scale) {
		this.center = new PVector(0,0);
		this.centerTarget = new PVector(0,0);
		this.scale = scale;
		this.canvas = canvas;
		this.zones = new ArrayList<Zone>();
		this.zoom = 10f;

		zones.add(new Zone(canvas, this, zoneSize));
		zones.add(new Zone(canvas, this, zoneSize));
		zones.add(new Zone(canvas, this, zoneSize));
		zones.add(new Zone(canvas, this, zoneSize));
		zones.add(new Zone(canvas, this, zoneSize));

		zones.get(0).center = new PVector(0,0);
		zones.get(0).makeNeighbor(Zone.NEIGHBOR_TOP, zones.get(1));
		zones.get(0).makeNeighbor(Zone.NEIGHBOR_RIGHT, zones.get(2));
		zones.get(0).makeNeighbor(Zone.NEIGHBOR_BOTTOM, zones.get(3));
		zones.get(0).makeNeighbor(Zone.NEIGHBOR_LEFT, zones.get(4));

		RoadSegment seed = new RoadSegment(zones.get(0).cells[0][0], canvas);
		zones.get(0).cells[0][0].addContent(seed);

		centerZone = zones.get(0);

		clipLines = new float[4];
	}

	void update() {
		// Ease into position
		PVector dP = PVector.sub(this.centerTarget, this.center);
		dP.mult(.1f);
		this.center = PVector.add(dP, this.center);

		float rotateDiff = (this.rotationTarget - this.rotation);
		while(rotateDiff > Math.PI) rotateDiff -= 2*Math.PI;
		while(rotateDiff < -Math.PI) rotateDiff += 2*Math.PI;
		float dTheta = rotateDiff * 0.1f;
		this.rotation += dTheta;

		PVector tl = screenToMap(new PVector(0,0));
		PVector br = screenToMap(new PVector(canvas.width, canvas.height));
		clipLines[CLIP_LEFT] = tl.x;
		clipLines[CLIP_RIGHT] = br.x;
		clipLines[CLIP_TOP] = tl.y;
		clipLines[CLIP_BOTTOM] = br.y;

		int closestZone = -1;
		float closestDist = 9999;
		PVector screenCenter = screenToMap(new PVector(canvas.width/2f, canvas.height/2f));
		//canvas.(screenCenter);
		for(int i=0; i<zones.size(); i++) {
			zones.get(i).updatedThisFrame = false;
			if(zones.get(i).center.dist(screenCenter) < closestDist) {
				closestZone = i;
				closestDist = zones.get(i).center.dist(screenCenter);
			}
		}
		//canvas.println(closestZone);

		centerZone = zones.get(closestZone);
		//centerZone = zones.get(0);
		centerZone.drawnThisFrame = false;
		centerZone.updatedThisFrame = false;
		centerZone.update();
	}

	void beginTransform() {
		canvas.pushMatrix();
		canvas.translate(canvas.width/2f, canvas.height/2f);
		canvas.rotate(rotation);
		canvas.scale(zoom);
		canvas.translate(center.x,center.y);
	}
	void endTransform() {
		canvas.popMatrix();
	}
  void draw() {
    centerZone.draw();
  }

  PVector screenToMap(PVector screen) {
    PVector out = PVector.sub(screen, new PVector(canvas.width/2f, canvas.height/2f));
    out = new PVector(out.x * PApplet.cos(-this.rotation) - out.y * PApplet.sin(-this.rotation),
			  out.x * PApplet.sin(-this.rotation) + out.y * PApplet.cos(-this.rotation));
    out.div(zoom);
    out.sub(center);
    return out;
  }

  PVector mapToScreen(PVector map) {
	    PVector out = PVector.add(map, center);
	    out.mult(zoom);
	    out = new PVector(out.x * PApplet.cos(this.rotation) - out.y * PApplet.sin(this.rotation),
				  out.x * PApplet.sin(this.rotation) + out.y * PApplet.cos(this.rotation));
	    out.add(new PVector(canvas.width/2f, canvas.height/2f));
	    return out;
  }
  /*
  Cell getNearestCell(PVector p) {
	  int closestZone = -1;
	  float closestDist = 9999;
	  //canvas.(screenCenter);
	  for(int i=0; i<zones.size(); i++) {
		  zones.get(i).updatedThisFrame = false;
		  if(zones.get(i).center.dist(screenCenter) < closestDist) {
			  closestZone = i;
			  closestDist = zones.get(i).center.dist(screenCenter);
		  }
	  }
  }
  */
  PVector cellOffset(PVector p) {
	  PVector out = new PVector(p.x, p.y);
	  int closestX = (int)(2/3f * p.x/scale);
	  int closestY = (int)((p.y/scale*2/Math.sqrt(3)-closestX)/2f + 0.5f);
	  PVector closestCell = new PVector(scale * 1.5f*closestX, scale*(float)Math.sqrt(3)*.5f*(closestX + 2 * closestY));
	  out.sub(closestCell);

	  /*
	  while(out.x >= scale) out.x -= scale;
	  while(out.y >= scale * Math.sqrt(3)*0.5) out.y -= scale * Math.sqrt(3)*0.5;
	  while(out.x <= -scale) out.x += scale;
	  while(out.y <= -scale * Math.sqrt(3)*0.5) out.y += scale * Math.sqrt(3)*0.5;
	   */
	  //canvas.println(out);
	  return out;
  }
  boolean pointIsVisible(PVector p) {
	  PVector screen = mapToScreen(p);
	  return (screen.x > 0 && screen.x < canvas.width && screen.y > 0 && screen.y < canvas.height);
  }
  boolean zoneIsVisible(Zone z) {
	  int buffer = 400;
	  PVector tl = mapToScreen(PVector.add(z.center, new PVector(-scale,-scale)));
	  PVector tr = mapToScreen(PVector.add(z.center, new PVector(z.width,-scale)));
	  PVector bl = mapToScreen(PVector.add(z.center, new PVector(-scale,z.height*1.5f)));
	  PVector br = mapToScreen(PVector.add(z.center, new PVector(z.width,z.height*1.5f)));
	  return !(tl.x < -buffer && tr.x < -buffer && bl.x < -buffer && br.x < -buffer) &&
	  		 !(tl.x > canvas.width+buffer && tr.x > canvas.width+buffer && bl.x > canvas.width+buffer && br.x > canvas.width+buffer) &&
	  		 !(tl.y < -buffer && tr.y < -buffer && bl.y < -buffer && br.y < -buffer) &&
	  		!(tl.y > canvas.height+buffer && tr.y > canvas.height+buffer && bl.y > canvas.height+buffer && br.y > canvas.height+buffer);


  }

  	Zone addZone() {
  		zones.add(new Zone(canvas, this, zoneSize));
  		return zones.get(zones.size() - 1);
  	}

  	void setZoom(float level) {
  		if(level > 8 && level < 60)
  			zoom = level;
  	}
}
