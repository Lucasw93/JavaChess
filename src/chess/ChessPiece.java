package chess;

import java.util.List;
import java.util.Objects;

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

    public boolean isLegalMove(ChessBoard.Position newPosition) {
        // new position must be on the board and if not empty square, must be opposite color
        if (!(newPosition.isOnBoard() &&
                (getBoard().isEmpty(newPosition) ||
                        !this.isSameColor(getBoard().getPiece(newPosition))))) return false;

        // cant move into check
        return getBoard().isCloned() || moveNotInCheck(newPosition);
    }

    protected boolean isLegalMovePawnWorkaround(ChessBoard.Position newPosition) {
        // workaround only for pawns so that moveNotInCheck can be called last
        // and does not need the same checks for `isLegalMove()`
        return getBoard().isCloned() || moveNotInCheck(newPosition);
    }

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

    public void setPosition(ChessBoard.Position newPosition) {
        this.position = newPosition;
    }

    public boolean isWhite() {
        return this.white;
    }

    public boolean isSameColor(ChessPiece piece) {
        return this.isWhite() == piece.isWhite();
    }

    public boolean notSameColor(ChessPiece piece) {
        return this.isWhite() != piece.isWhite();
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

            if (this.notSameColor(p)) {
                if (board.isCloned() || moveNotInCheck(p.getPosition())) {
                    l.add(p.getPosition());
                }
            }
            return true;
        }
        ChessBoard.Position pos = new ChessBoard.Position(row, col);

        if (board.isCloned() || moveNotInCheck(pos)) l.add(pos);

        return false;
    }

    private boolean moveNotInCheck(ChessBoard.Position newPos) {
        ChessBoard cloneBoard = ChessBoard.newInstance(getBoard());
        cloneBoard.movePiece(getPosition(), newPos);

        ChessBoard.Position kingPos = cloneBoard.getKingPosition(isWhite());

        // scan entire board to see if the board is in a state where the king is in check
        return cloneBoard.toStream()
                .filter(Objects::nonNull)
                .filter(this::notSameColor)
                .noneMatch(f -> f.getLegalMoves().contains(kingPos));
    }


//        private boolean moveNotInCheck(ChessBoard.Position newPos) {
//        ChessBoard cloneBoard = ChessBoard.newInstance(getBoard());
//        cloneBoard.movePiece(getPosition(), newPos);
//        cloneBoard.cloned = true;
//        ChessBoard.Position kingPos = cloneBoard.getKingPosition(isWhite());
//        for (int row = 0; row < 8; row++) {
//            for (int col = 0; col < 8; col++) {
//                ChessPiece p = cloneBoard.getPiece(row, col);
//                if (p == null || p.isSameColor(this)) continue;
//                if (p.getLegalMoves().contains(kingPos)) return false;
//            }
//        }
//        return true;
//    }
}