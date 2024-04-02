package chess;

public class ChessGame {
    private final ChessBoard board;
    private boolean whiteTurn = true;

    public ChessGame() {
        this.board = new ChessBoard();
    }

    public ChessBoard getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return this.whiteTurn;
    }

    public boolean moveIfLegal(ChessBoard.Position start, ChessBoard.Position end) {
        if (isLegalMove(start, end)) {
            board.movePiece(start, end);
            whiteTurn = !whiteTurn;
            return true;
        }
        return false;
    }

    private boolean isLegalMove(ChessBoard.Position start, ChessBoard.Position end) {
        ChessPiece p = board.getPiece(start);

        return (p != null && p.isLegalMove(end) && p.isWhite() == whiteTurn);
    }
}
