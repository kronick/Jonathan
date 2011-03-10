import java.io.File;

import processing.core.*;
import com.sun.opengl.util.texture.*;
import javax.media.opengl.*;

public class Sprite {
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

		center = new PVector(icenter.x, icenter.y);

		try {
			tex = TextureIO.newTexture(new File(canvas.dataPath("art/256/" + filename)),true);
			//tex.setTexParameterf(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			//tex.setTexParameterf(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST);
			tex.setTexParameterf(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			tex.setTexParameterf(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
		    canvas.println("Texture created: " + filename);
		}
		catch(Exception e) {
			canvas.println("Couldn't create Texture object from file: " + e);
		}
	}

	public void draw(float size) {
		if(tex != null) {
			float e = 0.01f;
			//float w = size * (float)tex.getImageWidth()/256f + e;	// width
			float w = size + e;	// width
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

		}
	}
}
