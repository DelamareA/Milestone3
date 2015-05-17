import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Comparator; 
import java.util.Collections; 
import java.util.Random; 
import java.util.Map; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ImageProcessing extends PApplet {






PImage img;
PImage sob;
PImage hou;

ArrayList<int[]> cycles = new ArrayList<int[]>();
int[][] graph;

public void setup() {
  size(1800, 450);
}
public void draw() {
  img = loadImage("board1.jpg");
  img.resize(600, 450);
  sob = sobel(convolute(hueTh(convolute(img)))); // first, we blur and then we threshold, and then we blur again
  
  image(img, 0, 0);
  hou = hough(sob, 4);
  image(sob, 1200, 0);
  image(hou, 600, 0);
  
}

public PImage convolute(PImage arg) {
  float[][] kernel = { 
    { 
      30, 20, 30
    }
    , 
    { 
      20, 0, 20 // slightly modified, to remove green pixels
    }
    , 
    { 
      30, 20, 30
    }
  };
  float weight = 200.0f;
  // create a greyscale image (type: ALPHA) for output
  PImage result = createImage(arg.width, arg.height, RGB);

  for (int y = 0; y < arg.height; y++) {
    for (int x = 0; x < arg.width; x++) {
      float r = 0.0f;
      float g = 0.0f;
      float b = 0.0f;

      for (int i = 0; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          int clampedX = x + i - 1;
          if (x + i - 1 < 0) {
            clampedX = 0;
          } else if (x + i - 1 >= arg.width) {
            clampedX = arg.width - 1;
          }

          int clampedY = y + j - 1;
          if (y + j - 1 < 0) {
            clampedY = 0;
          } else if (y + j - 1 >= arg.height) {
            clampedY = arg.height - 1;
          }

          r += red( arg.pixels[clampedY * arg.width + clampedX]) * kernel[i][j];
          g += green( arg.pixels[clampedY * arg.width + clampedX]) * kernel[i][j];
          b += blue( arg.pixels[clampedY * arg.width + clampedX]) * kernel[i][j];
        }
      }

      result.pixels[y * arg.width + x] = color(r / weight, g / weight, b / weight);
    }
  }

  return result;
}


public PImage sobel(PImage arg) {
  float[][] hKernel = { 
    { 
      0, 1, 0
    }
    , 
    { 
      0, 0, 0
    }
    , 
    { 
      0, -1, 0
    }
  };
  float[][] vKernel = { 
    { 
      0, 0, 0
    }
    , 
    { 
      1, 0, -1
    }
    , 
    { 
      0, 0, 0
    }
  };

  PImage thImg = createImage(arg.width, arg.height, RGB);

  for (int i = 0; i < arg.width * arg.height; i++) {
    float value = brightness(arg.pixels[i]);
    if (value >= 20 && value <= 255) {
      thImg.pixels[i] = color(255);
    } else {
      thImg.pixels[i] = color(0);
    }
  }

  PImage result = createImage(arg.width, arg.height, ALPHA);
  // clear the image
  for (int i = 0; i < arg.width * arg.height; i++) {
    result.pixels[i] = color(0);
  }
  float max=0;
  float[] buffer = new float[arg.width * arg.height];


  for (int y = 0; y < arg.height; y++) {
    for (int x = 0; x < arg.width; x++) {
      float sum_h = 0.0f;
      float sum_v = 0.0f;

      for (int i = 0; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          int clampedX = x + i - 1;
          if (x + i - 1 < 0) {
            clampedX = 0;
          } else if (x + i - 1 >= arg.width) {
            clampedX = arg.width - 1;
          }

          int clampedY = y + j - 1;
          if (y + j - 1 < 0) {
            clampedY = 0;
          } else if (y + j - 1 >= arg.height) {
            clampedY = arg.height - 1;
          }

          sum_h += brightness( thImg.pixels[clampedY * thImg.width + clampedX]) * hKernel[i][j];
          sum_v += brightness( thImg.pixels[clampedY * thImg.width + clampedX]) * vKernel[i][j];
        }
      }

      buffer[y * arg.width + x] = sqrt(pow(sum_h, 2) + pow(sum_v, 2));

      if (buffer[y * arg.width + x] > max) {
        max = buffer[y * arg.width + x];
      }
    }
  }

  for (int y = 2; y < arg.height - 2; y++) { // Skip top and bottom edges
    for (int x = 2; x < arg.width - 2; x++) { // Skip left and right
      if (buffer[y * arg.width + x] > max * 0.3f) { // 30% of the max
        result.pixels[y * arg.width + x] = color(255);
      } else {
        result.pixels[y * arg.width + x] = color(0);
      }
    }
  }
  return result;
}


