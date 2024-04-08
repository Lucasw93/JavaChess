package chess;

import chess.engine.ChessEngine;
import chess.pieces.King;
import chess.pieces.Pawn;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ChessGame {
    private final ChessBoard board;

    private boolean whiteTurn = true;
    private boolean inCheck = false;
    private boolean enPassantMove;

    private ChessEngine engine;

    public ChessGame() {
        this.board = new ChessBoard();
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

    public boolean isInCheck() {
        return inCheck;
    }

    public boolean getEnPassantMove() {
        return enPassantMove;
    }

    public boolean moveIfLegal(ChessBoard.Position oldPos, ChessBoard.Position newPos) {
        if (isLegalMove(oldPos, newPos)) {
            if (isEnPassantMove(oldPos, newPos)) {
                board.movePiece(oldPos, board.getEnPassantPiece());
                board.movePiece(board.getEnPassantPiece(), newPos);
            } else {
                board.movePiece(oldPos, newPos);
                updateEnPassantSquare(oldPos.row(), newPos);
            }
            whiteTurn = !whiteTurn;

            if (hasEngine()) engine.updateFenString(oldPos, newPos);

            // test
            // System.out.println(engine.getFenString());

            Optional<ChessPiece> check = isCheck(newPos);

            if (check.isPresent()) {
                System.out.println("CHECK");
                inCheck = true;

                ChessPiece p = check.get();

                if (isCheckMate(p.getPosition(), p.getLegalMoves())) {
                    System.out.println("CHECK MATE");
                }
            }
            return true;
        }
        return false;
    }

    private boolean isLegalMove(ChessBoard.Position start, ChessBoard.Position end) {
        ChessPiece p = board.getPiece(start);

        return (p != null && p.isLegalMove(end) && p.isWhite() == whiteTurn);
    }

    private Optional<ChessPiece> isCheck(ChessBoard.Position lastMove) {
        return board.getPiece(lastMove).getLegalMoves().stream()
                .map(f -> getBoard().getPiece(f))
                .filter(f -> f instanceof King)
                .findFirst();
    }

    private boolean isCheckMate(ChessBoard.Position curKingPosition, List<ChessBoard.Position> kingMoves) {
        for (ChessBoard.Position kingMove : kingMoves) {
            try {
                ChessBoard cloneBoard = (ChessBoard) getBoard().clone();

                cloneBoard.movePiece(curKingPosition, kingMove);

                /* if `scanBoardForCheck()` is false, there is a legal move for the king */
                if (!scanBoardForCheck(kingMove, cloneBoard)) return false;
            } catch (Exception e) {
                System.err.println("Clone failed");
            }
        }
        return true;
    }

    private boolean scanBoardForCheck(ChessBoard.Position testKingPos, ChessBoard boardState) {
        // scan entire @cloneBoard to see is the board is in a state where the king is in check
        // @testKingPos is given as a convenience to avoid (instance of king)
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece p = boardState.getPiece(row, col);
                if (p == null || p.isWhite() == whiteTurn) continue;

                if (p.getLegalMoves()
                        .stream()
                        .anyMatch(f -> f.equals(testKingPos))) return true;
            }
        }
        return false;
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
        enPassantMove = getBoard().getPiece(oldPos) instanceof Pawn &&
                board.hasEnPassantSquare() &&
                newPos.equals(board.getEnPassantSquare());

        return enPassantMove;
    }
}