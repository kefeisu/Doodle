import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class View extends JFrame implements Observer {

    private Model model;

    boolean saved = true;
    boolean docked = true;
    JFrame floatingPalette = null;
    JMenuBar menuBar;
    JMenu file, edit;
    JMenuItem fileOpen, fileSave;
    JMenuItem dock, editCopy, editPaste;
    JLabel doodle = new JLabel("Doodle!");
    JLabel spaces = new JLabel("        ");
    JLabel spaces2 = new JLabel("        ");
    JPanel west;

    /**
     * Create a new View.
     */
    public View(Model model) {
        // Set up the window.
        this.setTitle("Doodle!");
        this.setMinimumSize(new Dimension(400, 300));
        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Hook up this observer so that it will be notified when the model
        // changes.
        this.model = model;
        model.addObserver(this);


        west = new ColorPalette(model);
        JPanel south = new Playback(model);
        west.setPreferredSize(new Dimension(125, 600));
        west.setBorder(BorderFactory.createTitledBorder("Palette"));
        south.setBorder(BorderFactory.createTitledBorder("Playback"));
        this.setLayout(new BorderLayout());
        this.add(new Canvas(model));
        this.add(west, BorderLayout.WEST);
        this.add(south, BorderLayout.SOUTH);


        menuBar = new JMenuBar();
        menuBar.add(spaces);
        menuBar.add(doodle);
        menuBar.add(spaces2);
        file = new JMenu("File");
        file.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) { }
            public void menuDeselected(MenuEvent e) { }
            public void menuCanceled(MenuEvent e) { }
        });
        menuBar.add(file);

        edit = new JMenu("Edit");
        edit.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) { }
            public void menuDeselected(MenuEvent e) { }
            public void menuCanceled(MenuEvent e) { }
        });
        menuBar.add(edit);

        fileOpen = new JMenuItem("Open...");
        fileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!saved) {
                    if (JOptionPane.showConfirmDialog(null,
                            "Opening a new file will overwrite current drawing, are you sure?", "Save",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        JFileChooser chooser = new JFileChooser();
                        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                                "Serialized files (.ser)", "ser");
                        chooser.setFileFilter(filter);
                        int returnVal = chooser.showOpenDialog(null);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            System.out.println("You chose to open this file: " +
                                    chooser.getSelectedFile().getName());
                            File myFile = chooser.getSelectedFile();
                            data d = null;
                            try {
                                FileInputStream myFileInputStream = new FileInputStream(myFile);
                                ObjectInputStream myObjectInputStream = new ObjectInputStream(myFileInputStream);
                                d = (data) myObjectInputStream.readObject();
                                myObjectInputStream.close();
                            } catch (Exception exc) {
                                System.out.println("Error deserializing data ");
                                //Log.e("Error when loading from file.",Log.getStackTraceString(e));
                            }
                            model.load(d);
                            model.notifyObservers();
                        }
                    }
                } else {
                    JFileChooser chooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(
                            "Serialized files (.ser)", "ser");
                    chooser.setFileFilter(filter);
                    int returnVal = chooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        System.out.println("You chose to open this file: " +
                                chooser.getSelectedFile().getName());
                        File myFile = chooser.getSelectedFile();
                        data d = null;
                        try {
                            FileInputStream myFileInputStream = new FileInputStream(myFile);
                            ObjectInputStream myObjectInputStream = new ObjectInputStream(myFileInputStream);
                            d = (data) myObjectInputStream.readObject();
                            myObjectInputStream.close();
                        } catch (Exception exc) {
                            System.out.println("Error deserializing data ");
                            //Log.e("Error when loading from file.",Log.getStackTraceString(e));
                        }
                        model.load(d);
                        model.notifyObservers();
                    }
                }
            }
        });
        file.add(fileOpen);

        fileSave = new JMenuItem("Save...");
        fileSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!model.isEmpty()) {
                    String inputValue = JOptionPane.showInputDialog("File Name:");
                    JFileChooser chooser = new JFileChooser();
                    //FileNameExtensionFilter filter = new FileNameExtensionFilter("Directory");
                    chooser.setCurrentDirectory(new java.io.File("."));
                    chooser.setDialogTitle("Choose a Directory");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setAcceptAllFileFilterUsed(false);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File f = chooser.getSelectedFile();
                        data d = new data(model);
                        d.serializeData(f.getPath()+ "/" + inputValue);
                        saved = true;
                    }
                }
            }
        });
        file.add(fileSave);

        dock = new JMenuItem("dock/undock");
        dock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (docked) {
                    undock();
                    docked = false;
                } else {
                    dock();
                    docked = true;
                }
            }
            });
        edit.add(dock);

        this.add(menuBar,BorderLayout.NORTH);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!saved) {
                    if (JOptionPane.showConfirmDialog(null,
                            "Are you sure you want to close without saving?", "Save",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                } else {
                    System.exit(0);
                }
            }
        });

        setVisible(true);
    }

    public void undock(){
        this.remove(west);
        floatingPalette = new JFrame();
        floatingPalette.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dock();
                docked = true;
            }
        });
        floatingPalette.setSize(100, 550);
        floatingPalette.add(west);
        floatingPalette.setVisible(true);
        repaint();
    }

    public void dock(){

        this.add(west, BorderLayout.WEST);
        floatingPalette.setVisible(false);
        floatingPalette.dispose();
        model.notifyObservers();
    }


    /**
     * Update with data from the model.
     */
    public void update(Object observable) {
        saved = false;
    }
}