public PImage hueTh (PImage arg) {
  PImage thImg = createImage(arg.width, arg.height, RGB);

  for (int i = 0; i < arg.width * arg.height; i++) {
    float value = brightness(arg.pixels[i]);
    /* FOR A BETTER RESULT WITH BOARD 4, PLEASE DECOMMENT THE 2ND IF */
    if (hue(arg.pixels[i]) >= 100 && hue(arg.pixels[i]) <= 135 && saturation(arg.pixels[i]) >= 120 && saturation(arg.pixels[i]) <= 255 && brightness(arg.pixels[i]) >= 60 && brightness(arg.pixels[i]) <= 160) {
    // if (hue(arg.pixels[i]) >= 90 && hue(arg.pixels[i]) <= 135 && saturation(arg.pixels[i]) >= 65 && saturation(arg.pixels[i]) <= 255 && brightness(arg.pixels[i]) >= 30 && brightness(arg.pixels[i]) <= 140) {
      thImg.pixels[i] = color(255);
    } else {
      thImg.pixels[i] = color(0);
    }
  }

  return thImg;
}

public PImage hough(PImage edgeImg, int nLines) {
  float discretizationStepsPhi = 0.03f;
  float discretizationStepsR = 1f;
  // dimensions of the accumulator
  int phiDim = (int) (Math.PI / discretizationStepsPhi);
  int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
  double rMax = rDim;
  // our accumulator (with a 1 pix margin around)
  int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

  // pre-compute the sin and cos values, using maps, as array give bad result
  HashMap<Float, Float> mapCos = new HashMap<Float, Float>();
  HashMap<Float, Float> mapSin = new HashMap<Float, Float>();
  for (float phi = 0.0f; phi < Math.PI; phi += discretizationStepsPhi) {
    mapCos.put((Float)phi, ((Double) Math.cos(phi)).floatValue());
    mapSin.put((Float)phi, ((Double) Math.sin(phi)).floatValue());
  }

  // Fill the accumulator: on edge points (ie, white pixels of the edge
  // image), store all possible (r, phi) pairs describing lines going
  // through the point.
  for (int y = 0; y < edgeImg.height; y++) {
    for (int x = 0; x < edgeImg.width; x++) {
      // Are we on an edge?
      if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {

        for (float i = 0.0f; i < Math.PI; i += discretizationStepsPhi) {
          double r = (x * mapCos.get(i) + y * mapSin.get(i)) / discretizationStepsR;
          r += (rDim - 1) / 2;
          accumulator[(int) ((i / discretizationStepsPhi + 1) * (rDim + 2) +( r))] += 1;
        }
        // ...determine here all the lines (r, phi) passing through
        // pixel (x,y), convert (r,phi) to coordinates in the
        // accumulator, and increment accordingly the accumulator.
      }
    }
  }


   PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
   for (int i = 0; i < accumulator.length; i++) {
   houghImg.pixels[i] = color(min(255, accumulator[i]));
   }
   houghImg.updatePixels();

  ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
  int minVotes = 60;

  // size of the region we search for a local maximum
  int neighbourhood = 55;
  // only search around lines with more that this amount of votes
  // (to be adapted to your image)
  for (int accR = 0; accR < rDim; accR++) {
    for (int accPhi = 0; accPhi < phiDim; accPhi++) {
      // compute current index in the accumulator
      int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
      if (accumulator[idx] > minVotes) {
        boolean bestCandidate=true;
        // iterate over the neighbourhood
        for (int dPhi=-neighbourhood/2; dPhi < neighbourhood/2+1; dPhi++) {
          // check we are not outside the image
          if ( accPhi+dPhi < 0 || accPhi+dPhi >= phiDim) continue;
          for (int dR=-neighbourhood/2; dR < neighbourhood/2 +1; dR++) {
            // check we are not outside the image
            if (accR+dR < 0 || accR+dR >= rDim) continue;
            int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;
            if (accumulator[idx] < accumulator[neighbourIdx]) {
              // the current idx is not a local maximum!
              bestCandidate=false;
              break;
            }
          }
          if (!bestCandidate) break;
        }
        if (bestCandidate) {
          // the current idx *is* a local maximum
          bestCandidates.add(idx);
        }
      }
    }
  }

  Collections.sort(bestCandidates, new HoughComparator(accumulator));

  ArrayList<PVector> lines = new ArrayList<PVector>(); 


  for (int i = 0; i < bestCandidates.size () && i < nLines; i++) {
    int idx = bestCandidates.get(i);
    // first, compute back the (r, phi) polar coordinates:
    int accPhi = (int) (idx / (rDim + 2)) - 1;
    int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
    float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
    float phi = accPhi * discretizationStepsPhi;

    lines.add(new PVector(r, phi));

    // Cartesian equation of a line: y = ax + b
    // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
    // => y = 0 : x = r / cos(phi)
    // => x = 0 : y = r / sin(phi)
    // compute the intersection of this line with the 4 borders of
    // the image
    int x0 = 0;
    int y0 = (int) (r / sin(phi));
    int x1 = (int) (r / cos(phi));
    int y1 = 0;
    int x2 = edgeImg.width;
    int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
    int y3 = edgeImg.width;
    int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
    // Finally, plot the lines
    stroke(204, 102, 0);
    if (y0 > 0) {
      if (x1 > 0)
        line(x0, y0, x1, y1);
      else if (y2 > 0)
        line(x0, y0, x2, y2);
      else
        line(x0, y0, x3, y3);
    } else {
      if (x1 > 0) {
        if (y2 > 0)
          line(x1, y1, x2, y2);
        else
          line(x1, y1, x3, y3);
      } else
        line(x2, y2, x3, y3);
    }
  }

  getIntersections(lines);

  build(lines, edgeImg.width, edgeImg.height);

  ArrayList<int[]> oldquads = findCycles();

  ArrayList<int[]> quads = new ArrayList<int[]>();

  for (int i = 0; i < oldquads.size (); i++) {
    if (oldquads.get(i).length == 4) {
      quads.add(oldquads.get(i));
    }
  }
  
  //println("Size : " + quads.size());
  
  

  for (int[] quad : quads) {
    PVector l1 = lines.get(quad[0]);
    PVector l2 = lines.get(quad[1]);
    PVector l3 = lines.get(quad[2]);
    PVector l4 = lines.get(quad[3]);
    // (intersection() is a simplified version of the
    // intersections() method you wrote last week, that simply
    // return the coordinates of the intersection between 2 lines)
    PVector c12 = intersection(l1, l2);
    PVector c23 = intersection(l2, l3);
    PVector c34 = intersection(l3, l4);
    PVector c41 = intersection(l4, l1);
    
    if (validArea(c12, c23, c34, c41, 600000, 50000) && isConvex(c12, c23, c34, c41) && nonFlatQuad(c12, c23, c34, c41)){
      // Choose a random, semi-transparent colour
      Random random = new Random();
      fill(color(min(255, random.nextInt(300)), 
      min(255, random.nextInt(300)), 
      min(255, random.nextInt(300)), 50));
      quad(c12.x, c12.y, c23.x, c23.y, c34.x, c34.y, c41.x, c41.y);
    }
  }

  houghImg.resize(600, 450);

  return houghImg;
}

