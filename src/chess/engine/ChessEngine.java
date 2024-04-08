package chess.engine;

import chess.ChessBoard;
import chess.ChessConstants;
import chess.ChessGame;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChessEngine {
    private final Process processHandle;
    private final BufferedWriter writer;
    private final Scanner reader;
    private final FenStringBuilder fenString;


    public ChessEngine(String pathToEngine, ChessGame game) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(pathToEngine);

        processHandle = pb.start();
        OutputStream stdin = processHandle.getOutputStream();
        InputStream stdout = processHandle.getInputStream();

        writer = new BufferedWriter(new OutputStreamWriter(stdin, StandardCharsets.UTF_8));
        reader = new Scanner(stdout);

        fenString = new FenStringBuilder(game);

        reader.nextLine();
    }

    public boolean isReady() {
        try {
            writer.write("isready \n");
            writer.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (reader.hasNext()) {
            if (reader.nextLine().equals("readyok")) return true;
        }
        return false;
    }

    public void updateFenString(ChessBoard.Position oldPos, ChessBoard.Position newPos) {
        fenString.update(oldPos, newPos);
    }

    public String getFenString() {
        return fenString.buildFenString();
    }
}

class FenStringBuilder {
    private String[] fenPosition;
    private ChessGame game;

    FenStringBuilder(ChessGame game) {
        fenPosition = new String[ChessConstants.MAX_COL];
        this.game = game;

        for (int row = 0; row < ChessConstants.MAX_COL; row++) updateRow(row);
    }

    public void update(ChessBoard.Position oldPosition, ChessBoard.Position newPosition) {
        updateRow(oldPosition.row());
        if (oldPosition.row() != newPosition.row()) {
            updateRow(newPosition.row());
        }
    }

    public String buildFenString() {
        return String.format("%s %s %s %s \n",
                getFenPosition(),
                (game.isWhiteTurn() ? "w" : "b"),
                "KQkq",
                (game.getBoard().hasEnPassantSquare()
                        ? game.getBoard().getEnPassantSquare() : "-"));
    }

    private String getFenPosition() {
        return String.join("/", fenPosition);
    }

    private void updateRow(int row) {
        int count, col;
        StringBuilder sb = new StringBuilder(ChessConstants.MAX_COL);

        for (count = col = 0; col < ChessConstants.MAX_COL; col++) {
            if (game.getBoard().isEmpty(row, col)) {
                count++;
                continue;
            } else if (count > 0) {
                sb.append(count);
                count = 0;
            }
            sb.append(game.getBoard().getPiece(row, col));
        }
        if (count > 0) sb.append(count);

        fenPosition[row] = sb.toString();
    }
}