class ColorPalette extends JPanel implements Observer {
    private Model model;

    private JButton choose = new JButton(new ImageIcon("./src/choose.png"));
    private JButton black = new JButton(new ImageIcon("./src/black.png"));
    private JButton gray = new JButton(new ImageIcon("./src/gray.png"));
    private JButton red = new JButton(new ImageIcon("./src/red.png"));
    private JButton blue = new JButton(new ImageIcon("./src/blue.png"));
    private JButton green = new JButton(new ImageIcon("./src/green.png"));
    private JButton orange = new JButton(new ImageIcon("./src/orange.png"));
    private JButton pink = new JButton(new ImageIcon("./src/pink.png"));

    private JButton brush = new JButton(new ImageIcon("./src/brush.png"));
    private JButton line = new JButton(new ImageIcon("./src/line.png"));
    private JButton ellipse = new JButton(new ImageIcon("./src/ellipse.png"));
    private JButton rectangle = new JButton(new ImageIcon("./src/rectangle.png"));
    private JButton eraser = new JButton(new ImageIcon("./src/eraser.png"));
    private JButton clear = new JButton("clear");

    private JLabel opacity = new JLabel("opacity");
    private JSlider opacityLevel = new JSlider(1,99,99);

    private JLabel thickness = new JLabel("thickness");
    private JSlider thicknessLevel = new JSlider(1,25,5);

    private JLabel col = new JLabel("Color:");
    private JLabel coll = new JLabel("");

