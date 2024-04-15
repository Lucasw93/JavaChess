package chess;

import chess.engine.ChessEngine;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Pawn;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ChessGame {
    private final ChessBoard board;

    private boolean whiteTurn = true;
    private boolean enPassantMove;

    private List<ChessBoard.Position[]> possibleCheckMoves = new ArrayList<>();

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
        return !possibleCheckMoves.isEmpty();
    }

    public List<ChessBoard.Position[]> getInCheckPossibleMoves() {
        return possibleCheckMoves;
    }

    public boolean getEnPassantMove() {
        return enPassantMove;
    }

    public boolean moveIfLegal(ChessBoard.Position oldPos, ChessBoard.Position newPos) {
        //System.out.println("LEN: " + inCheckPossibleMoves);


        if (isLegalMove(oldPos, newPos)) {
//            if (isInCheck()) {
//                ChessBoard.Position p = inCheckPossibleMoves.get(oldPos);
//                if (p == null ||  !p.equals(newPos)) return false;
//                inCheckPossibleMoves.clear();
//            }
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

                findInCheckMoves(newPos, check.get());

                if (possibleCheckMoves.isEmpty()) {
                    System.out.println("CHECK MATE");
                }
            }
            return true;
        }
        return false;
    }

    private boolean isLegalMove(ChessBoard.Position start, ChessBoard.Position end) {
        if (isInCheck()) {
            for (ChessBoard.Position[] p : possibleCheckMoves) {
                if (p[0].equals(start) && p[1].equals(end)) {
                    possibleCheckMoves.clear();
                    return true;
                }
            }
            return false;
        } else {
            ChessPiece p = board.getPiece(start);

            return (p != null && p.isWhite() == whiteTurn && p.isLegalMove(end));
        }
    }

    private Optional<ChessPiece> isCheck(ChessBoard.Position lastMove) {
        return board.getPiece(lastMove).getLegalMoves().stream()
                .map(board::getPiece)
                .filter(f -> f instanceof King)
                .findFirst();
    }

    private void findInCheckMoves(ChessBoard.Position attackingPiece, ChessPiece king) {
        board.toStream()
                .filter(Objects::nonNull)
                .filter(f -> f.isWhite() == whiteTurn)
                .forEach(p -> {
                    for (ChessBoard.Position end : p.getLegalMoves()) {
                        if (isLegalInCheckMove(p.getPosition(), end)) {
                            possibleCheckMoves.add(new ChessBoard.Position[] { p.getPosition(), end });
                        }
                    }
                });

        //// test
        possibleCheckMoves.forEach(f -> System.out.println(f[0] + " to " + f[1]));
    }

    private boolean isLegalInCheckMove(ChessBoard.Position oldPos, ChessBoard.Position newPos) {
        ChessBoard cloneBoard = ChessBoard.newInstance(board);
        cloneBoard.movePiece(oldPos, newPos);

        ChessBoard.Position kingPos = cloneBoard.getKingPosition(whiteTurn);

        return cloneBoard.toStream()
                .filter(Objects::nonNull)
                .filter(f -> f.isWhite() != whiteTurn)
                .noneMatch(f -> f.getLegalMoves().contains(kingPos));
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