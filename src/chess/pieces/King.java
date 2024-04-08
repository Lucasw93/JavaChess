package chess.pieces;

import chess.ChessBoard;
import chess.ChessPiece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class King extends ChessPiece {
    public King(int row, int col, boolean white, ChessBoard board) {
        super(row, col, white, board, white ? "\u2654" : "\u265A");
    }

    private ChessBoard.Position[] possibleKingMoves() {
        return new ChessBoard.Position[] {
                new ChessBoard.Position(getRow() + 1, getCol() + 1),
                new ChessBoard.Position(getRow() - 1, getCol() - 1),
                new ChessBoard.Position(getRow() + 1, getCol() - 1),
                new ChessBoard.Position(getRow() - 1, getCol() + 1),
                new ChessBoard.Position(getRow() + 1, getCol()),
                new ChessBoard.Position(getRow() - 1, getCol()),
                new ChessBoard.Position(getRow(), getCol() + 1),
                new ChessBoard.Position(getRow(), getCol() - 1),
        };
    }

    @Override
    public List<ChessBoard.Position> getLegalMoves() {
        return Arrays.stream(possibleKingMoves()).filter(super::isLegalMove).toList();
    }

    @Override
    public boolean isLegalMove(ChessBoard.Position newPosition) {
        if (!super.isLegalMove(newPosition)) return false;

        int rowOff = Math.abs(this.getRow() - newPosition.row());
        int colOff = Math.abs(this.getCol() - newPosition.col());

        return (rowOff + colOff == 1) || (rowOff == 1 && colOff == 1);
    }

    @Override
    public String toString() {
        return isWhite() ? "K" : "k";
    }
}
