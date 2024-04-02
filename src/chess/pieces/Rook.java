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
        if (!isLegalMoveCommon(newPosition)) return false;

        int direction;
        int curCol = this.getCol();
        int curRow = this.getRow();

        if (newPosition.sameCol(this.getPosition())) {
            direction = Integer.compare(newPosition.row(), this.getRow());

            while ((curRow += direction) != newPosition.row()) {
                if (!getBoard().isEmpty(curRow, curCol)) return false;
            }
            return true;
        }else if (newPosition.sameRow(this.getPosition())) {
            direction = Integer.compare(newPosition.col(), this.getCol());

            while ((curCol += direction) != newPosition.col()) {
                if (!getBoard().isEmpty(curRow, curCol)) return false;
            }
            return true;
        }
        return false;
    }
}
