package chess;

import chess.engine.ChessEngine;
import chess.pieces.*;

import java.io.IOException;


public class ChessGame {
    private final ChessBoard board;
    private boolean whiteTurn = true;
    private boolean enPassantMove;
    private ChessEngine engine;
    private boolean check;
    private boolean promotion;

    public ChessGame() {
        this.board = new ChessBoard();
    }

    public boolean hasPromotion() {
        return promotion;
    }

    public boolean hasCheck() {
        return check;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public void addEngine(String path) {
        try {
            engine = new ChessEngine(path, this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasEngine() {
        return engine != null;
    }

    public ChessEngine getEngine() {
        return engine;
    }

    public boolean isWhiteTurn() {
        return this.whiteTurn;
    }

    public boolean isEnPassantMove() {
        return enPassantMove;
    }

    public void promote(ChessBoard.Position position, Class<?> c) {
        board.promotePiece(c.equals(Queen.class)
                ? new Queen(position.row(), position.col(), whiteTurn, board)
                : c.equals(Rook.class)
                ? new Rook(position.row(), position.col(), whiteTurn, board)
                : c.equals(Bishop.class)
                ? new Bishop(position.row(), position.col(), whiteTurn, board)
                : c.equals(Knight.class)
                ? new Knight(position.row(), position.col(), whiteTurn, board):
                    null);

        promotion = false;
        whiteTurn = !whiteTurn;

        if (isCheck(position)) {
            System.out.println("CHECK");

            if (isCheckMate()) {
                System.out.println("CHECK MATE");
            }
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

            if (hasEngine()) engine.updateFenString(oldPos, newPos);

            //// test
            // System.out.println(engine.getFenString());


            if (isCheck(newPos)) {
                System.out.println("CHECK");

                if (isCheckMate()) {
                    System.out.println("CHECK MATE");
                }
            }
            return true;
        }
        return false;
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
        return promotion = (newPos.row() == 8 && !whiteTurn) || (newPos.row() == 0 && whiteTurn) &&
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