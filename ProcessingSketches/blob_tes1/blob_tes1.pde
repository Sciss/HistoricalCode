import blobscanner.*;

Detector bd;

String s1 = "Screenshot from 2014-10-30 15:27:39.png";
String s2 = "Screenshot from 2014-10-30 15:51:33.png";

PImage img = loadImage("/home/hhrutz/Pictures/" + s1  );

size(img.width, img.height);

image(img, 0, 0);

img.filter(THRESHOLD, 0.6);

bd = new Detector( this, 0, 0, img.width, img.height, 255 );

color boundingBoxCol = color(255, 0, 0);
int boundingBoxThickness = 1;

img.loadPixels();

bd.findBlobs(img.pixels, img.width, img.height);

int blobnumber = bd.getBlobsNumber();

println(blobnumber);

bd.loadBlobsFeatures(); 
bd.drawBox(boundingBoxCol, boundingBoxThickness);
