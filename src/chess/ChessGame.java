package chess;

import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class ChessGame {
    private final ChessBoard board = new ChessBoard();
    private final List<String> log = new ArrayList<>();
    private boolean whiteTurn = true;
    private boolean enPassantMove;
    private boolean check;
    private boolean checkMate;
    private boolean promotion;

    public ChessBoard getBoard() {
        return board;
    }

    public List<String> log() {
        return log;
    }

    public boolean hasPromotion() {
        return promotion;
    }

    public boolean hasCheck() {
        return check;
    }

    public boolean hasCheckMate() {
        return checkMate;
    }

    public boolean isWhiteTurn() {
        return this.whiteTurn;
    }

    public boolean isEnPassantMove() {
        return enPassantMove;
    }

    public void promote(ChessBoard.Position oldpos, ChessBoard.Position newpos, Class<?> c) {
        ChessPiece p = c.equals(Queen.class)
                ? new Queen(newpos.row(), newpos.col(), whiteTurn, board)
                : c.equals(Rook.class)
                ? new Rook(newpos.row(), newpos.col(), whiteTurn, board)
                : c.equals(Bishop.class)
                ? new Bishop(newpos.row(), newpos.col(), whiteTurn, board)
                : c.equals(Knight.class)
                ? new Knight(newpos.row(), newpos.col(), whiteTurn, board):
                    null;

        board.promotePiece(p);
        promotion = false;
        whiteTurn = !whiteTurn;

        log.add(oldpos.toString() + newpos + p.toString().toLowerCase());

        if (isCheck(newpos) && isCheckMate()) {
            checkMate = true;
        }
    }

    public boolean moveIfLegal(ChessBoard.Position oldPos, ChessBoard.Position newPos) {
        if (isLegalMove(oldPos, newPos)) {
            if (isEnPassantMove(oldPos, newPos)) {
                board.movePiece(oldPos, board.getEnPassantPiece());
                board.movePiece(board.getEnPassantPiece(), newPos);
            } else {
                board.movePiece(oldPos, newPos);
                updateEnPassantSquare(oldPos.row(), newPos);

                if (checkForPawnPromotion(newPos)) return true;
            }
            whiteTurn = !whiteTurn;

            log.add(oldPos.toString() + newPos);

            if (isCheck(newPos) && isCheckMate()) {
                checkMate = true;
            }
            return true;
        }
        return false;
    }

    public void resetGame() {
        ChessBoard.startpos(board);
        whiteTurn = true;
    }


    private boolean isLegalMove(ChessBoard.Position start, ChessBoard.Position end) {
        ChessPiece p = board.getPiece(start);

        return (p != null && p.isWhite() == whiteTurn && p.isLegalMove(end));
    }

    private boolean isCheck(ChessBoard.Position lastMove) {
        return check = board.getPiece(lastMove)
                .getLegalMoves().contains(board.getKingPosition(whiteTurn));
    }

    private boolean isCheckMate() {
        return ChessBoard.stream(board)
                .filter(f -> f.isWhite() == whiteTurn)
                .allMatch(p -> p.getLegalMoves().isEmpty());
    }

    private boolean checkForPawnPromotion(ChessBoard.Position newPos) {
        return promotion = (newPos.row() == 7 && !whiteTurn) || (newPos.row() == 0 && whiteTurn) &&
                board.getPiece(newPos) instanceof Pawn;
    }

    private void updateEnPassantSquare(int startRow, ChessBoard.Position endPos) {
        if (startRow == ChessConstants.BLACK_PAWN_START_COL ||
                startRow == ChessConstants.WHITE_PAWN_START_COL) {

            ChessPiece p = getBoard().getPiece(endPos);

            if (p instanceof Pawn && (Math.abs(startRow - endPos.row()) == 2)) {
                ChessBoard.Position enPassSquare = new ChessBoard.Position(
                        p.isWhite() ? startRow - 1 : startRow + 1, endPos.col());

                board.setEnPassantSquare(new ChessBoard.Position[] { enPassSquare, endPos });
                return;
            }
        }
        board.setEnPassantSquare(null);
    }

    private boolean isEnPassantMove(ChessBoard.Position oldPos, ChessBoard.Position newPos) {
        return enPassantMove = getBoard().getPiece(oldPos) instanceof Pawn &&
                board.hasEnPassantSquare() &&
                newPos.equals(board.getEnPassantSquare());
    }


/*
 * GetLegalMoves accurately gets all the legal moves with accounting for
 * check and checkmate so this is unnecessary. keep just as a back up
 */
//    private List<ChessBoard.Position[]> possibleCheckMoves = new ArrayList<>();
//    public List<ChessBoard.Position[]> getInCheckPossibleMoves() {
//        return possibleCheckMoves;
//    }
//    public boolean isInCheck() {
//        return !possibleCheckMoves.isEmpty();
//    }
//
//    private void findInCheckMoves(ChessBoard.Position attackingPiece, ChessPiece king) {
//        board.toStream()
//                .filter(Objects::nonNull)
//                .filter(f -> f.isWhite() == whiteTurn)
//                .forEach(p -> {
//                    for (ChessBoard.Position end : p.getLegalMoves()) {
//                        if (isLegalInCheckMove(p.getPosition(), end)) {
//                            possibleCheckMoves.add(new ChessBoard.Position[] { p.getPosition(), end });
//                        }
//                    }
//                });
//    }
//
//    private boolean isLegalInCheckMove(ChessBoard.Position oldPos, ChessBoard.Position newPos) {
//        ChessBoard cloneBoard = ChessBoard.newInstance(board);
//        cloneBoard.movePiece(oldPos, newPos);
//
//        ChessBoard.Position kingPos = cloneBoard.getKingPosition(whiteTurn);
//
//        return cloneBoard.toStream()
//                .filter(Objects::nonNull)
//                .filter(f -> f.isWhite() != whiteTurn)
//                .noneMatch(f -> f.getLegalMoves().contains(kingPos));
//    }
}