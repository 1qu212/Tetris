package cn.xydzjnq.tetris.piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PieceFatory {
    public static Piece createPiece() {
        List<Piece> pieceList = new ArrayList<>();
        pieceList.add(new IPiece());
        pieceList.add(new JPiece());
        pieceList.add(new LPiece());
        pieceList.add(new OPiece());
        pieceList.add(new SPiece());
        pieceList.add(new TPiece());
        pieceList.add(new ZPiece());
        Random random = new Random();
        int randomInt = random.nextInt(6);
        return pieceList.get(randomInt);
    }

    public static Piece createPiece(String shape, int state) {
        Piece piece;
        switch (shape) {
            default:
                piece = new IPiece();
                break;
            case "I":
                piece = new IPiece();
                break;
            case "J":
                piece = new JPiece();
                break;
            case "L":
                piece = new LPiece();
                break;
            case "O":
                piece = new OPiece();
                break;
            case "S":
                piece = new SPiece();
                break;
            case "T":
                piece = new TPiece();
                break;
            case "Z":
                piece = new ZPiece();
                break;
        }
        piece.setState(state);
        return piece;
    }
}
