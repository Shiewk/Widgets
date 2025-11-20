package de.shiewk.widgets;

import org.joml.Vector2i;

public enum Anchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    public int getAlignStartPosX(int scaledScreenWidth){
        return switch (this){
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0;
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> scaledScreenWidth / 2;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> scaledScreenWidth;
        };
    }

    public int getAlignStartPosY(int scaledScreenHeight){
        return switch (this){
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0;
            case CENTER_LEFT, CENTER, CENTER_RIGHT -> scaledScreenHeight / 2;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> scaledScreenHeight;
        };
    }

    public Vector2i getTopLeft(int scaledScreenWidth, int scaledScreenHeight){
        return new Vector2i(
                // X component
                switch (this){
                    case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0;
                    case TOP_CENTER, CENTER, BOTTOM_CENTER -> scaledScreenWidth / 3;
                    case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> (int) (scaledScreenWidth / 3d * 2d);
                },
                // Y component
                switch (this){
                    case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0;
                    case CENTER_LEFT, CENTER, CENTER_RIGHT -> scaledScreenHeight / 3;
                    case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> (int) (scaledScreenHeight / 3d * 2d);
                }
        );
    }

    public static Anchor getAnchor(int scaledScreenWidth, int scaledScreenHeight, int posX, int posY){
        for (Anchor anchor : values()) {
            Vector2i topLeft = anchor.getTopLeft(scaledScreenWidth, scaledScreenHeight);
            if (
                    topLeft.x <= posX && topLeft.x + scaledScreenWidth / 3 >= posX &&
                    topLeft.y <= posY && topLeft.y + scaledScreenHeight / 3 >= posY
            ){
                return anchor;
            }
        }
        return Anchor.TOP_LEFT;
    }
}
