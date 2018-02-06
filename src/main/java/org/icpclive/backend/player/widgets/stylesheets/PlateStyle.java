package org.icpclive.backend.player.widgets.stylesheets;

import java.awt.*;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class PlateStyle {
    public Color background;
    public Color text;
    public org.icpclive.backend.graphics.Graphics.RectangleType rectangleType;
    public org.icpclive.backend.graphics.Graphics.Alignment alignment;

    public PlateStyle(Color background, Color text, org.icpclive.backend.graphics.Graphics.RectangleType rectangleType, org.icpclive.backend.graphics.Graphics.Alignment alignment) {
        this.background = background;
        this.text = text;
        this.rectangleType = rectangleType;
        this.alignment = alignment;
    }

    public PlateStyle brighter() {
        if (background.equals(background.brighter())) {
            return new PlateStyle(background.darker(), text, rectangleType, alignment);
        }
        return new PlateStyle(background.brighter(), text, rectangleType, alignment);
    }

    public PlateStyle darker() {
        if (background.equals(background.darker())) {
            return new PlateStyle(background.brighter(), text, rectangleType, alignment);
        }
        return new PlateStyle(background.darker(), text, rectangleType, alignment);
    }

    public PlateStyle(String styleName) {
        background = Color.decode(Stylesheet.styles.getOrDefault(styleName + ".background", "#000000"));
        text = Color.decode(Stylesheet.styles.getOrDefault(styleName + ".text", "#FFFFFF"));
        rectangleType = org.icpclive.backend.graphics.Graphics.RectangleType.SOLID;
//        rectangleType = Graphics.RectangleType.valueOf(Stylesheet.styles.getOrDefault(styleName + ".style", "SOLID_ROUNDED").toUpperCase());
        alignment = org.icpclive.backend.graphics.Graphics.Alignment.valueOf(Stylesheet.styles.getOrDefault(styleName + ".align", "Left").toUpperCase());
    }


}