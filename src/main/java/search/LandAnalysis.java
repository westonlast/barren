package search;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * https://en.wikipedia.org/wiki/Shoelace_formula
 * https://www.geeksforgeeks.org/area-of-a-polygon-with-given-n-ordered-vertices/
 * https://en.wikipedia.org/wiki/Space_partitioning
 * http://coopsoft.com/ar/Calamity2Article.html
 */
class LandAnalysis{
	private int width = 400;
	private int height = 600;
	private int rootArea = width * height;
	private ArrayList<Rectangle> rectangles;
	boolean containsRootArea;
	
	public static void main(String[] args){
		LandAnalysis landAnalysis = new LandAnalysis();
		ArrayList<Polygon> polygons = landAnalysis.extractPolygons();
		HashMap<Polygon, Integer> areas = landAnalysis.calculateAreas(polygons);
		DefaultMutableTreeNode defaultMutableTreeNode = landAnalysis.buildTree(polygons, areas);
		ArrayList<Integer> fertileAreas = landAnalysis.determineAreaOfFertileLand(defaultMutableTreeNode);
		fertileAreas.sort(Comparator.naturalOrder());
		
		for(Integer area : fertileAreas){
			System.out.print(area + " ");
		}
		System.out.println();
	}
	
	/**
	 * Parses input from STDIN.  Could be more simply written with a regular expression, but I wanted to play with
	 * Scanner.
	 */
	public LandAnalysis(){
		rectangles = new ArrayList<>();

		try(Scanner scanner = new Scanner(System.in)){
			Pattern openingQuote = Pattern.compile("“");
			Pattern closingQuote = Pattern.compile("”");
			Pattern comma = Pattern.compile(",");
			Pattern defaultDelimiter = scanner.skip("\\{").delimiter();
			
			do{
				int bottomLeftX = scanner.skip(openingQuote).nextInt();
				int bottomLeftY = scanner.nextInt();
				int topRightX = scanner.nextInt();
				int topRightY = scanner.skip(defaultDelimiter).useDelimiter(closingQuote).nextInt();
				rectangles.add(convertToUserSpace(bottomLeftX, bottomLeftY, topRightX, topRightY));
				scanner.useDelimiter(defaultDelimiter).skip(closingQuote);
			}
			while(scanner.hasNext(comma) && (scanner.skip(comma).skip(defaultDelimiter) != null));
		}
	}
	
	/**
	 * https://docs.oracle.com/javase/tutorial/2d/overview/coordinate.html
	 */
	private Rectangle convertToUserSpace(int bottomLeftX, int bottomLeftY, int topRightX, int topRightY){
		int yDiff = topRightY - bottomLeftY;
		return new Rectangle(bottomLeftX, height - bottomLeftY - yDiff - 1, topRightX - bottomLeftX + 1, yDiff + 1);
	}
	
	public ArrayList<Polygon> extractPolygons(){
		Area area = new Area(new Rectangle(width, height));
		setupArea(area);
		
		PathIterator pathIterator = area.getPathIterator(null);
		
		float[] array = new float[6];
		ArrayList<Polygon> polygons = new ArrayList<>();
		Polygon polygon = null;
		
		while(!pathIterator.isDone()){
			int currentSegment = pathIterator.currentSegment(array);
			
			switch(currentSegment){
			case PathIterator.SEG_MOVETO:
				//System.out.println("SEG_MOVETO");
				//System.out.println("X: " + array[0] + " Y: " + array[1]);
				polygon = new Polygon();
				polygon.addPoint((int) array[0], (int) array[1]);
				break;
			case PathIterator.SEG_LINETO:
				//System.out.println("SEG_LINETO");
				//System.out.println("X: " + array[0] + " Y: " + array[1]);
				polygon.addPoint((int) array[0], (int) array[1]);
				break;
			case PathIterator.SEG_CLOSE:
				//System.out.println("SEG_CLOSE");
				polygons.add(polygon);
				break;
			}
			pathIterator.next();
		}
		return polygons;
	}
	
	public Area setupArea(Area area){
		for(Rectangle rectangle : rectangles){
			area.subtract(new Area(rectangle));
		}
		return area;
	}

