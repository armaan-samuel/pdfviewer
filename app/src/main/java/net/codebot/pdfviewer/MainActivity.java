package net.codebot.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// PDF sample code from
// https://medium.com/@chahat.jain0/rendering-a-pdf-document-in-android-activity-fragment-using-pdfrenderer-442462cb8f9a
// Issues about cache etc. are not at all obvious from documentation, so we should expect people to need this.
// We may wish to provied them with this code.

public class MainActivity extends AppCompatActivity {

    final String LOGNAME = "pdf_viewer";
    final String FILENAME = "shannon1948.pdf";
    final int FILERESID = R.raw.shannon1948;

    // manage the pages of the PDF, see below
    PdfRenderer pdfRenderer;
    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer.Page currentPage;
    ImageButton pgdown;
    ImageButton pgup;
    ImageButton redo;
    ImageButton undo;
    ImageButton draw;
    ImageButton highlight;
    ImageButton erase;
    ImageButton pan;
    TextView pgnum;
    TextView pdfname;

    // custom ImageView class that captures strokes and draws them over the image
    PDFimage pageImage;

    //zoom stuff



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("Main Activity", "Trying Load");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Log.d("Main Activity", "Trying Load");

        LinearLayout layout = (LinearLayout) findViewById(R.id.pdfLayout);
        pageImage = new PDFimage(this);

        pageImage.setMinimumWidth(1500);
        pageImage.setMinimumHeight(3000);
        layout.addView(pageImage);
        layout.setEnabled(true);

        pgdown = (ImageButton) findViewById(R.id.pgdown);
        pgdown.setScaleType(ImageView.ScaleType.FIT_XY);
        pgup = (ImageButton) findViewById(R.id.pgup);
        pgup.setScaleType(ImageView.ScaleType.FIT_XY);
        undo = (ImageButton) findViewById(R.id.undo);
        undo.setScaleType(ImageView.ScaleType.FIT_XY);
        redo = (ImageButton) findViewById(R.id.redo);
        redo.setScaleType(ImageView.ScaleType.FIT_XY);
        draw = (ImageButton) findViewById(R.id.draw);
        draw.setScaleType(ImageView.ScaleType.FIT_XY);
        highlight = (ImageButton) findViewById(R.id.highlight);
        highlight.setScaleType(ImageView.ScaleType.FIT_XY);
        erase = (ImageButton) findViewById(R.id.erase);
        erase.setScaleType(ImageView.ScaleType.FIT_XY);
        pan = (ImageButton) findViewById(R.id.pan);
        pan.setScaleType(ImageView.ScaleType.FIT_XY);

        pgdown.setImageResource(R.drawable.left_arrow);
        pgup.setImageResource(R.drawable.right_arrow);
        undo.setImageResource(R.drawable.undo);
        redo.setImageResource(R.drawable.redo);
        draw.setImageResource(R.drawable.pen);
        highlight.setImageResource(R.drawable.highlight);
        erase.setImageResource(R.drawable.erase);
        pan.setImageResource(R.drawable.pan);

        pgnum = (TextView) findViewById(R.id.pgnum);
        pdfname = (TextView) findViewById(R.id.pdfname);
        pdfname.setText(FILENAME);




//        try {
//            Model.getInstance().load(this);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        // open page 0 of the PDF
//        // it will be displayed as an image in the pageImage (above)
//        try {
//            openRenderer(this);
//            Model.getInstance().initialize(pdfRenderer.getPageCount());
//            showPage(Model.getInstance().getCurPage());
//            if (Model.getInstance().getCurPage() == 0){
//                pgdown.setEnabled(false);
//            }
//            if (Model.getInstance().getCurPage() == pdfRenderer.getPageCount() - 1){
//                pgup.setEnabled(false);
//
//            }
//            //closeRenderer();
//        } catch (IOException exception) {
//            Log.d(LOGNAME, "Error opening PDF");
//        }
        //showPage(1);



        pgdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Model.getInstance().pageDown();
                if (Model.getInstance().getCurPage() == 0){
                    pgdown.setEnabled(false);
                }
                showPage(Model.getInstance().getCurPage());
                pgup.setEnabled(true);
                pageImage.invalidate();
            }
        });
        pgup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Model.getInstance().pageUp();
                if (Model.getInstance().getCurPage() == pdfRenderer.getPageCount() - 1){
                    pgup.setEnabled(false);
                }
                pgdown.setEnabled(true);
                showPage(Model.getInstance().getCurPage());
                pageImage.invalidate();
            }
        });
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage(Model.getInstance().undoTransition());
                pageImage.invalidate();
            }
        });
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage(Model.getInstance().redoTransition());
                pageImage.invalidate();
            }
        });
        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageImage.setType("DRAW");
            }
        });
        highlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageImage.setType("HL");
            }
        });
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageImage.setType("ERASE");
            }
        });
        pan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageImage.setType("PAN");
            }
        });



    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        try {
//            Model.getInstance().save(this);
//            //closeRenderer();
//        } catch (IOException e) {
//            Log.e("SAVE", e.toString());
//            e.printStackTrace();
//        }
//    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        Log.d("RESTORE","ONRESTORE");
//        try {
//            Model.getInstance().load(this);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            openRenderer(this);
//            Model.getInstance().initialize(pdfRenderer.getPageCount());
//            showPage(Model.getInstance().getCurPage());
//            if (Model.getInstance().getCurPage() == 0){
//                pgdown.setEnabled(false);
//            }
//            if (Model.getInstance().getCurPage() == pdfRenderer.getPageCount() - 1){
//                pgup.setEnabled(false);
//
//            }
//            //closeRenderer();
//        } catch (IOException exception) {
//            Log.d(LOGNAME, "Error opening PDF");
//        }
//    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {

        super.onResume();
        Log.d("ONRESUME", "Trying Load");
        try {
            Model.getInstance().load(this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            openRenderer(this);
            Model.getInstance().initialize(pdfRenderer.getPageCount());
            showPage(Model.getInstance().getCurPage());
            if (Model.getInstance().getCurPage() == 0){
                pgdown.setEnabled(false);
            }
            if (Model.getInstance().getCurPage() == pdfRenderer.getPageCount() - 1){
                pgup.setEnabled(false);

            }
            //closeRenderer();
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error opening PDF");
        }
        pageImage.invalidate();
    }

    @Override
    protected void onPause() {

        super.onPause();
        try {
            Model.getInstance().save(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onStop() {
//        super.onStop();
//        try {
//            closeRenderer();
//        } catch (IOException ex) {
//            Log.d(LOGNAME, "Unable to close PDF renderer");
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
            // pdfRenderer cannot handle the resource directly,
            // so extract it into the local cache directory.
            InputStream asset = this.getResources().openRawResource(FILERESID);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

        // capture PDF data
        // all this just to get a handle to the actual PDF representation
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    // do this before you quit!
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeRenderer() throws IOException {
        Log.d("PDF", "CLOSE RENDER");
        if (null != currentPage) {
            currentPage.close();
        }
        pdfRenderer.close();
        parcelFileDescriptor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        // Close the current page before opening another one.
        if (null != currentPage) {
            currentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);

        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        pgnum.setText(String.valueOf(Model.getInstance().getCurPage() + 1) +"/" + String.valueOf(pdfRenderer.getPageCount()));


        // Display the page
        pageImage.setImage(bitmap);
        pageImage.invalidate();
    }


}
