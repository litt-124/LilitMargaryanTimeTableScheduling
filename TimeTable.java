import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.logging.*;

public class TimeTable extends JFrame implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(TimeTable.class.getName());

    static {
        try {
            LOGGER.addHandler(new FileHandler("timetable.log", true));
        } catch (Exception e) {
            System.err.println("Could not set up logger: " + e);
        }
    }
    private Autoassociator autoassociator;
    private JPanel screen = new JPanel(), tools = new JPanel();
    private JButton tool[], testButton;
    private JTextField field[];
    private CourseArray courses;
    private Color CRScolor[] = {Color.RED, Color.GREEN, Color.BLACK};
    
    public TimeTable() {
		super("Dynamic Time Table");
        initializeComponents();
        setVisible(true);

    }
	private void initializeComponents() {
        setSize(500, 800);
        setLayout(new FlowLayout());

        screen = new JPanel();
        screen.setPreferredSize(new Dimension(400, 800));
        add(screen);

        tools = new JPanel();
        setTools();
        add(tools);

        testButton = new JButton("Run Tests");
        testButton.addActionListener(e -> testDifferentCombinations());
        tools.add(testButton);
    }
	public void testDifferentCombinations() {
		int[] shiftsArray = {15,16,17,18,19,20,21,22,23,24,25, 26, 27, 28};
		int[] iterationsArray = {50,100,};
		int[] slotsArray = {28};
	
		for (int slots : slotsArray) {
			for (int shifts : shiftsArray) {
				for (int iterations : iterationsArray) {
					runTest(slots, shifts, iterations);
				}
			}
		}
	}
	
	private void runTest(int slots, int shifts, int iterations) {
		courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, slots);
		courses.readClashes(field[2].getText()); 
	
		int minClashes = Integer.MAX_VALUE;
		int bestStep = 0;
	
		for (int i = 0; i < iterations; i++) {
			courses.iterate(shifts);
			int currentClashes = courses.clashesLeft();
			if (currentClashes < minClashes) {
				minClashes = currentClashes;
				bestStep = i + 1;
			}
		}
	
		LOGGER.log(Level.INFO, "Test with slots = {0}, shifts = {1}, iterations = {2} resulted in min clashes = {3} at step {4}",
				   new Object[] {slots, shifts, iterations, minClashes, bestStep});
	}
	
    public void setTools() {
        String capField[] = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
        field = new JTextField[capField.length];
        
        String capButton[] = {"Load", "Start", "Step", "Print", "Exit","Continue"};
        tool = new JButton[capButton.length];
        
        tools.setLayout(new GridLayout(2 * capField.length + capButton.length, 1));
        
        for (int i = 0; i < field.length; i++) {
            tools.add(new JLabel(capField[i]));
            field[i] = new JTextField(5);
            tools.add(field[i]);
        }
        
        for (int i = 0; i < tool.length; i++) {
            tool[i] = new JButton(capButton[i]);
            tool[i].addActionListener(this);
            tools.add(tool[i]);
        }
        
        field[0].setText("28");
        field[1].setText("543");
        field[2].setText("car-f-92.stu");
        field[3].setText("1");
    }
    
    public void draw() {
        Graphics g = screen.getGraphics();
        int width = Integer.parseInt(field[0].getText()) * 10;
        for (int courseIndex = 1; courseIndex < courses.length(); courseIndex++) {
            g.setColor(CRScolor[courses.status(courseIndex) > 0 ? 0 : 1]);
            g.drawLine(0, courseIndex, width, courseIndex);
            g.setColor(CRScolor[CRScolor.length - 1]);
            g.drawLine(10 * courses.slot(courseIndex), courseIndex, 10 * courses.slot(courseIndex) + 10, courseIndex);
        }
    }
    
    public void actionPerformed(ActionEvent click) {
		int min, step, clashes;
		
		switch (getButtonIndex((JButton) click.getSource())) {
		case 0:  // Load button
			int slots = Integer.parseInt(field[0].getText());
			courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, slots);
			courses.readClashes(field[2].getText());
			if (autoassociator == null) {
				autoassociator = new Autoassociator(courses);
			}
			draw();
			break;
		case 1:  // Start button
			if (courses == null) {
				JOptionPane.showMessageDialog(this, "Please load the courses first.");
				return;
			}
			min = Integer.MAX_VALUE;
			step = 0;
			for (int i = 1; i < courses.length(); i++) courses.setSlot(i, 0);
			
			for (int iteration = 1; iteration <= Integer.parseInt(field[3].getText()); iteration++) {
				courses.iterate(Integer.parseInt(field[4].getText()));
				draw();
				clashes = courses.clashesLeft();
				if (clashes < min) {
					min = clashes;
					step = iteration;
				}
				LOGGER.log(Level.INFO, "Shift = {0}, Min clashes = {1}, at step {2}", new Object[]{field[4].getText(), min, step});
			}
			System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
			setVisible(true);
			break;
		case 2:  // Step button
			if (courses == null) {
				JOptionPane.showMessageDialog(this, "Please load the courses first.");
				return;
			}
			courses.iterate(Integer.parseInt(field[4].getText()));
			draw();
			break;
		case 3:  // Print button
			if (courses == null) {
				JOptionPane.showMessageDialog(this, "No course data available to print.");
				return;
			}
			System.out.println("Exam\tSlot\tClashes");
			for (int i = 1; i < courses.length(); i++)
				System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
			break;
		case 4:  // Exit button
			System.exit(0);
			break;
		case 5:  // Continue button logic
			if (courses == null) {
				JOptionPane.showMessageDialog(this, "Please load the courses first.");
				return;
			}
			continueScheduling();
			break;
		}
	}
	
	private void continueScheduling() {
        int[] simulatedClashes = new int[100];
        for (int i = 0; i < simulatedClashes.length; i++) {
            simulatedClashes[i] = (int) (Math.random() * 10); 
        }
        autoassociator.training(simulatedClashes);
        int bestSlot = autoassociator.predictBestSlot();

        for (int i = 0; i < courses.length(); i++) {
            courses.setSlot(i, bestSlot);
        }
    }

    
    private int getButtonIndex(JButton source) {
        int result = 0;
        while (source != tool[result]) result++;
        return result;
    }
    
    public static void main(String[] args) {
        new TimeTable();
    }
}
