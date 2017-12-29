/**
 * Rotate 1. 
 * 
 * Rotating simultaneously in the X and Y axis. 
 * Transformation functions such as rotate() are additive.
 * Successively calling rotate(1.0) and rotate(2.0)
 * is equivalent to calling rotate(3.0). 
 */
 
import java.io.*;
 
float a = 0.0;
float rSize;  // rectangle size

Coord[] coords;

void setup() {
  size(960, 640, P3D);
  println("Loading...");
  try {    
    readData();
    println("Done. #" + coords.length);
  } catch(Exception e) {
    println("Cannot read data:");
    e.printStackTrace();
    coords = new Coord[0];
  }
  rSize = width / 6;  
}

void readData() throws Exception {
  String home = System.getProperty("user.home");
  File path = new File(home, "Documents/projects/Imperfect/cern_daten/CERN_hits_positions.txt");
  InputStream fis = new FileInputStream(path);
  try {
    BufferedReader r = new BufferedReader(new InputStreamReader(fis)); 
    int numHits = Integer.parseInt(r.readLine());
    coords = new Coord[numHits];    
    for (int i = 0; i < numHits; i++) {
      String ln = r.readLine();
      // example: 0 -32.093044281 0 0 -13719.4 BM01P1__ 121
      String[] columns = ln.split(" ");
      float t = Float.parseFloat(columns[1]);
      float x = Float.parseFloat(columns[2]);
      float y = Float.parseFloat(columns[3]);
      float z = Float.parseFloat(columns[4]);
      coords[i] = new Coord(t, x, y, z);
    }
  } finally {
    fis.close();
  }
  
  float mx = 0.0f;
  float my = 0.0f;
  float mz = 0.0f;
  float mt = 0.0f;
  for (Coord c : coords) {
    mx = max(mx, abs(c.x));
    my = max(my, abs(c.y));
    mz = max(mz, abs(c.z));
    mt = max(mt, abs(c.t));
  }
  println("mx " + mx + "; my " + my + "; mz " + mz + "; mt " + mt);
  for (Coord c : coords) {
    c.x /= mx;
    c.y /= my;
    c.z /= mz;
    c.t /= mt;
  }
  
  float px = mx;
  float py = my;
  float pz = mz;
  float pt = mt;
  Coord[] sorted = new Coord[coords.length];
  Coord[] copy = coords.clone();
  for (int i = 0; i < sorted.length; i++) {
    int idx = -1;
    double bestDist = Double.POSITIVE_INFINITY;
    for (int j = 0; j < copy.length - i; j++) {
      Coord that = copy[j];
      float dx = px - that.x;
      float dy = py - that.y;
      float dz = pz - that.z;
      float dt = pt - that.t;
      //double dist = sqrt(dx * dx + dy * dy + dz * dz);
      double dist = sqrt(dx * dx + dt * dt + dz * dz);
      if (dist < bestDist) {
        idx = j;
        bestDist = dist;
      }
    }
    println("next: " + idx + "; " + bestDist);
    Coord next = copy[idx];
    sorted[i] = next;
    px = next.x;
    py = next.y;
    pz = next.z;
    pt = next.t;
    System.arraycopy(copy, idx + 1, copy, idx, copy.length - (idx + 1));
  }
  coords = sorted;
}

void draw() {  
  background(0);
  
  //a += 0.001;
  //if(a > TWO_PI) { 
  //  a = 0.0; 
  //}
  
  //translate(width/2, height/2);
  //translate(0, 0, 200);
  
  ////rotateX(a);
  ////rotateY(a * 2.0);
  //rotateZ(a);
  //fill(255);

  float cameraY = height/2.0;
  float fov = mouseX/float(width) * PI/2;
  float cameraZ = cameraY / tan(fov / 2.0);
  float aspect = float(width)/float(height);
  if (mousePressed) {
    aspect = aspect / 2.0;
  }
  perspective(fov, aspect, cameraZ/10.0, cameraZ*10.0);
  
  translate(width/2+30, height/2, 0);
  rotateX(-PI/6);
  rotateY(PI/3 + mouseY/float(height) * PI);

  fill(255);
  
  stroke(255, 0, 0);
  line( 0, 0, -10, 0, 0, 10);
  line(-2, 0,  10, 0, 0, 10);
  line( 2, 0,  10, 0, 0, 10);
  
  stroke(255);
  sphereDetail(3);
  
  Coord prev = null;
  float sc  = 100f;
  float scz = 100f;
  int num = coords.length; // min(coords.length, 30);
  for (int i = 0; i < num; i++) {
    Coord c = coords[i];
    if (prev != null) {
      //line(prev.x * sc, prev.y * sc, prev.z * scz, c.x * sc, c.y * sc, c.z * scz);
      line(prev.x * sc, prev.t * sc, prev.z * scz, c.x * sc, c.t * sc, c.z * scz);
    }
    pushMatrix();
    //translate(c.x * sc, c.y * sc, c.z * scz);
    translate(c.x * sc, c.t * sc, c.z * scz);
    sphere(0.5);
    popMatrix();
    prev = c;
  }
  //rect(-rSize, -rSize, rSize*2, rSize*2);
  
  //rotateX(a * 1.001);
  //rotateY(a * 2.002);
  //fill(0);
  //rect(-rSize, -rSize, rSize*2, rSize*2);

}