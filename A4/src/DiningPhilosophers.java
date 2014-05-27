import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class DiningPhilosophers extends javax.swing.JApplet
                                implements ActionListener, ChangeListener {

    //delays can go from 0 to 10,000 milliseconds, initial value is 1000
    public int grabDelay = 1000;
    private JButton stopStartButton = new JButton("start");
    private JSlider grabDelaySlider = new JSlider(JSlider.HORIZONTAL, 0, 10000, 1000);
    private JLabel label = new JLabel("  1000 milliseconds");
    private JPanel philosopherArea;

    private int width = 0;
    private int height = 0;
    private double spacing;
    private static final double MARGIN = 10.0f;

    public Map<Dukes, ImageIcon> imgs;
    
    public int NUMPHILS = 5;
    public Chopstick[] chopsticks = null;
    public String[] names = null;
    private Philosopher[] philosophers = null;
 
    public boolean DEADLOCK = true;
   
    public static void main(String[] args) {
	DiningPhilosophers applet = new DiningPhilosophers();
	JFrame frame = new JFrame();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.getContentPane().add(applet, BorderLayout.CENTER);
	frame.setTitle("DiningPhilosophers, by Mike");
	applet.init();
	applet.start();
	frame.setSize(400, 600);
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension frameSize = frame.getSize();
	frame.setLocation((d.width - frameSize.width) / 2, (d.height - frameSize.height) / 2);
	frame.setVisible(true);
    }
    
    public void init() {
         // FOR YOU ...
        NUMPHILS = 7;
        System.err.format("NUMPHILS=%d%n", NUMPHILS);
        
         // FOR YOU ...
        DEADLOCK = true;
        System.err.format("DEADLOCK=%b%n", DEADLOCK);
        
        chopsticks = new Chopstick[NUMPHILS];
        philosophers = new Philosopher[NUMPHILS];
        names = new String[NUMPHILS];
        
        for (int i=0; i<NUMPHILS; i++) {
            names[i] = "Phil #"+(i);
        }
        
        imgs = new EnumMap<Dukes, ImageIcon>(Dukes.class);
        imgs.put(Dukes.NOSTICKSDUKE,
            createAppletImageIcon("images/nosticksduke2.gif",
            "no sticks duke"));
        imgs.put(Dukes.LEFTSTICKDUKE,
            createAppletImageIcon("images/leftstickduke2.gif",
            "left stick duke"));
        imgs.put(Dukes.RIGHTSTICKDUKE,
            createAppletImageIcon("images/rightstickduke2.gif",
            "right stick duke"));
        imgs.put(Dukes.BOTHSTICKSDUKE,
            createAppletImageIcon("images/bothsticksduke2.gif",
            "both sticks duke"));
        width = imgs.get(Dukes.BOTHSTICKSDUKE).getIconWidth() + (int)(MARGIN*2.0);
        height = imgs.get(Dukes.BOTHSTICKSDUKE).getIconHeight() + (int)(MARGIN*2.0);
        width = Math.min(width, height);
        height = width;
        spacing = width + MARGIN;
                        
        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) {
            System.err.format("%s%n", e.getMessage());
            System.err.format("createGUI didn't successfully complete%n");
        }
    }
                
    private void createGUI() {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
                        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(gridBag);
                        
        philosopherArea = new JPanel(null);
        philosopherArea.setBackground(Color.white);
        Dimension preferredSize = createPhilosophersAndChopsticks();
        philosopherArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        philosopherArea.setPreferredSize(preferredSize);
                        
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridBag.setConstraints(philosopherArea, c);
        contentPane.add(philosopherArea);
                        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        gridBag.setConstraints(stopStartButton, c);
        contentPane.add(stopStartButton);
                    
        c.gridwidth = GridBagConstraints.RELATIVE; //don't end row
        c.weightx = 1.0;
        c.weighty = 0.0;
        gridBag.setConstraints(grabDelaySlider, c);
        contentPane.add(grabDelaySlider);
                    
        c.weightx = 0.0;
        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridBag.setConstraints(label, c);
        contentPane.add(label);
        contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            setContentPane(contentPane);
                        
        stopStartButton.addActionListener(this);
        grabDelaySlider.addChangeListener(this);
        
        getRootPane().setDefaultButton(stopStartButton);

    }

    public void actionPerformed(ActionEvent e) {
        if ("stop/reset".equals(stopStartButton.getText())) {
            stopPhilosophers();
            stopStartButton.setText("start");
        } else if ("start".equals(stopStartButton.getText())) {
            startPhilosophers();
            stopStartButton.setText("stop/reset");
        }
    }

    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        grabDelay = source.getValue();
        label.setText(String.valueOf(grabDelay + " milliseconds"));
    }

    public void startPhilosophers() {
        for (int i = 0; i < NUMPHILS; i++)
            philosophers[i].start();
    }

    public void stopPhilosophers() {
        for (int i = 0; i < NUMPHILS; i++)
            philosophers[i].interrupt();
    }

    public Dimension createPhilosophersAndChopsticks() {
        double x, y;
        double radius = 120.0;
        double centerAdj = 140.0;
        double angle;

        Dimension preferredSize = new Dimension(0, 0);

        for (int i = 0; i < NUMPHILS; i++)
            chopsticks[i] = new Chopstick();

        for (int i = 0; i < NUMPHILS; i++) {
            angle = i*(2.0 * Math.PI /(double)NUMPHILS);
            x = Math.sin(angle) * radius + centerAdj; 
            y = Math.cos(angle) * radius + centerAdj;
            philosophers[i] = new Philosopher(this, i, Math.PI - angle);
            philosophers[i].setBounds((int)x, (int)y, width, height);
            philosophers[i].setOpaque(true);
            philosopherArea.add(philosophers[i]);
            if ((int)x > preferredSize.width)
                preferredSize.width = (int)x;
            if ((int)y > preferredSize.height)
                preferredSize.height = (int)y;
        }
        preferredSize.width += width;
        preferredSize.height += height;
        return preferredSize;
    }

    protected static ImageIcon createAppletImageIcon(String path,
                                              String description) {
        int MAX_IMAGE_SIZE = 75000; //Change this to the size of
                                    //your biggest image, in bytes.
        int count = 0;
        BufferedInputStream imgStream = new BufferedInputStream(
           DiningPhilosophers.class.getResourceAsStream(path));
        if (imgStream != null) {
            byte buf[] = new byte[MAX_IMAGE_SIZE];
            try {
                count = imgStream.read(buf);
            } catch (IOException ieo) {
                System.err.format("Couldn't read stream from file: %s%n", path);
            }

            try {
                imgStream.close();
            } catch (IOException ieo) {
                 System.err.format("Can't close file %s%n", path);
            }

            if (count <= 0) {
                System.err.format("Empty file: %s%n", path);
                return null;
            }
            return new ImageIcon(Toolkit.getDefaultToolkit().createImage(buf),
                                 description);
        } else {
            System.err.format("Couldn't find file: %s%n", path);
            return null;
        }
    }
    
}
