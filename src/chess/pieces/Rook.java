package chess.pieces;

import chess.ChessBoard;
import chess.ChessConstants;
import chess.ChessPiece;

import java.util.ArrayList;
import java.util.List;

public class Rook extends ChessPiece {
    public Rook(int row, int col, boolean white, ChessBoard board) {
        super(row, col, white, board, white ? "\u2656" : "\u265C");
        this.value = ChessConstants.ROOK_VALUE;
    }


    @Override
    public List<ChessBoard.Position> getLegalMoves() {
        List<ChessBoard.Position> ret = new ArrayList<>();

        addLineMoves(getPosition(), ret);
        return ret;
    }

    @Override
    public boolean isLegalMove(ChessBoard.Position newPosition) {
        return super.isLegalMove(newPosition) &&
                getPosition().sameRowOrCol(newPosition) &&
                !isBlocked(newPosition);
    }

    @Override
    public String toString() {
        return isWhite() ? "R" : "r";
    }
}
