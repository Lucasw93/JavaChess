package chess.engine;

import chess.ChessBoard;
import chess.ChessConstants;
import chess.ChessGame;

import java.io.*;
import java.util.*;

public class ChessEngine {
    private final String name;
    private final List<String> options;
    private final Process process;
    private final BufferedWriter writer;
    private final BufferedReader reader;


    public ChessEngine(String pathToEngine, ChessGame game) throws IOException {
        process = new ProcessBuilder(pathToEngine).start();

        OutputStream pipedIn = process.getOutputStream();
        InputStream pipedOut = process.getInputStream();

        writer = new BufferedWriter(new OutputStreamWriter(pipedIn));
        reader = new BufferedReader(new InputStreamReader(pipedOut));

        String[] tmp = new String[1];

        sendCommand("uci");
        options = reader.lines()
                .peek(f -> {
                    if (f.startsWith("id name")) {
                        tmp[0] = f.substring(7).stripLeading();
                    }
                })
                .takeWhile(f -> !f.equals("uciok"))
                .filter(f -> f.startsWith("option"))
                .toList();

        name = tmp[0];
    }

    public String name() {
        return name;
    }
    public List<String> options() {
        return options;
    }

    public boolean isready() throws IOException {
        sendCommand("isready");

        return reader.readLine().equals("readyok");
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

    public GoResult go(int movetime) throws IOException {
        sendCommand("go movetime " + movetime);

        GoResult res = new GoResult();
        
        res.info = reader.lines()
                .peek(f -> {
                    if (f.startsWith("bestmove")) res.bestmove = f;
                })
                .takeWhile(f -> !f.startsWith("bestmove"))
                .toList();

        return res;
    }

    public void position(String moves) throws IOException {
        sendCommand("position startpos moves " + moves);
    }

    private void sendCommand(String cmd) throws IOException {
        writer.write(cmd);
        writer.newLine();
        writer.flush();
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