    public ColorPalette(Model model) {
        super();

        this.model = model;
        model.addObserver(this);
        model.defaultColor = this.getBackground();


        GridLayout strategy = new GridLayout(10,2);
        this.setLayout(strategy);

        this.choose.setBounds(10,10,10,10);
        this.choose.setVisible(true);
        choose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.strokeColor = JColorChooser.showDialog(null,  "Choose a color", Color.BLACK);
                model.fillColor = model.strokeColor;
                coll.setBackground(model.strokeColor);
            }
        });
        this.add(this.choose);

        black.setBackground(Color.BLACK);
        //black.setForeground(Color.BLACK);
        //black.setOpaque(true);
        black.setBorderPainted(false);
        black.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.strokeColor = Color.BLACK;
                model.fillColor = Color.BLACK;
                coll.setBackground(model.strokeColor);
            }
        });
        this.add(this.black);


        gray.setBackground(Color.GRAY);
        //gray.setOpaque(true);
        //gray.setBorderPainted(false);
        gray.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.strokeColor = Color.GRAY;
                model.fillColor = Color.GRAY;
                coll.setBackground(model.strokeColor);
            }
        });
        this.add(this.gray);

        red.setBackground(Color.RED);
        //red.setOpaque(true);
        //red.setBorderPainted(false);
        red.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.strokeColor = Color.RED;
                model.fillColor = Color.RED;
                coll.setBackground(model.strokeColor);
            }
        });
        this.add(this.red);

        blue.setBackground(Color.BLUE);
        //blue.setOpaque(true);
        //blue.setBorderPainted(false);
        blue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.strokeColor = Color.BLUE;
                model.fillColor = Color.BLUE;
                coll.setBackground(model.strokeColor);
            }
        });
        this.add(this.blue);

        green.setBackground(Color.GREEN);
        //green.setOpaque(true);
        //green.setBorderPainted(false);
        green.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.strokeColor = Color.GREEN;
                model.fillColor = Color.GREEN;
                coll.setBackground(model.strokeColor);
            }
        });
        this.add(this.green);

        orange.setBackground(Color.ORANGE);
        //orange.setOpaque(true);
        //orange.setBorderPainted(false);
        orange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.strokeColor = Color.ORANGE;
                model.fillColor = Color.ORANGE;
                coll.setBackground(model.strokeColor);
            }
        });
        this.add(this.orange);


        pink.setBackground(Color.PINK);
        //pink.setOpaque(true);
        //pink.setBorderPainted(false);
        pink.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.strokeColor = Color.PINK;
                model.fillColor = Color.PINK;
                coll.setBackground(model.strokeColor);
            }
        });
        this.add(this.pink);

        brush.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.tool = 1;
                model.erase = false;
            }
        });
        this.add(this.brush);


        line.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.tool = 2;
                model.erase = false;
            }
        });
        this.add(this.line);

        eraser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.tool = 1;
                model.erase = true;
            }
        });
        this.add(this.eraser);

        ellipse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.tool = 3;
                model.erase = false;
            }
        });
        this.add(this.ellipse);

        rectangle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.tool = 4;
                model.erase = false;
            }
        });
        this.add(this.rectangle);

        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!model.isEmpty()) {
                    int b = JOptionPane.showConfirmDialog(null,
                            "Clearing will delete everything including playback.\n " +
                                    "Are you sure you want to clear?", "Clear",
                            JOptionPane.YES_NO_OPTION);
                    if (b == 0) {
                        model.clear();
                        model.notifyObservers();
                        model.notifyObservers2();
                    }
                }
            }
        });
        this.add(this.clear);

        this.add(this.opacity);
        this.add(this.opacityLevel);
        this.add(this.thickness);
        this.add(this.thicknessLevel);
        SliderListener sl = new SliderListener();
        opacityLevel.addChangeListener(sl);
        thicknessLevel.addChangeListener(sl);

        this.add(this.col);
        coll.setBackground(model.strokeColor);
        coll.setOpaque(true);
        this.add(this.coll);
    }

    private class SliderListener implements ChangeListener{
        public void stateChanged(ChangeEvent e) {
            if(e.getSource() == opacityLevel){
                model.opacityVal = (float) (opacityLevel.getValue() * .01);
            } else
            if (e.getSource() == thicknessLevel) {
                model.thickness = thicknessLevel.getValue();
            }
        }
    }


    public void update(Object observable) {
        coll.setBackground(this.model.strokeColor);
        repaint();
        //System.out.println("Model changed!");
    }
}

class Canvas extends JComponent implements Observer {

    private Model model;
    Shape aShape = null;

    Point drawStart, drawEnd;

    public Canvas(Model model)
    {
        this.model = model;
        model.addObserver(this);

        this.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                if(model.tool != 1) {
                    drawStart = new Point(e.getX(), e.getY());
                    drawEnd = drawStart;
                    repaint();
                }
            }

