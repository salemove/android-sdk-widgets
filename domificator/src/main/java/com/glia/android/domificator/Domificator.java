package com.glia.android.domificator;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hp.gagawa.java.Document;
import com.hp.gagawa.java.DocumentType;
import com.hp.gagawa.java.FertileNode;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Img;
import com.hp.gagawa.java.elements.Style;

import java.util.concurrent.locks.ReentrantReadWriteLock;

// TODO: name to be decided
public final class Domificator {

    private static final String IMAGE_PLACEHOLDER_SRC = "https://www.svgrepo.com/show/354935/document-missing.svg";
    private final ReentrantReadWriteLock mWindowsLock = new ReentrantReadWriteLock(); // TODO: check details regarding this

    public Domificator() {
    }

    public String generateDomFromActivity(Activity activity) {
        if (activity == null) {
            return null;
        }

        View rootView = activity.getWindow().getDecorView().getRootView();
        if (rootView == null) {
            throw new NullPointerException("This activity does not has an UI set???"); // TODO: check if it is a possible case 
        }

        MStyle bodyStyle = new MStyle()
                .add("border", "1px solid")
                .add("margin", "0")
                .add("overflow", "hidden") // TODO: This line seems to have no effect
                .add("width", rootView.getWidth() + "px")
                .add("height", rootView.getHeight() + "px");

        Document activityDom = new Document(DocumentType.HTMLStrict);
        activityDom.head.appendChild(new Style("text/css")
                        .appendText("body {" + bodyStyle.toString() + "}"));
        activityDom.body.appendChild(generateDomForView(rootView));
        return activityDom.write();
    }

    /**
     * Div div = new Div();
     * div.setId("mydiv").setCSSClass("myclass");
     * <p>
     * A link = new A();
     * link.setHref("http://www.example.com").setTarget("_blank");
     * <p>
     * div.appendChild( link );
     * <p>
     * Img image = new Img( "some alt", "some-image.png" );
     * image.setCSSClass( "frame" ).setId( "myimageid" );
     * link.appendChild( image );
     * <p>
     * System.out.print( div.write() );
     */

    public FertileNode generateDomForView(View view) {
        if (view == null) {
            return null;
        }

        if (view.getVisibility() == View.GONE || view.getVisibility() == View.INVISIBLE) {
            return new Div();
        }

        Div div = new Div();
        MStyle style = new MStyle();
        style.addViewMetrics(view);

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = ((ViewGroup) view);
            if (view instanceof ScrollView) {
                // TODO
            }
            if (view instanceof LinearLayout) {
                if (((LinearLayout) view).getOrientation() == LinearLayout.VERTICAL) {
                    style.add("margin-left", "auto");
                    style.add("margin-right", "auto");
                } else {
                    // TODO
                }
            }
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                div.appendChild(generateDomForView(viewGroup.getChildAt(i)));
            }
//        } else if (view instanceof EditText) { // Should be before TextView to prevent it being always TextView
            // TODO
//        } else if (view instanceof Button) { // Should be before TextView to prevent it being always TextView
            // TODO
        } else if (view instanceof TextView) {
            int gravity = Gravity.getAbsoluteGravity(((TextView) view).getGravity(), view.getLayoutDirection());

            div.appendText(((TextView) view).getText().toString());
            style.addColor(((TextView) view).getCurrentTextColor());
            style.addTextAlign(gravity);
            style.addTextSize(((TextView) view).getTextSize());
        } else if (view instanceof ImageView) {
            Img image = new Img("Image placeholder", IMAGE_PLACEHOLDER_SRC);
            image.setStyle(new MStyle()
                    .add("object-fit", "fill")
                    .add("width", view.getWidth() + "px")
                    .add("height", view.getHeight() + "px")
                    .toString()
            );
            div.appendChild(image);

        }

        // TODO: Web view
        // TODO: ImageButton view
        div.setStyle(style.toString());
        return div;
    }
}
