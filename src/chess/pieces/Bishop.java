package chess.pieces;

import chess.ChessBoard;
import chess.ChessConstants;
import chess.ChessPiece;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends ChessPiece {
    public Bishop(int row, int col, boolean white, ChessBoard board) {
        super(row, col, white, board, white ? "\u2657" : "\u265D");
        this.value = ChessConstants.BISHOP_VALUE;
    }

    @Override
    public List<ChessBoard.Position> getLegalMoves() {
        List<ChessBoard.Position> ret = new ArrayList<>();

        addDiagonalMoves(getPosition(), ret);
        return ret;
    }

    @Override
    public boolean isLegalMove(ChessBoard.Position newPosition) {
        if (!newPosition.isOnBoard()) return false;

        if (!getBoard().isEmpty(newPosition)
                && this.isSameColor(getBoard().getPiece(newPosition))) return false;

        if (!newPosition.onDiagonal(this.getPosition())) return false;

        return !isBlocked(newPosition);
    }
}
