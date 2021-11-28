package net.codebot.pdfviewer;

import android.graphics.Point;

import java.io.IOException;
import java.io.Serializable;

public class Mpoint extends Point implements Serializable {

    public Mpoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {

        out.writeInt(x);
        out.writeInt(y);


    }
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        x = in.readInt();
        y = in.readInt();
    }
}
