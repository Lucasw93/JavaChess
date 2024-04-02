package chess;

import java.util.List;

public abstract class ChessPiece {
    private final boolean white;
    private final ChessBoard board;
    private final String symbol;
    private ChessBoard.Position position;
    protected int value;


    public ChessPiece(int row, int col, boolean white, ChessBoard board, String symbol) {
        this.position = new ChessBoard.Position(row, col);
        this.white = white;
        this.board = board;
        this.symbol = symbol;
    }

    public abstract List<ChessBoard.Position> getLegalMoves();
    public abstract boolean isLegalMove(ChessBoard.Position newPosition);

    public ChessBoard getBoard() {
        return this.board;
    }

    public String getSymbol() {
        return symbol;
    }

    public ChessBoard.Position getPosition() {
        return this.position;
    }

    public int getRow() {
        return this.position.row();
    }

    public int getCol() {
        return position.col();
    }

    public  int getValue() {
        return value;
    }

    public void setPosition(int row, int col) {
        //this.position = new ChessBoard.Position(row, col);

        // this.position.get = row;
        //this.position.col = col;
    }

    public void setPosition(ChessBoard.Position newPosition) {
        this.position = newPosition;
    }

    public boolean isWhite() {
        return this.white;
    }

    public boolean isSameColor(ChessPiece piece) {
        return this.isWhite() == piece.isWhite();
    }

    protected boolean isBlocked(ChessBoard.Position position) {
        int dRow = Integer.compare(position.row(), this.getRow());
        int dCol = Integer.compare(position.col(), this.getCol());
        int row = this.getRow() + dRow;
        int col = this.getCol() + dCol;

        while (!position.equals(row, col)) {
            if (getBoard().getPiece(row, col) != null) return true;
            row += dRow;
            col += dCol;
        }
        return false;
    }

    protected boolean isLegalMoveCommon(ChessBoard.Position position) {
        // new position must be on the board
        // if new position is not empty must be a piece of the opposite color
        return position.isOnBoard() &&
                !(!getBoard().isEmpty(position) && this.isSameColor(getBoard().getPiece(position)));
    }

    protected void addLineMoves(ChessBoard.Position position, List<ChessBoard.Position> l) {
        int row = position.row();
        int col = position.col();

        while (++row < ChessConstants.MAX_ROW) {
            if (addIfLegalMove(row, col, l)) break;
        }
        row = this.getRow();

        while (row-- > ChessConstants.MIN_ROW) {
            if (addIfLegalMove(row, col, l)) break;
        }
        row = this.getRow();

        while (++col < ChessConstants.MAX_COL) {
            if (addIfLegalMove(row, col, l)) break;
        }
        col = this.getCol();

        while (col-- > ChessConstants.MIN_COL) {
            if (addIfLegalMove(row, col, l)) break;
        }
    }

    protected void addDiagonalMoves(ChessBoard.Position position, List<ChessBoard.Position> l) {
        int row = position.row();
        int col = position.col();

        while (++row < ChessConstants.MAX_ROW && ++col < ChessConstants.MAX_COL) {
            if (addIfLegalMove(row, col, l)) break;
        }
        row = position.row();
        col = position.col();

        while (row-- > ChessConstants.MIN_ROW && col-- > ChessConstants.MIN_COL) {
            if (addIfLegalMove(row, col, l)) break;
        }
        row = position.row();
        col = position.col();

        while (++row < ChessConstants.MAX_ROW && col-- > ChessConstants.MIN_COL) {
            if (addIfLegalMove(row, col, l)) break;
        }
        row = position.row();
        col = position.col();

        while (row-- > ChessConstants.MIN_ROW && ++col < ChessConstants.MAX_COL) {
            if (addIfLegalMove(row, col, l)) break;
        }
    }

    private boolean addIfLegalMove(int row, int col, List<ChessBoard.Position> l) {
        if (!getBoard().isEmpty(row, col)) {
            ChessPiece p = getBoard().getPiece(row, col);

            if (!this.isSameColor(p)) {
                l.add(p.getPosition());
            }
            return true;
        }
        l.add(new ChessBoard.Position(row, col));

        return false;
    }
}