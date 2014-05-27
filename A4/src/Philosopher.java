import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class Philosopher extends JLabel {
    private DiningPhilosophers parent;
    private int position;
    private double angle;
    public PhilWorker philWorker = null;

    private int left() {
        return position == 0? parent.NUMPHILS-1: position-1;
    }

    private int right() {
        return position;
    }

    private boolean even() {
        return position % 2 == 0;
    }

    private boolean odd() {
        return position % 2 != 0;
    }
    
    private int first() {
        // FOR YOU ...
        return right();
    }

    private int second() {
       // FOR YOU ...
       return left();
    }

   private Chopstick firstStick() {
        return parent.chopsticks[first()];
    }
        
    private Chopstick secondStick() {
        return parent.chopsticks[second()];
    }
        
    private Dukes firstStickDuke() {
        // FOR YOU ...
        return Dukes.RIGHTSTICKDUKE;
    }
    
    private Dukes secondStickDuke() {
        // FOR YOU ...
        return Dukes.LEFTSTICKDUKE;
    }
    
    public Philosopher(DiningPhilosophers parent, int position, double angle) {
	 super(parent.names[position], 
	       parent.imgs.get(Dukes.NOSTICKSDUKE),
	       JLabel.CENTER);

        this.parent = parent;
        this.position = position;
        this.angle = angle;

        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
    }
    
    public void start() {
         philWorker = new PhilWorker();
         philWorker.start(); // FOR YOU ...
    }

    public void interrupt() {
         philWorker.interrupt(); // FOR YOU ...
         philWorker = null;
    }

    private int period(int low, int high) {
         return (int) (low + (high-low) * Math.random()) * parent.grabDelay;
    }
    
    class PhilWorker extends Thread { // FOR YOU SwingWorker <Object, Update>
        @Override
        public void run() {
             try {
                while (! isCancelled()) {
                    // THINK
                    System.err.format("Philospher %d thinks%n", position, first());
                    try {
                        updatePhilosopher(Dukes.NOSTICKSDUKE, "Think...", Color.black);
                    } catch (Exception e) {
                        break;
                    }
                    Thread.sleep(2*period(1, 2));
                    
                    // HUNGER
                    System.err.format("Philospher %d hungry%n", position, first());
                    // FOR YOU ...
                    
                    // HUNGER AND FIRST STICK
                    firstStick().grab();
                    System.err.format("Philospher %d grabs Stick %d%n", position, first());
                    try {
                        updatePhilosopher(firstStickDuke(), "Hungry", Color.RED);
                    } catch (Exception e) {
                        break;
                    }
                    Thread.sleep(period(1, 2));
                    
                    // BOTH STICKS AND EAT
                    secondStick().grab();
                    System.err.format("Philospher %d grabs Stick %d and eats%n", position, second());
                    // FOR YOU ...
                    Thread.sleep(3*period(1, 2)); 
                    
                    // RELEASE SECOND STICK
                    secondStick().release();
                    // FOR YOU ...
                    
                    // RELEASE FIRST STICK
                    firstStick().release();
                    // FOR YOU ...
                }
                
            } catch (InterruptedException e) {
                System.err.format("Philospher %d was interrupted%n", position);
            } catch (Exception e) {
                System.err.format("Philospher %d raised an exception %s%n", position, e.getMessage());
            }

            firstStick().releaseIfMine();
            secondStick().releaseIfMine();
                        
            try {
                updatePhilosopher(Dukes.NOSTICKSDUKE, parent.names[position], Color.black);
            } catch (Exception e) {
                System.err.format("Couldn't update icon for %d%n", position);
            }
            
            //return null;
        }
        
        boolean isCancelled() {
            return interrupted(); // FOR YOU ...
        }
        
        void updatePhilosopher(Dukes icon, String text, Color color) throws Exception {
            // FOR YOU ... publish
            updatePhilosopher2(new Update(icon, text, color));
        }
        
       // FOR YOU ... process
       public void updatePhilosopher2(final Update update) throws Exception {
	   Runnable updateThePhilosopher = new Runnable() {
		   public void run() {
		       setIcon(parent.imgs.get(update.icon));
		       setText(String.format("%d: %s", position, update.text));
		       setForeground(update.color);
		   }
	       };
	   SwingUtilities.invokeAndWait(updateThePhilosopher);
       }
    }      
    
    class Update {
        public Dukes icon; 
        public String text;
        public Color color;
        public Update(Dukes icon, String text, Color color) {
            this.icon = icon;
            this.text = text;
            this.color = color;
        }
    }
    
}
