package chess.engine;

import chess.ChessBoard;
import chess.ChessConstants;
import chess.ChessGame;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChessEngine {
    private final String name;
    private final Process processHandle;
    private final BufferedWriter writer;
    private final BufferedReader reader;

//     private final FenStringBuilder fenString;


    public ChessEngine(String pathToEngine, ChessGame game, String name) throws IOException {
        this.name = name;
        ProcessBuilder pb = new ProcessBuilder(pathToEngine);

        processHandle = pb.start();
        OutputStream stdin = processHandle.getOutputStream();
        InputStream stdout = processHandle.getInputStream();

        writer = new BufferedWriter(new OutputStreamWriter(stdin, StandardCharsets.UTF_8));
        reader = new BufferedReader(new InputStreamReader(stdout));

        do reader.read();
        while (reader.ready());
    }

    public boolean isReady() {
        try {
            sendCommand("isready");

            return reader.readLine().equals("readyok");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final static class GoResult {
        private List<String> info;
        private String bestmove;

        public List<String> info() {
            return info;
        }
        public String bestmove() {
            return bestmove;
        }
    }

    public GoResult go(int movetime) {
        try {
            sendCommand("go movetime " + movetime);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GoResult res = new GoResult();
        
        res.info = reader.lines()
                .peek(f -> {
                    if (f.startsWith("bestmove")) res.bestmove = f;
                })
                .takeWhile(f -> !f.startsWith("bestmove"))
                .toList();

        return res;
    }

    public void position(String moves) {
        try {
            sendCommand("position startpos moves " + moves);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendCommand(String cmd) throws IOException {
        writer.write(cmd);
        writer.newLine();
        writer.flush();
    }


//    public void updateFenString(ChessBoard.Position oldPos, ChessBoard.Position newPos) {
//        fenString.update(oldPos, newPos);
//    }
//
//    public String getFenString() {
//        return fenString.buildFenString();
//    }
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