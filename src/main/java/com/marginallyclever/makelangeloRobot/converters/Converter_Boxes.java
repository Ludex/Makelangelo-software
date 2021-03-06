package com.marginallyclever.makelangeloRobot.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;


public class Converter_Boxes extends ImageConverter {
	public static int boxMaxSize=4; // 0.8*5
	public static float cutoff=0.5f;
	
	@Override
	public String getName() {
		return Translator.get("BoxGeneratorName");
	}


	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_Boxes_Panel(this);
	}

	public void setBoxMaxSize(int arg0) {
		boxMaxSize=arg0;
	}
	
	public int getBoxMasSize() {
		return boxMaxSize;
	}
	
	public void setCutoff(float arg0) {
		cutoff = arg0; 
	}
	public float getCutoff() {
		return cutoff;
	}
	
	/**
	 * turn the image into a grid of boxes.  box size is affected by source image darkness.
	 * @param img the image to convert.
	 */
	public void finish(Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);

		imageStart(out);
		liftPen(out);
		machine.writeChangeToDefaultColor(out);

		double yBottom = machine.getPaperBottom() * machine.getPaperMargin();
		double yTop    = machine.getPaperTop()    * machine.getPaperMargin();
		double xLeft   = machine.getPaperLeft()   * machine.getPaperMargin();
		double xRight  = machine.getPaperRight()  * machine.getPaperMargin();
		double pw = xRight - xLeft;
		
		// figure out how many lines we're going to have on this image.
		double d = machine.getPenDiameter()*boxMaxSize;
		double fullStep = d;
		double halfStep = fullStep / 2.0f;
		
		double steps = pw / fullStep;
		if (steps < 1) steps = 1;

		// from top to bottom of the image...
		double x, y, z;
		int i = 0;
		for (y = yBottom + halfStep; y < yTop - halfStep; y += fullStep) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				for (x = xLeft; x < xRight; x += fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample( x, y - halfStep, x + fullStep, y + halfStep );
					// scale the intensity value
					double scaleZ =  (255.0f - z) / 255.0f;
					double pulseSize = (halfStep) * scaleZ *0.9;
					if (scaleZ > cutoff) {
						double xmin = x + halfStep - pulseSize;
						double xmax = x + halfStep + pulseSize;
						double ymin = y + halfStep - pulseSize;
						double ymax = y + halfStep + pulseSize;
						// Draw a square.  the diameter is relative to the intensity.
						moveTo(out, xmin, ymin, true);
						lowerPen(out);
						moveTo(out, xmax, ymin, false);
						moveTo(out, xmax, ymax, false);
						moveTo(out, xmin, ymax, false);
						moveTo(out, xmin, ymin, false);
						// fill in the square
						boolean flip = false;
						for(double yy=ymin;yy<ymax;yy+=d) {
							moveTo(out,flip?xmin:xmax,yy,false);
							flip = !flip;
						}
						liftPen(out);
					}
				}
			} else {
				// every odd line move right to left
				for (x = xRight; x > xLeft; x -= fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample(x - fullStep, y - halfStep, x, y + halfStep );
					// scale the intensity value
					double scaleZ = (255.0f - z) / 255.0f;
					double pulseSize = (halfStep - 0.5f) * scaleZ;
					if (pulseSize > 0.1f) {
						double xmin = x - halfStep - pulseSize;
						double xmax = x - halfStep + pulseSize;
						double ymin = y + halfStep - pulseSize;
						double ymax = y + halfStep + pulseSize;
						// draw a square.  the diameter is relative to the intensity.
						moveTo(out, xmin, ymin, true);
						lowerPen(out);
						moveTo(out, xmax, ymin, false);
						moveTo(out, xmax, ymax, false);
						moveTo(out, xmin, ymax, false);
						moveTo(out, xmin, ymin, false);
						// fill in the square
						boolean flip = false;
						for(double yy=ymin;yy<ymax;yy+=d) {
							moveTo(out,flip?xmin:xmax,yy,false);
							flip = !flip;
						}
						liftPen(out);
					}
				}
			}
		}

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}
}


/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