public PVector intersection(PVector l1, PVector l2){
  ArrayList<PVector> lines = new ArrayList<PVector>();
  lines.add(l1);
  lines.add(l2);
  return getIntersections(lines).get(0);
}

public ArrayList<PVector> getIntersections(ArrayList<PVector> lines) {
  ArrayList<PVector> intersections = new ArrayList<PVector>();
  for (int i = 0; i < lines.size () - 1; i++) {
    PVector line1 = lines.get(i);
    for (int j = i + 1; j < lines.size (); j++) {
      PVector line2 = lines.get(j);
      // compute the intersection and add it to 'intersections'
      // draw the intersection
      double d = cos(line2.y) * sin(line1.y) - cos(line1.y) * sin(line2.y);
      int x = (int) ((line2.x * sin(line1.y) - line1.x * sin(line2.y)) / d);
      int y = (int) (( - line2.x * cos(line1.y) + line1.x * cos(line2.y)) / d);

      intersections.add(new PVector(x, y));
      fill(255, 128, 0);
      ellipse(x, y, 10, 10);
    }
  }
  return intersections;
}

class HoughComparator implements Comparator<Integer> {
  int[] accumulator;
  public HoughComparator(int[] accumulator) {
    this.accumulator = accumulator;
  }
  @Override
    public int compare(Integer l1, Integer l2) {
    if (accumulator[l1] > accumulator[l2]
      || (accumulator[l1] == accumulator[l2] && l1 < l2)) return -1;
    return 1;
  }
}