	public HashMap<Polygon, Integer> calculateAreas(ArrayList<Polygon> polygons){
		HashMap<Polygon, Integer> areas = new HashMap<>();
		
		for(Polygon polygon : polygons){
			int area = calculateArea(polygon);
			
			if(area == rootArea){
				containsRootArea = true;
			}
			areas.put(polygon, area);
		}
		
		if(!containsRootArea){
			Polygon polygon = new Polygon();
			polygon.addPoint(0, 0);
			polygon.addPoint(width, 0);
			polygon.addPoint(width, height);
			polygon.addPoint(0, height);
			polygons.add(polygon);
			areas.put(polygon, rootArea);
		}
		return areas;
	}
	
	/**
	 * Uses the shoelace algorithm to calculate the area of the Polygon.
	 * @param polygon	The polygon.
	 * @return			The area.
	 */
	private int calculateArea(Polygon polygon){
		int index = polygon.npoints - 1;
		int total = 0;
		
		for(int j = 0; j < polygon.npoints; j++){
			total += (polygon.xpoints[index] + polygon.xpoints[j]) * (polygon.ypoints[index] - polygon.ypoints[j]);
			index = j;
		}
		return Math.abs(total / 2);
	}
	
	
	public DefaultMutableTreeNode buildTree(ArrayList<Polygon> polygons, HashMap<Polygon, Integer> areas){
		polygons.sort(Comparator.comparingInt((polygon) -> areas.get(polygon)).reversed());
		return convertUsingSpacePartitioning(polygons, 0);
	}
	
	/**
	 * Builds a tree from the sorted list by checking if a point from a smaller polygon is contained within a larger
	 * one.  If the larger polygon contains a single point of another, it contains the entire other polygon.
	 * Precondition: The polygons are sorted in descending order by area.
	 * @param polygons	The sorted polygons.
	 * @param index		The index from which to start (for recursion).
	 * @return			The root node of the space partitioning tree.
	 */
	private DefaultMutableTreeNode convertUsingSpacePartitioning(ArrayList<Polygon> polygons, int index){
		Polygon current = polygons.get(index);
		DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(current);

		for(int i = index + 1; i < polygons.size(); i++){
			Polygon polygon = polygons.get(i);
			
			if(current.contains(polygon.xpoints[0], polygon.ypoints[0])){
				if((i + 1) < polygons.size()){
					defaultMutableTreeNode.add(convertUsingSpacePartitioning(polygons, i));	//sublist again
					i--;
				}
				else{
					polygons.remove(polygon);
					defaultMutableTreeNode.add(new DefaultMutableTreeNode(polygon));
				}
			}
		}
		polygons.remove(current);
		return defaultMutableTreeNode;
	}
	
	public ArrayList<Integer> determineAreaOfFertileLand(DefaultMutableTreeNode defaultMutableTreeNode){
		int remainder;
		
		if(containsRootArea){	//root node is fertile
			remainder = 0;
		}
		else{	//root node is barren
			remainder = 1;
		}
		ArrayList<Integer> arrayList = new ArrayList<>();
		
		/*Breadth-first traversal*/
		LinkedList<DefaultMutableTreeNode> linkedList = new LinkedList<>();
		
		linkedList.add(defaultMutableTreeNode);
		while(!linkedList.isEmpty()){
			DefaultMutableTreeNode node = linkedList.remove();
			
			Polygon polygon = (Polygon) node.getUserObject();
			
			if(node.getLevel() % 2 == remainder){	//fertile
				int area = calculateArea(polygon);
				
				/*https://docs.oracle.com/javase/specs/jls/se11/html/jls-9.html#jls-9.6.4.5 */
				@SuppressWarnings("unchecked")
				Enumeration children = node.children();
				
				while(children.hasMoreElements()){
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
					int inner = calculateArea((Polygon) child.getUserObject());
					area -= inner;
					linkedList.add(child);
				}
				arrayList.add(area);
			}
			else{	//barren
				/*https://docs.oracle.com/javase/specs/jls/se11/html/jls-9.html#jls-9.6.4.5 */
				@SuppressWarnings("unchecked")
				Enumeration children = node.children();
				
				while(children.hasMoreElements()){
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
					linkedList.add(child);
				}
			}
		}
		return arrayList;
	}
	
	public ArrayList<Rectangle> getRectangles(){
		return rectangles;
	}
}
