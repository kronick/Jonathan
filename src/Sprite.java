import java.io.File;

import processing.core.*;
import com.sun.opengl.util.texture.*;
import javax.media.opengl.*;

public class Sprite {
	PImage[] images;
	LawnmowerGame canvas;
	String filename;
	PVector center;

	Texture tex;

	public Sprite(LawnmowerGame canvas, String filename) {
		this(canvas, filename, new PVector(0,0));
	}
	public Sprite(LawnmowerGame canvas, String filename, PVector icenter) {
		this.filename = filename;
		this.canvas = canvas;
		images = new PImage[4];
		center = new PVector(icenter.x, icenter.y);
		// Load all by default
		//images[0] = canvas.requestImage("art/32/" + filename);
		//images[1] = canvas.requestImage("art/64/" + filename);
		//images[2] = canvas.requestImage("art/128/" + filename);
		//images[3] = canvas.requestImage("art/256/" + filename);

		try {
			tex = TextureIO.newTexture(new File(canvas.dataPath("art/256/" + filename)),true);
		    //tex.setTexParameteri(GL.GL_TEXTURE_WRAP_R,GL.GL_REPEAT);
		    //tex.setTexParameteri(GL.GL_TEXTURE_WRAP_S,GL.GL_REPEAT);
		    //tex.setTexParameteri(GL.GL_TEXTURE_WRAP_T,GL.GL_REPEAT);
		    canvas.println("Texture created: " + filename);
		}
		catch(Exception e) {
			canvas.println("Couldn't create Texture object from file: " + e);
		}
	}

	public void draw(float size) {
		if(canvas.grid.zoom > 20)
			this.draw(size, 3);
		else
			this.draw(size, 2);
	}
	public void draw(float size, int level) {
		if(tex != null) {
			float e = 0.5f;
			float w = size * (float)tex.getImageWidth()/256f + e;	// width
			float h = w / tex.getAspectRatio();						// height
			float x = this.center.x*w/(float)tex.getImageWidth();	// center x
			float y = this.center.y*w/(float)tex.getImageWidth();	// center y

			canvas.gl.glColor3f(1, 1, 1);
			this.tex.bind();
			this.tex.enable();
			canvas.gl.glBegin(GL.GL_QUADS);
			canvas.gl.glNormal3f(0,0,-1.0f);
			canvas.gl.glTexCoord2f(0,0);
			canvas.gl.glVertex2f(x-w/2f, y-h/2f);	// top left
			canvas.gl.glTexCoord2f(1.0f,0);
			canvas.gl.glVertex2f(x+w/2f, y-h/2f);	// top right
			canvas.gl.glTexCoord2f(1.0f,1.0f);
			canvas.gl.glVertex2f(x+w/2f, y+h/2f);	// bottom right
			canvas.gl.glTexCoord2f(0,1.0f);
			canvas.gl.glVertex2f(x-w/2f, y+h/2f);	// bottom left
			canvas.gl.glEnd();
			this.tex.disable();


			//canvas.image(images[level], (center.x*(s)/(float)images[3].width),(center.y*(s)/(float)images[3].width),
			//			 s, images[level].height/(float)images[level].width*(s));
		}
	}
}
