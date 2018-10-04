package com.coolioasjulio.chess.ui;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.coolioasjulio.chess.Piece;

public class PieceImage {

    private static HashMap<String, Image> pieceImages = new HashMap<>();

    /**
     * Loads the piece image from disk and cache it in memory. If it's already been
     * loaded from disk, return from memory.
     * 
     * This operation requires a specific file system. White image files are stored
     * in a folder called, white, and the same for black. Each piece image must
     * support transparency (png) and should be 2 letters long. The first letter is
     * the color of the piece. ("w" or "b") The second letter is the name of the
     * piece. k for King, n for Knight, q for Queen, r for Rook, b for Bishop, p for
     * Pawn.
     * 
     * @param p        The piece to load the image of
     * @param tileSize The width and height of the tiles
     * @return An Image, scaled to tileSizextileSize
     * @throws IOException
     */
    public static Image getImage(Piece p, int tileSize) throws IOException {
        String name = p.getName();
        String folder = "";
        if (p.getTeam() == Piece.WHITE) {
            name = "w" + name;
            folder = "white/";
        } else {
            name = "b" + name;
            folder = "black/";
        }

        if (pieceImages.containsKey(name)) {
            return pieceImages.get(name);
        }

        Image img = ImageIO.read(new File(folder + name + ".png")).getScaledInstance(tileSize, tileSize,
                Image.SCALE_SMOOTH);
        pieceImages.put(name, img);
        return img;
    }
}