            public void mouseReleased(MouseEvent e) {
                if(model.tool != 1 && model.tool != 5){

                    if (model.tool == 2){
                        aShape = drawLine(drawStart.x, drawStart.y, e.getX(), e.getY());
                    } else

                    if (model.tool == 3){
                        aShape = drawEllipse(drawStart.x, drawStart.y, e.getX(), e.getY());
                    } else

                    if (model.tool == 4) {
                        aShape = drawRectangle(drawStart.x, drawStart.y, e.getX(), e.getY());
                    }

                    model.addShape(aShape);
                    ++model.shapeAmount;
                    model.drawAmount = model.shapeAmount;

                    drawStart = null;
                    drawEnd = null;

                }
            }
        } );

        this.addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent e)
            {
                if(model.tool == 1){

                    int x = e.getX();
                    int y = e.getY();

                    model.strokeColor = model.fillColor;

                    aShape = drawBrush(x,y,model.thickness, model.thickness);
                    model.addShape(aShape);
                    ++model.shapeAmount;
                    model.drawAmount = model.shapeAmount;

                }
                drawEnd = new Point(e.getX(), e.getY());
               /*if (currentAction == 1) {
                    Shape aShape = null;
                    aShape = drawLine(drawStart.x,drawStart.y,drawEnd.x,drawEnd.x);
                    shapes.add(aShape);
                    shapeFill.add(fillColor);
                    shapeStroke.add(strokeColor);
                    transPercent.add(transparentVal);
                }*/

                repaint();
            }
        } );
    }

    public void paint(Graphics g)
    {
        Graphics2D graphSettings = (Graphics2D)g;

        graphSettings.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphSettings.setStroke(new BasicStroke(4));

        Iterator<Color> strokeCounter = model.shapeStroke.iterator();
        Iterator<Color> fillCounter = model.shapeFill.iterator();
        Iterator<Float> transCounter = model.transPercent.iterator();

        graphSettings.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1.0f));

        int counter = 0;
        for(Shape s : model.shapes)
        {
            graphSettings.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, transCounter.next()));
            graphSettings.setPaint(strokeCounter.next());
            graphSettings.draw(s);
            graphSettings.setPaint(fillCounter.next());
            graphSettings.fill(s);

            ++counter;
            if (counter >= model.drawAmount) { break; }
        }

        if (drawStart != null && drawEnd != null)
        {

            graphSettings.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.40f));
            graphSettings.setPaint(Color.LIGHT_GRAY);
            Shape aShape = null;

            if (model.tool == 2){
                aShape = drawLine(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
            } else
            if (model.tool == 3){
                aShape = drawEllipse(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
            } else
            if (model.tool == 4) {
                aShape = drawRectangle(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
            }
            graphSettings.draw(aShape);
        }
    }

    private Rectangle2D.Float drawRectangle(
            int x1, int y1, int x2, int y2)
    {
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);

        int width = Math.abs(x1 - x2);
        int height = Math.abs(y1 - y2);

        return new Rectangle2D.Float(
                x, y, width, height);
    }

    private Ellipse2D.Float drawEllipse(
            int x1, int y1, int x2, int y2)
    {
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        int width = Math.abs(x1 - x2);
        int height = Math.abs(y1 - y2);

        return new Ellipse2D.Float(
                x, y, width, height);
    }
    private Line2D.Float drawLine(
            int x1, int y1, int x2, int y2)
    {

        return new Line2D.Float(
                x1, y1, x2, y2);
    }

    private Ellipse2D.Float drawBrush(
            int x1, int y1, int brushStrokeWidth, int brushStrokeHeight)
    {

        return new Ellipse2D.Float(
                x1, y1, brushStrokeWidth, brushStrokeHeight);

    }

    public void update(Object observable) {
        repaint();
        //System.out.println("Model changed!");
    }
}

class Playback extends JPanel implements Observer {

    private Model model;
    private JSlider timeSlider = new JSlider(0,100,100);

    Playback(Model model) {
        super();

        this.model = model;
        model.addObserver2(this);

        GridLayout strategy = new GridLayout(1,1);
        this.setLayout(strategy);
        this.add(this.timeSlider);
        SliderListener sl = new SliderListener();
        timeSlider.addChangeListener(sl);

    }

    private class SliderListener implements ChangeListener{
        public void stateChanged(ChangeEvent e) {
            if(e.getSource() == timeSlider){
                if(model.isEmpty()) { timeSlider.setEnabled(false); }
                int oldAmount = model.drawAmount;
                model.drawAmount = (model.shapeAmount * timeSlider.getValue())/100;
                for (int i = model.drawAmount; i < oldAmount; ++i) {
                    model.pop();
                }
                for (int i = oldAmount; i < model.drawAmount; ++i) {
                    model.unpop();
                }
                model.notifyObservers();
            }
        }
    }

    public void update(Object observable) {
        timeSlider.setValue(100);
        timeSlider.setEnabled(true);
    }
}

