package chess.pieces;

import chess.ChessBoard;
import chess.ChessConstants;
import chess.ChessPiece;

import java.util.Arrays;
import java.util.List;

public class Pawn extends ChessPiece {
    public Pawn(int row, int col, boolean white, ChessBoard board) {
        super(row, col, white, board, white ? "\u2659" : "\u265F");
        this.value = ChessConstants.PAWN_VALUE;
    }

    private boolean isStartingPosition() {
        return this.isWhite()
                ? this.getPosition().row() == ChessConstants.WHITE_PAWN_START_COL
                : this.getPosition().row() == ChessConstants.BLACK_PAWN_START_COL;
    }

    private final int forward = isWhite() ? -1 : 1;

    private ChessBoard.Position[] possiblePawnMoves() {
        return new ChessBoard.Position[] {
                new ChessBoard.Position(forward + getRow(), getCol()),
                new ChessBoard.Position(forward + getRow(), getCol() + 1),
                new ChessBoard.Position(forward + getRow(), getCol() - 1),
                new ChessBoard.Position((forward * 2) + getRow(), getCol())
        };
    }

    @Override
    public List<ChessBoard.Position> getLegalMoves() {
        return Arrays.stream(possiblePawnMoves()).filter(this::isLegalMove).toList();
    }

    @Override
    public boolean isLegalMove(ChessBoard.Position newPosition) {
        if (!newPosition.isOnBoard()) return false;

        boolean res = false;
        ChessPiece target = getBoard().getPiece(newPosition);
        int rowOff = (newPosition.row() - this.getRow()) * forward;
        int colOff = Math.abs((newPosition.col() - this.getCol()));

        if (colOff == 1 && rowOff == 1) {
            // can capture
            if (target != null) {
                res = target.isWhite() != this.isWhite();
            } else if (getBoard().hasEnPassantSquare()) {
                res = newPosition.equals(getBoard().getEnPassantSquare());
            }
        } else if (colOff == 0) {
            // single move
            if (rowOff == 1) res = target == null;

            // can move two squares
            if (rowOff == 2 && isStartingPosition()) {
                res = (getBoard().isEmpty(getRow() + forward, getCol()) &&
                            getBoard().isEmpty(newPosition));
            }
        }
        return res && super.isLegalMovePawnWorkaround(newPosition);

    }

    @Override
    public String toString() {
        return isWhite() ? "P" : "p";
    }
}