import blobDetection.*;

// http://www.v3ga.net/processing/BlobDetection/index-page-documentation.html
BlobDetection theBlobDetection;
PGraphics img;

// ==================================================
// setup()
// ==================================================
void setup()
{
  size(640, 640);
  // Works with Processing 1.5
  // img = createGraphics(640, 480,P2D);
  PImage img1 = loadImage("/home/hhrutz/Documents/projects/Anemone/sim/sim_20h30_21h15_1024.jpg");

  // Works with Processing 2.0b3
  img = createGraphics(640, 480);

  img.beginDraw();
  //img.background(255);
  //img.noStroke();
  //img.fill(0);
  //for (int i=0;i<20;i++) {
  //  float r = random(50);
  //  img.ellipse(random(img.width), random(img.height), r, r);
  //}
  img.image(img1, 0, 0);
  img.endDraw();

  // int blobMaxNb, int blobLinesMaxNb, int blobTrianglesMaxNb
  /*
    blobMaxNb          is the maximum number of blobs that an instance of
                       BlobDetection can store. (default value is 1000)
    blobLinesMaxNb     is the maximum number of edges ("lines") a single 
                       blob can store. (default value is 4000)
    blobTrianglesMaxNb is the maximum number of triangles a single blob
                       can store. (default value is 500)
  */
  BlobDetection.setConstants(1000, 1000, 100);
  theBlobDetection = new BlobDetection(img.width, img.height);
  /*
  If passed boolean value is true, the blob detection will attempt
  to find bright areas. Otherwise, it will detect dark areas (default mode)
  */
  theBlobDetection.setPosDiscrimination(true);
  /*
  BlobDetection analyzes image for finding dark or bright areas, 
  depending on the selected mode. The threshold is a float number
  between 0.0f and 1.0f used by the blobDetection to find blobs which
  contains pixels whose luminosity is inferior (for dark areas) or
  superior (for bright areas) to this value.
  */
  theBlobDetection.setThreshold(0.28f); // (0.38f)
  theBlobDetection.computeBlobs(img.pixels);

  // Size of applet
  // size(img.width, img.height);
}

// ==================================================
// draw()
// ==================================================
void draw()
{
  image(img, 0, 0, width, height);
  drawBlobsAndEdges(true, false /* true */);
}

// ==================================================
// drawBlobsAndEdges()
// ==================================================
void drawBlobsAndEdges(boolean drawBlobs, boolean drawEdges)
{
  noFill();
  Blob b;
  EdgeVertex eA, eB;
  for (int n=0 ; n<theBlobDetection.getBlobNb() ; n++)
  {
    b=theBlobDetection.getBlob(n);
    if (b!=null)
    {
      // Edges
      if (drawEdges)
      {
        strokeWeight(2);
        stroke(0, 255, 0);
        for (int m=0;m<b.getEdgeNb();m++)
        {
          eA = b.getEdgeVertexA(m);
          eB = b.getEdgeVertexB(m);
          if (eA !=null && eB !=null)
            line(
            eA.x*width, eA.y*height, 
            eB.x*width, eB.y*height
              );
        }
      }

      // Blobs
      if (drawBlobs)
      {
        strokeWeight(1);
        stroke(255, 0, 0);
        rect(
        b.xMin*width, b.yMin*height, 
        b.w*width, b.h*height
          );
      }
    }
  }
}