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
        startpos(this);
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

        ChessBoard.stream(_board).forEach(piece -> {
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

    public void promotePiece(ChessPiece piece) {
        board[piece.getRow()][piece.getCol()] = piece;
    }

    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    public boolean isEmpty(int row, int col) {
        return getPiece(row, col) == null;
    }

    public static Stream<ChessPiece> stream(ChessBoard board) {
        return Arrays.stream(board.board).flatMap(Arrays::stream).filter(Objects::nonNull);
    }

    public static void startpos(ChessBoard b) {

        //// test
        // b.addPiece(new Rook(4, 4, true, b));

        b.bKingPosition = new Position(0, 4);
        b.wKingPosition = new Position(7, 4);

        b.addPiece(new Rook(0, 0, false, b));
        b.addPiece(new Knight(0, 1, false, b));
        b.addPiece(new Bishop(0, 2, false, b));
        b.addPiece(new Queen(0, 3, false, b));
        b.addPiece(new King(0, 4, false, b));
        b.addPiece(new Bishop(0, 5, false, b));
        b.addPiece(new Knight(0, 6, false, b));
        b.addPiece(new Rook(0, 7, false, b));

        b.addPiece(new Rook(7, 0, true, b));
        b.addPiece(new Knight(7, 1, true, b));
        b.addPiece(new Bishop(7, 2, true, b));
        b.addPiece(new Queen(7, 3, true, b));
        b.addPiece(new King(7, 4, true, b));
        b.addPiece(new Bishop(7, 5, true, b));
        b.addPiece(new Knight(7, 6, true, b));
        b.addPiece(new Rook(7, 7, true, b));

        for (int row = ChessConstants.BLACK_PAWN_START_COL;
             row <= ChessConstants.WHITE_PAWN_START_COL; row++) {
            for (int col = 0; col < 8; col++) {
                if (row == ChessConstants.BLACK_PAWN_START_COL) {
                    b.addPiece(new Pawn(ChessConstants.BLACK_PAWN_START_COL, col, false, b));
                } else if (row == ChessConstants.WHITE_PAWN_START_COL) {
                    b.addPiece(new Pawn(ChessConstants.WHITE_PAWN_START_COL, col, true, b));
                } else {
                    b.board[row][col] = null;
                }
            }
        }
    }

    private void addPiece(ChessPiece p) {
        board[p.getRow()][p.getCol()] = p;
    }
}
