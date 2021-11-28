package net.codebot.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

public class Model implements Serializable {
    static Model instance = new Model();

    Stack<Mpath> transitions = new Stack<>();
    Stack<Mpath> undo = new Stack<>();
    //ArrayList<Path> paths [];// = new ArrayList<>();

    int curPage = 0;

    public static Model getInstance(){
        return  instance;
    }

    public void addTransition(Mpath p){
        //paths[curPage].add(p);
        p.setPageNum(curPage);
        transitions.add(p);
        undo.clear();
    }

    public int undoTransition(){

        if (!transitions.empty()){
            Mpath p = transitions.pop();
            undo.push(p);
            curPage = p.getPageNum();
            if (p.getType().equals("ERASE")){
                transitions.get(p.getErased()).setVisible(true);
            }
            return p.getPageNum();
        }
        return curPage;

        //paths[curPage].remove(paths[curPage].size()-1);

    }

    public int redoTransition(){

        if (!undo.empty()){
            Mpath p = undo.pop();
            transitions.push(p);
            curPage = p.getPageNum();
            if (p.getType().equals("ERASE")){
                transitions.get(p.getErased()).setVisible(false);
            }
            return p.getPageNum();
        }
        return curPage;

        //paths[curPage].add(p);

    }

    public ArrayList<Mpath> getPaths() {
        ArrayList<Mpath> out = new ArrayList<>();
        for (Mpath mp : transitions){
            if (mp.getPageNum() == curPage && mp.isVisible()){
                out.add(mp);
            }
        }
        return out;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int s){
        curPage = s;
    }

    public void initialize(int pageCount){
//        paths = new ArrayList [pageCount];
//        for (int i = 0; i < pageCount; i++){
//            paths[i] = new ArrayList<>();
//        }
    }

    public void pageUp(){
        curPage++;
    }
    public void pageDown(){
        curPage--;
        if (curPage < 0) curPage = 0;
    }

    public void check(Mpoint p){
        //for (Mpath mp : transitions){
         for (int i = 0; i < transitions.size(); i++)   {
             Mpath mp = transitions.get(i);
            if (!mp.getType().equals("ERASE") && mp.isVisible() && mp.contains(p)){
                mp.setVisible(false);
                Mpath e = new Mpath();
                e.setType("ERASE");
                e.setVisible(false);
                e.setErased(i);
                addTransition(e);
                e.setPageNum(mp.getPageNum());
                break;
            }
        }
    }

//    public void applyScale(float f){
//        for (Mpath mp:transitions){
//            if (!mp.getType().equals("ERASE")){
//                mp.applyScale(f);
//            }
//        }
//    }

    public void save(Context context) throws IOException {
        Log.d("Save", "Saved Called");
        Log.d("SAVE-PAGENUM", String.valueOf(instance.getCurPage()));
        Log.d("SAVE_NUMTRANS", String.valueOf(instance.transitions.size()));
        FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), "data.txt"));
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(instance);
        os.close();
        fos.close();


    }
    public void load(Context context) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(new File(context.getFilesDir(), "data.txt"));
        ObjectInputStream is = new ObjectInputStream(fis);
        instance = (Model) is.readObject();
        is.close();
        fis.close();
        Log.d("Load", "Load Called");
        Log.d("LOAD-PAGENUM", String.valueOf(instance.getCurPage()));
        Log.d("LOAD_NUMTRANS", String.valueOf(instance.transitions.size()));


    }

//    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//        out.writeInt(curPage);
//        out.writeObject(transitions);
//        out.writeObject(undo);
//
//
//    }
//    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
//        instance.curPage = in.readInt();
//        //instance.transitions = (Stack<Mpath>) in.readObject();
//        if (instance.transitions == null) instance.transitions = new Stack<>();
//        instance.undo = (Stack<Mpath>) in.readObject();
//
//    }
}
