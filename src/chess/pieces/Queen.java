package chess.pieces;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessConstants;

import java.util.ArrayList;
import java.util.List;

public class Queen extends ChessPiece {
    public Queen(int row, int col, boolean white, ChessBoard board) {
        super(row, col, white, board, white ? "\u2655" : "\u265B");
        this.value = ChessConstants.QUEEN_VALUE;
    }


    @Override
    public List<ChessBoard.Position> getLegalMoves() {
        List<ChessBoard.Position> ret = new ArrayList<>();

        addLineMoves(getPosition(), ret);
        addDiagonalMoves(getPosition(), ret);
        return ret;
    }

    @Override
    public boolean isLegalMove(ChessBoard.Position newPosition) {
        if (!(
            isLegalMoveCommon(newPosition) &&
            (this.getPosition().onDiagonal(newPosition) ||
                    this.getPosition().sameRowOrCol(newPosition))
        )) return false;

        return !isBlocked(newPosition);
    }
}