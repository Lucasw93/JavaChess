package chess.pieces;

import chess.ChessBoard;
import chess.ChessConstants;
import chess.ChessPiece;

import java.util.Arrays;
import java.util.List;

public class Knight extends ChessPiece {
    public Knight(int row, int col, boolean white, ChessBoard board) {
        super(row, col, white, board, white ? "\u2658" : "\u265E");
        this.value = ChessConstants.KNIGHT_VALUE;
    }

     private ChessBoard.Position[] possibleKnightMoves() {
        return new ChessBoard.Position[] {
                 new ChessBoard.Position(getRow() + 2, getCol() + 1),
                 new ChessBoard.Position(getRow() - 2, getCol() - 1),
                 new ChessBoard.Position(getRow() + 2, getCol() - 1),
                 new ChessBoard.Position(getRow() - 2, getCol() + 1),
                 new ChessBoard.Position(getRow() + 1, getCol() + 2),
                 new ChessBoard.Position(getRow() - 1, getCol() - 2),
                 new ChessBoard.Position(getRow() + 1, getCol() - 2),
                 new ChessBoard.Position(getRow() - 1, getCol() + 2)
         };
     }

    @Override
    public List<ChessBoard.Position> getLegalMoves() {
        return Arrays.stream(possibleKnightMoves()).filter(this::isLegalMoveCommon).toList();
    }

    @Override
    public boolean isLegalMove(ChessBoard.Position newPosition) {
        if (!isLegalMoveCommon(newPosition)) return false;

        int rowOff = Math.abs(this.getRow() - newPosition.row());
        int colOff = Math.abs(this.getCol() - newPosition.col());

        return rowOff * colOff == 2;
    }
}