import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.*;

public class Model {

    float opacityVal = 1.0f;
    int thickness = 5;
    Color defaultColor;
    Color strokeColor = Color.BLACK;
    Color fillColor = Color.BLACK;
    int tool = 1;
    int shapeAmount = 0;
    int drawAmount = 0;
    boolean erase = false;

    ArrayList<Shape> shapes = new ArrayList<Shape>();
    ArrayList<Color> shapeFill = new ArrayList<Color>();
    ArrayList<Color> shapeStroke = new ArrayList<Color>();
    ArrayList<Float> transPercent = new ArrayList<Float>();

    ArrayList<Shape> shapes2 = new ArrayList<Shape>();
    ArrayList<Color> shapeFill2 = new ArrayList<Color>();
    ArrayList<Color> shapeStroke2 = new ArrayList<Color>();
    ArrayList<Float> transPercent2 = new ArrayList<Float>();

    private ArrayList<Observer> observers;
    private ArrayList<Observer> observers2;

    /**
     * Create a new model.
     */
    public Model() {
        this.observers = new ArrayList();
        this.observers2 = new ArrayList();
    }

    /**
     * Add an observer to be notified when this model changes.
     */
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }
    public void addObserver2(Observer observer) {
        this.observers2.add(observer);
    }

    public void addShape(Shape s) {
        this.shapes.add(s);
        if (erase) {
            this.shapeFill.add(defaultColor);
            this.shapeStroke.add(defaultColor);
        } else {
            this.shapeFill.add(fillColor);
            this.shapeStroke.add(strokeColor);
        }
        this.transPercent.add(opacityVal);
        this.deleteRemoved();
        shapeAmount = drawAmount;
        notifyObservers();
        notifyObservers2();
    }

    public void pop() {
        if(!shapes.isEmpty()) {
            shapes2.add(shapes.remove(shapes.size() - 1));
            shapeFill2.add(shapeFill.remove(shapeFill.size() - 1));
            shapeStroke2.add(shapeStroke.remove(shapeStroke.size() - 1));
            transPercent2.add(transPercent.remove(transPercent.size() - 1));
        }
    }

    public void unpop() {
        if (!shapes2.isEmpty()) {
            shapes.add(shapes2.remove(shapes2.size() - 1));
            shapeFill.add(shapeFill2.remove(shapeFill2.size() - 1));
            shapeStroke.add(shapeStroke2.remove(shapeStroke2.size() - 1));
            transPercent.add(transPercent2.remove(transPercent2.size() - 1));
        }
    }

    public void deleteRemoved() {
        shapes2.clear();
        shapeFill2.clear();
        shapeStroke2.clear();
        transPercent2.clear();
    }

    public void clear() {
        shapes.clear();
        shapeFill.clear();
        shapeStroke.clear();
        transPercent.clear();
        shapeAmount = 0;
        drawAmount = 0;
    }

    public boolean isEmpty() {
        return (shapes.isEmpty() && shapes2.isEmpty());
    }

    public void load(data d) {
        this.clear();
        shapes.addAll(d.shapes);
        shapeFill.addAll(d.shapeFill);
        shapeStroke.addAll(d.shapeStroke);
        transPercent.addAll(d.transPercent);
        drawAmount = d.shapes.size();
        shapeAmount = drawAmount;
    }

    /**
     * Remove an observer from this model.
     */
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }
    public void removeObserver2(Observer observer) {
        this.observers2.remove(observer);
    }

    /**
     * Notify all observers that the model has changed.
     */
    public void notifyObservers() {
        for (Observer observer: this.observers) {
            observer.update(this);
        }
    }

    public void notifyObservers2() {
        for (Observer observer: this.observers2) {
            observer.update(this);
        }
    }
}

class data implements java.io.Serializable {

    ArrayList<Shape> shapes = new ArrayList<Shape>();
    ArrayList<Color> shapeFill = new ArrayList<Color>();
    ArrayList<Color> shapeStroke = new ArrayList<Color>();
    ArrayList<Float> transPercent = new ArrayList<Float>();

    public data(Model m) {
        shapes.addAll(m.shapes);
        shapeFill.addAll(m.shapeFill);
        shapeStroke.addAll(m.shapeStroke);
        transPercent.addAll(m.transPercent);
    }

    public void serializeData(String s) {
        try
        {
            FileOutputStream myFileOutputStream = new FileOutputStream(s + ".ser");
            ObjectOutputStream myObjectOutputStream = new ObjectOutputStream(myFileOutputStream);
            myObjectOutputStream.writeObject(this);
            myObjectOutputStream.close();
        }
        catch (Exception e)
        {
            //Log.e("Error when saving to file.",Log.getStackTraceString(e));
            System.out.println("Error serializing data ");
        }
    }

    /*
    public void deserializeData(File myFile) {
        try
        {
            FileInputStream myFileInputStream = new FileInputStream(myFile);
            ObjectInputStream myObjectInputStream = new ObjectInputStream(myFileInputStream);
            this = (data) myObjectInputStream.readObject();
            myObjectInputStream.close();
        }
        catch (Exception e)
        {
            System.out.println("Error deserializing data ");
            //Log.e("Error when loading from file.",Log.getStackTraceString(e));
        }

    }
    */

}

