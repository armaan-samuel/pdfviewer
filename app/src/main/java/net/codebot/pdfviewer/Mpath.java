package net.codebot.pdfviewer;

import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;
//import java.awt.geom.Line2D;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Mpath extends Path implements Serializable {

    private String type;
    private int pageNum;
    //private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Mpoint> points = new ArrayList<>();

    private boolean visible = true;
    private int erased = -1;

    public Mpath() {
        super();
    }

    public void setType(String t) {
        type = t;
    }

    public String getType() {
        return type;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void addPoint(Mpoint p) {
        points.add(p);
    }

    public boolean contains(Mpoint p) {

        for (int i = 0; i < points.size() - 2; i++) {
            double d = distToSeg(p, points.get(i), points.get(i + 1));
            Log.d("DIST", String.valueOf(d));
            if (d <= 80) {

                return true;
            }
        }
        return false;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    private int dist(Mpoint v, Mpoint w) {

        int dx = w.x - v.x;
        int dy = w.y - v.y;

        return dx * dx + dy * dy;
    }

    private double distToSeg(Mpoint p, Mpoint v, Mpoint w) {

        int lengthSquared = dist(v, w);
        if (lengthSquared == 0) return Math.sqrt(dist(p, v));

        int dx = w.x - v.x;
        int dy = w.y - v.y;

        double t = (double) ((p.x - v.x) * dx + (p.y - v.y) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        return Math.sqrt(dist(p, new Mpoint(v.x + (int) (t * dx), v.y + (int) (t * dy))));
    }

    public int getNumPoints() {
        return points.size();
    }

    public void setErased(int e) {
        erased = e;
    }

    public int getErased() {
        return erased;
    }

    public void applyScale(float f) {
        for (Point p : points) {
            p.x = (int) (p.x * f);
            p.y = (int) (p.y * f);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(type);
        out.writeInt(pageNum);
        out.writeInt(erased);
        out.writeBoolean(visible);
        out.writeObject(points);
//        out.writeInt(curPage);
//        out.writeObject(transitions);
//        out.writeObject(undo);
    }


    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        type = (String) in.readObject();
        pageNum = in.readInt();
        erased = in.readInt();
        visible = in.readBoolean();
        points = (ArrayList<Mpoint>) in.readObject();
        if (points.size() > 0){
            moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size() - 1; i++){
                lineTo(points.get(i).x, points.get(i).y);
            }
        }

        Log.d("NUMPOINTS", String.valueOf(points.size()));
        //        instance.curPage = in.readInt();
//        //instance.transitions = (Stack<Mpath>) in.readObject();
//        if (instance.transitions == null) instance.transitions = new Stack<>();
//        instance.undo = (Stack<Mpath>) in.readObject();
    }
}
