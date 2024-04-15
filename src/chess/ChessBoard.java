package chess;

import chess.pieces.*;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Stream;

public class ChessBoard {
    private final ChessPiece[][] board;

    private Position[] enPassantSquare;

    private ChessBoard.Position bKingPosition;
    private ChessBoard.Position wKingPosition;

    private boolean cloned = false;

    public ChessBoard() {
        this.board = new ChessPiece[ChessConstants.MAX_ROW][ChessConstants.MAX_COL];
        setup();
    }

    // copy constructor
    public ChessBoard(ChessPiece[][] board, ChessBoard.Position bKing, ChessBoard.Position wKing) {
        this.board = board;
        this.bKingPosition = bKing;
        this.wKingPosition = wKing;
        this.cloned = true;
    }

    public static ChessBoard newInstance(ChessBoard _board) {
        ChessBoard ret = new ChessBoard(
                new ChessPiece[8][8], _board.bKingPosition, _board.wKingPosition);

        _board.toStream().filter(Objects::nonNull).forEach(piece -> {
            switch (piece) {
                case King k: ret.addPiece(new King(k.getRow(), k.getCol(), k.isWhite(), ret));
                    break;
                case Queen q: ret.addPiece(new Queen(q.getRow(), q.getCol(), q.isWhite(), ret));
                    break;
                case Rook r: ret.addPiece(new Rook(r.getRow(), r.getCol(), r.isWhite(), ret));
                    break;
                case Bishop b: ret.addPiece(new Bishop(b.getRow(), b.getCol(), b.isWhite(), ret));
                    break;
                case Knight k: ret.addPiece(new Knight(k.getRow(), k.getCol(), k.isWhite(), ret));
                    break;
                case Pawn p: ret.addPiece(new Pawn(p.getRow(), p.getCol(), p.isWhite(), ret));
                    break;
                default:
            }
        });
        return ret;
    }

    public Position getKingPosition(boolean white) {
        return white ? wKingPosition : bKingPosition;
    }

    public boolean isCloned() {
        return cloned;
    }

    public void setEnPassantSquare(Position[] enPassantSquare) {
        this.enPassantSquare = enPassantSquare;
    }

    public boolean hasEnPassantSquare() {
        return enPassantSquare != null;
    }

    public Position getEnPassantSquare() {
        return enPassantSquare[0];
    }

    public Position getEnPassantPiece() {
        return enPassantSquare[1];
    }

    public record Position(int row, int col) {

        @Override
        public String toString() {
            int rank = 8 - row;
            char file = (char) ('a' + col);
            return String.format("%c%d", file, rank);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Position p) {
                return (row == p.row) && (col == p.col);
            }
            return obj.equals(this);
        }

        public boolean equals(int row, int col) {
            return (this.row == row) && (this.col == col);
        }

        public boolean sameRow(Position cmp) {
            return this.row == cmp.row;
        }

        public boolean sameCol(Position cmp) {
            return this.col == cmp.col;
        }

        public boolean sameRowOrCol(Position cmp) {
            return this.row == cmp.row || this.col == cmp.col;
        }

        public boolean onDiagonal(Position cmp) {
            return Math.abs(this.row - cmp.row) == Math.abs(this.col - cmp.col);
        }

        public boolean isOnBoard() {
            return this.row >= ChessConstants.MIN_ROW
                    && this.row < ChessConstants.MAX_ROW
                    && this.col >= ChessConstants.MIN_COL
                    && this.col < ChessConstants.MAX_COL;
        }
    }

    public ChessPiece getPiece(int row, int col) {
        return board[row][col];
    }

    public ChessPiece getPiece(Position position) {
        return board[position.row()][position.col()];
    }

    public void movePiece(Position start, Position end) {
        board[end.row()][end.col()] = board[start.row()][start.col()];
        board[start.row()][start.col()] = null;
        getPiece(end).setPosition(end);

        if (bKingPosition.equals(start)) {
            bKingPosition = end;
        } else if (wKingPosition.equals(start)) {
            wKingPosition = end;
        }
    }

    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    public boolean isEmpty(int row, int col) {
        return getPiece(row, col) == null;
    }

    public Stream<ChessPiece> toStream() {
        return Arrays.stream(board).flatMap(Arrays::stream);
    }

    private void setup() {

        // test
        // addPiece(new Rook(4, 4, true, this));

        bKingPosition = new Position(0, 4);
        wKingPosition = new Position(7, 4);

        addPiece(new Rook(0, 0, false, this));
        addPiece(new Knight(0, 1, false, this));
        addPiece(new Bishop(0, 2, false, this));
        addPiece(new Queen(0, 3, false, this));
        addPiece(new King(0, 4, false, this));
        addPiece(new Bishop(0, 5, false, this));
        addPiece(new Knight(0, 6, false, this));
        addPiece(new Rook(0, 7, false, this));

        addPiece(new Rook(7, 0, true, this));
        addPiece(new Knight(7, 1, true, this));
        addPiece(new Bishop(7, 2, true, this));
        addPiece(new Queen(7, 3, true, this));
        addPiece(new King(7, 4, true, this));
        addPiece(new Bishop(7, 5, true, this));
        addPiece(new Knight(7, 6, true, this));
        addPiece(new Rook(7, 7, true, this));

        for (int col = ChessConstants.MIN_COL; col < ChessConstants.MAX_COL; col++) {
            addPiece(new Pawn(ChessConstants.BLACK_PAWN_START_COL, col, false, this));
            addPiece(new Pawn(ChessConstants.WHITE_PAWN_START_COL, col, true, this));
        }
    }

    private void addPiece(ChessPiece p) {
        board[p.getRow()][p.getCol()] = p;
    }
}