public void build(ArrayList<PVector> lines, int width, int height) {

  int n = lines.size();

  // The maximum possible number of edges is sum(0..n) = n * (n + 1)/2
  graph = new int[n * (n + 1)/2][2];

  int idx =0;

  for (int i = 0; i < lines.size (); i++) {
    for (int j = i + 1; j < lines.size (); j++) {
      if (intersect(lines.get(i), lines.get(j), width, height)) {

        // TODO
        // fill the graph using intersect() to check if two lines are
        // connected in the graph.
        
        graph[idx][0] = i;
        graph[idx][1] = j;

        idx++;
      }
    }
  }
}

/** Returns true if polar lines 1 and 2 intersect 
 * inside an area of size (width, height)
 */
public static boolean intersect(PVector line1, PVector line2, int width, int height) {

  double sin_t1 = Math.sin(line1.y);
  double sin_t2 = Math.sin(line2.y);
  double cos_t1 = Math.cos(line1.y);
  double cos_t2 = Math.cos(line2.y);
  float r1 = line1.x;
  float r2 = line2.x;

  double denom = cos_t2 * sin_t1 - cos_t1 * sin_t2;

  int x = (int) ((r2 * sin_t1 - r1 * sin_t2) / denom);
  int y = (int) ((-r2 * cos_t1 + r1 * cos_t2) / denom);

  if (0 <= x && 0 <= y && width >= x && height >= y)
    return true;
  else
    return false;
}

public ArrayList<int[]> findCycles() {

  cycles.clear();
  for (int i = 0; i < graph.length; i++) {
    for (int j = 0; j < graph[i].length; j++) {
      findNewCycles(new int[] {
        graph[i][j]
      }
      );
    }
  }
  for (int[] cy : cycles) {
    String s = "" + cy[0];
    for (int i = 1; i < cy.length; i++) {
      s += "," + cy[i];
    }
    System.out.println(s);
  }
  return cycles;
}

public void findNewCycles(int[] path)
{
  int n = path[0];
  int x;
  int[] sub = new int[path.length + 1];

  for (int i = 0; i < graph.length; i++)
    for (int y = 0; y <= 1; y++)
      if (graph[i][y] == n)
        //  edge refers to our current node
      {
        x = graph[i][(y + 1) % 2];
        if (!visited(x, path))
          //  neighbor node not on path yet
        {
          sub[0] = x;
          System.arraycopy(path, 0, sub, 1, path.length);
          //  explore extended path
          findNewCycles(sub);
        } else if ((path.length > 2) && (x == path[path.length - 1]))
          //  cycle found
        {
          int[] p = normalize(path);
          int[] inv = invert(p);
          if (isNew(p) && isNew(inv))
          {
            cycles.add(p);
          }
        }
      }
}

//  check of both arrays have same lengths and contents
public static Boolean equals(int[] a, int[] b)
{
  Boolean ret = (a[0] == b[0]) && (a.length == b.length);

  for (int i = 1; ret && (i < a.length); i++)
  {
    if (a[i] != b[i])
    {
      ret = false;
    }
  }

  return ret;
}

//  create a path array with reversed order
public static int[] invert(int[] path)
{
  int[] p = new int[path.length];

  for (int i = 0; i < path.length; i++)
  {
    p[i] = path[path.length - 1 - i];
  }

  return normalize(p);
}

