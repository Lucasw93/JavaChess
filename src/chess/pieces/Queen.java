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
        if (!isLegalMoveCommon(newPosition)) return false;

        if (!(newPosition.onDiagonal(this.getPosition())
                || newPosition.sameRowOrCol(this.getPosition()))) return false;

        int dRow = Integer.compare(newPosition.row(), this.getRow());
        int dCol = Integer.compare(newPosition.col(), this.getCol());

        int curRow = this.getRow() + dRow;
        int curCol = this.getCol() + dCol;

        while (!newPosition.equals(curRow, curCol)) {
            if (getBoard().getPiece(curRow, curCol) != null) return false;
            curRow += dRow;
            curCol += dCol;
        }
        return true;
    }
}