package search;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;

class LandAnalysisTest{
	private int height = 600;
	private int width = 400;
	private int rootArea = width * height;
	
	class FakeApp extends MockUp<LandAnalysis>{
		@Mock
		void $init(Invocation invocation) throws NoSuchFieldException, IllegalAccessException{
			LandAnalysis thisApp = (LandAnalysis) invocation.getInvokedInstance();
			Field field1 = thisApp.getClass().getDeclaredField("rectangles");
			field1.setAccessible(true);
			field1.set(thisApp, rectangles);
			Field field2 = thisApp.getClass().getDeclaredField("width");
			field2.setAccessible(true);
			field2.set(thisApp, width);
			Field field3 = thisApp.getClass().getDeclaredField("height");
			field3.setAccessible(true);
			field3.set(thisApp, height);
			Field field4 = thisApp.getClass().getDeclaredField("rootArea");
			field4.setAccessible(true);
			field4.set(thisApp, rootArea);
		}
	}
	
	/*int yDiff1 = 207 - 192;
	int yDiff2 = 407 - 392;
	int yDiff3 = 547 - 52;
	int yDiff4 = 547 - 52;
	
	
	ArrayList<Rectangle> rectangles = new ArrayList<>(Arrays.asList(new Rectangle(48, height - 192 - yDiff1 - 1, 351 - 48 + 1, yDiff1 + 1),
																	new Rectangle(48, height - 392 - yDiff2 - 1, 351 - 48 + 1, yDiff2 + 1),
																	new Rectangle(120, height - 52 - yDiff3 - 1, 135 - 120 + 1, yDiff3 + 1),
																	new Rectangle(260, height - 52 - yDiff4 - 1, 275 - 260 + 1, yDiff4 + 1)));*/
	
	

	int yDiff = 307 - 292;
	int bottomLeftX = 0;
	int bottomLeftY = height - 292 - yDiff - 1;
	int myWidth = 399 - bottomLeftX + 1;
	
	ArrayList<Rectangle> rectangles = new ArrayList<>(Arrays.asList(new Rectangle(	bottomLeftX,
																					bottomLeftY,
																					myWidth,
																					yDiff + 1)));
	
	@Test
	void test(){
		String data = "{“48 192 351 207”, “48 392 351 407”, “120 52 135 547”, “260 52 275 547”}";
		System.setIn(new ByteArrayInputStream(data.getBytes()));
		//LandAnalysis app = new LandAnalysis();
		//assertEquals(4, app.getRectangles().size());
		
		LandAnalysis.main(new String[]{});
	}
	
	@Test
	void stuff(){
		new FakeApp();
		LandAnalysis landAnalysis = new LandAnalysis();
		ArrayList<Polygon> polygons = landAnalysis.extractPolygons();
		HashMap<Polygon, Integer> areas = landAnalysis.calculateAreas(polygons);
		DefaultMutableTreeNode defaultMutableTreeNode = landAnalysis.buildTree(polygons, areas);
		landAnalysis.determineAreaOfFertileLand(defaultMutableTreeNode);
	}
	
	Area area = new Area(new Rectangle(400, 600));
	float firstX;
	float firstY;
	float x;
	float y;
	ArrayList<int[]> lines = new ArrayList<>();
	int index;
	boolean yes;
	
	class Panel extends JPanel{
		protected void paintComponent(Graphics graphics){
			super.paintComponent(graphics);
			
			if(yes){
				for(Rectangle rectangle : rectangles){
					graphics.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
				}
			
			}
			else{
				((Graphics2D) graphics).draw(area);	//https://docs.oracle.com/javase/tutorial/2d/overview/rendering.html
			}
			
			/*Corners*/
			graphics.drawRect(0, 0, 5, 5);
			graphics.drawRect(395, 595, 5, 5);
		}
	}
	
	private Thread eventDispatchingThread;	//local variable isn't effectively final
	
	@Test
	@Disabled("Integration/visual analysis blocks single-threaded test runner")
	void visual() throws InvocationTargetException, InterruptedException{
		new FakeApp();
		LandAnalysis app = new LandAnalysis();
		app.setupArea(area);
		JFrame jFrame = new JFrame();
		jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Panel panel = new Panel();
		panel.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent mouseEvent){
				if(mouseEvent.getButton() == MouseEvent.BUTTON1){
					/*if(!pathIterator.isDone()){
						iterate(pathIterator, array);
					}*/
				}
				else{
					yes = !yes;
				}
				panel.repaint();
			}
			
		});
		jFrame.add(panel);
		jFrame.setVisible(true);
		Insets insets = jFrame.getInsets();
		jFrame.setSize(400 + insets.left + insets.right, 600 + insets.top + insets.bottom);
		
		SwingUtilities.invokeAndWait(() -> eventDispatchingThread = Thread.currentThread());
		eventDispatchingThread.join();
	}
	
	/*void iterate(PathIterator pathIterator, float[] array){
		int currentSegment = pathIterator.currentSegment(array);
		
		switch(currentSegment){
		case PathIterator.SEG_MOVETO:
			System.out.println("SEG_MOVETO");
			System.out.println("X: " + array[0] + " Y: " + array[1]);
			x = firstX = array[0];
			y = firstY = array[1];
			break;
		case PathIterator.SEG_LINETO:
			System.out.println("SEG_LINETO");
			System.out.println("X: " + array[0] + " Y: " + array[1]);
			index++;
			lines.add(new int[]{(int) x, (int) y, (int) array[0], (int) array[1]});
			x = (int) array[0];
			y = (int) array[1];
			break;
		case PathIterator.SEG_QUADTO:
			System.out.println("SEG_QUADTO");
			break;
		case PathIterator.SEG_CUBICTO:
			System.out.println("SEG_CUBICTO");
			break;
		case PathIterator.SEG_CLOSE:
			System.out.println("SEG_CLOSE");
			index++;
			lines.add(new int[]{(int) x, (int) y, (int) firstX, (int) firstY});
			break;
		}
		pathIterator.next();
	}*/
}