//  rotate cycle path such that it begins with the smallest node
public static int[] normalize(int[] path)
{
  int[] p = new int[path.length];
  int x = smallest(path);
  int n;

  System.arraycopy(path, 0, p, 0, path.length);

  while (p[0] != x)
  {
    n = p[0];
    System.arraycopy(p, 1, p, 0, p.length - 1);
    p[p.length - 1] = n;
  }

  return p;
}

//  compare path against known cycles
//  return true, iff path is not a known cycle
public Boolean isNew(int[] path)
{
  Boolean ret = true;

  for (int[] p : cycles)
  {
    if (equals(p, path))
    {
      ret = false;
      break;
    }
  }

  return ret;
}

//  return the int of the array which is the smallest
public static int smallest(int[] path)
{
  int min = path[0];

  for (int p : path)
  {
    if (p < min)
    {
      min = p;
    }
  }

  return min;
}

//  check if vertex n is contained in path
public static Boolean visited(int n, int[] path)
{
  Boolean ret = false;

  for (int p : path)
  {
    if (p == n)
    {
      ret = true;
      break;
    }
  }

  return ret;
}



/** Check if a quad is convex or not.
 * 
 * Algo: take two adjacent edges and compute their cross-product. 
 * The sign of the z-component of all the cross-products is the 
 * same for a convex polygon.
 * 
 * See http://debian.fmi.uni-sofia.bg/~sergei/cgsr/docs/clockwise.htm
 * for justification.
 * 
 * @param c1
 */
public static boolean isConvex(PVector c1, PVector c2, PVector c3, PVector c4) {

  PVector v21= PVector.sub(c1, c2);
  PVector v32= PVector.sub(c2, c3);
  PVector v43= PVector.sub(c3, c4);
  PVector v14= PVector.sub(c4, c1);

  float i1=v21.cross(v32).z;
  float i2=v32.cross(v43).z;
  float i3=v43.cross(v14).z;
  float i4=v14.cross(v21).z;

  if (   (i1>0 && i2>0 && i3>0 && i4>0) 
    || (i1<0 && i2<0 && i3<0 && i4<0))
    return true;
  else 
    System.out.println("Eliminating non-convex quad");
  return false;
}

/** Compute the area of a quad, and check it lays within a specific range
 */
public static boolean validArea(PVector c1, PVector c2, PVector c3, PVector c4, float max_area, float min_area) {

  PVector v21= PVector.sub(c1, c2);
  PVector v32= PVector.sub(c2, c3);
  PVector v43= PVector.sub(c3, c4);
  PVector v14= PVector.sub(c4, c1);

  float i1=v21.cross(v32).z;
  float i2=v32.cross(v43).z;
  float i3=v43.cross(v14).z;
  float i4=v14.cross(v21).z;

  float area = Math.abs(0.5f * (i1 + i2 + i3 + i4));

  //System.out.println(area);

  boolean valid = (area < max_area && area > min_area);

  //if (!valid) System.out.println("Area out of range : " + area);
  
  System.out.println("Area : " + area);

  return valid;
}

/** Compute the (cosine) of the four angles of the quad, and check they are all large enough
 * (the quad representing our board should be close to a rectangle)
 */
public static boolean nonFlatQuad(PVector c1, PVector c2, PVector c3, PVector c4) {

  // cos(70deg) ~= 0.3
  float min_cos = 0.45f;

  PVector v21= PVector.sub(c1, c2);
  PVector v32= PVector.sub(c2, c3);
  PVector v43= PVector.sub(c3, c4);
  PVector v14= PVector.sub(c4, c1);

  float cos1=Math.abs(v21.dot(v32) / (v21.mag() * v32.mag()));
  float cos2=Math.abs(v32.dot(v43) / (v32.mag() * v43.mag()));
  float cos3=Math.abs(v43.dot(v14) / (v43.mag() * v14.mag()));
  float cos4=Math.abs(v14.dot(v21) / (v14.mag() * v21.mag()));

  if (cos1 < min_cos && cos2 < min_cos && cos3 < min_cos && cos4 < min_cos)
    return true;
  else {
    System.out.println("Flat quad");
    return false;
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ImageProcessing" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
