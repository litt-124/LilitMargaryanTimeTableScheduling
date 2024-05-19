import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.logging.*;
import java.util.HashSet;
import java.lang.Math;

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
    private HashSet<String> usedSlotShiftCombos = new HashSet<>();
    private int slots;
    private int shifts;

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
        //for 
        int[] shiftsArray = {15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28};
        int[] iterationsArray = {1000};
        int[] slotsArray = {28, 29};
        // //for file ute-s-92
        // int[] shiftsArray = {1,2,3,4,5,6,7,8,9,10};
        // int[] iterationsArray = {500};
        // int[] slotsArray = {10,11,};

        for (int slots : slotsArray) {
            for (int shifts : shiftsArray) {
                for (int iterations : iterationsArray) {
                    runTest(slots, shifts, iterations);
                }
            }
        }
    }

    private void runTest(int slots, int shifts, int iterations) {
        int numberOfCourses = Integer.parseInt(field[1].getText()) + 1;
        courses = new CourseArray(numberOfCourses, slots);
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
                   new Object[]{slots, shifts, iterations, minClashes, bestStep});
    }

    public void setTools() {
        String capField[] = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
        field = new JTextField[capField.length];

        String capButton[] = {"Load", "Start", "Step", "Print", "Exit", "Continue"};
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
        field[3].setText("100");
        field[4].setText("28");

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
                slots = Integer.parseInt(field[0].getText());
                int numberOfCourses = Integer.parseInt(field[1].getText()) + 1;
                courses = new CourseArray(numberOfCourses, slots);
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

    public void continueScheduling() {
        slots = Integer.parseInt(field[0].getText());
        shifts = Integer.parseInt(field[4].getText());
        int numberOfCourses = courses.length();
        
        for (int i = 1; i < numberOfCourses; i++) {
            if (courses.slot(i) == 0) {
                String key = i + "-" + slots + "-" + shifts;
                if (!usedSlotShiftCombos.contains(key)) {
                    usedSlotShiftCombos.add(key);
                    autoassociator.training(courses.getClashes());
                }

                int bestSlot = autoassociator.predictBestSlot();
                courses.setSlot(i, bestSlot);
                System.out.println("Setting slot " + bestSlot + " for course index " + i);
            }
        }

        // Iterate again to further reduce clashes
        int initialClashes = courses.clashesLeft();
        for (int i = 1; i < numberOfCourses; i++) {
            if (courses.slot(i) != 0) {
                courses.iterate(shifts);
            }
        }
        int finalClashes = courses.clashesLeft();
        if (finalClashes < initialClashes) {
            LOGGER.log(Level.INFO, "Clashes reduced from {0} to {1}", new Object[]{initialClashes, finalClashes});
        } else {
            LOGGER.log(Level.INFO, "No reduction in clashes. Current clashes: {0}", new Object[]{finalClashes});
        }

        // Print results each time continue is clicked
        System.out.println("Current schedule after continue:");
        System.out.println("Exam\tSlot\tClashes");
        for (int i = 1; i < courses.length(); i++) {
            System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
        }

        draw();
    }
    public void continueSchedulingOld() {
        slots = Integer.parseInt(field[0].getText());
        shifts = Integer.parseInt(field[4].getText());
        int numberOfCourses = courses.length();
        double temperature = 1000.0; 
        double coolingRate = 0.95; 
    
        for (int i = 1; i < numberOfCourses; i++) {
            if (courses.slot(i) == 0) {
                String key = i + "-" + slots + "-" + shifts;
                if (!usedSlotShiftCombos.contains(key)) {
                    usedSlotShiftCombos.add(key);
                    autoassociator.training(courses.getClashes());
                }
    
                int bestSlot = autoassociator.predictBestSlot();
                courses.setSlot(i, bestSlot);
                System.out.println("Setting slot " + bestSlot + " for course index " + i);
            }
        }
    
        int initialClashes = courses.clashesLeft();
    
        for (int i = 1; i < numberOfCourses; i++) {
            if (courses.slot(i) != 0) {
                for (int move = 0; move < shifts; move++) {
                    int currentSlot = courses.slot(i);
                    courses.iterate(1);
                    int newClashes = courses.clashesLeft();
    
                    if (newClashes < initialClashes) {
                        initialClashes = newClashes;
                    } else {
                        double acceptanceProbability = Math.exp((initialClashes - newClashes) / Math.max(1.0, temperature));
                        if (Math.random() > acceptanceProbability) {
                            courses.setSlot(i, currentSlot); // Revert to previous slot
                        } else {
                            initialClashes = newClashes;
                        }
                    }
                }
            }
            temperature = Math.max(1.0, temperature * coolingRate); // Cool down but never reach 0
        }
    
        int finalClashes = courses.clashesLeft();
        if (finalClashes < initialClashes) {
            LOGGER.log(Level.INFO, "Clashes reduced from {0} to {1}", new Object[]{initialClashes, finalClashes});
        } else {
            LOGGER.log(Level.INFO, "No reduction in clashes. Current clashes: {0}", new Object[]{finalClashes});
        }
        // Print results each time continue is clicked
        System.out.println("Current schedule after continue:");
        System.out.println("Exam\tSlot\tClashes");
        for (int i = 1; i < courses.length(); i++) {
            System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
        }
    
        draw();
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
