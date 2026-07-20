package dev.nokhxyr.wherewasi.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.nokhxyr.wherewasi.WhereWasI;
import dev.nokhxyr.wherewasi.model.ActivityEvent;
import dev.nokhxyr.wherewasi.model.Note;
import dev.nokhxyr.wherewasi.model.Session;

/**
 * Append-only JSONL journal per world/server, plus whole-file JSON for the small
 * mutable collections (notes, zones, discovered items). Writes are atomic
 * (temp-file + move); the active journal rotates past ~2&nbsp;MB; loading skips any
 * corrupt line or file rather than failing.
 */
public final class JournalStorage {

    private static final long MAX_JOURNAL_BYTES = 2_000_000L;
    private static final Gson COMPACT = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private final Path dir;
    private final Path activeJournal;
    private BufferedWriter journalWriter;

    public JournalStorage(Path dir) {
        this.dir = dir;
        this.activeJournal = dir.resolve("journal.jsonl");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            WhereWasI.LOGGER.error("WhereWasI: could not create journal dir {}", dir, e);
        }
    }

    public Path dir() {
        return dir;
    }

    // ---- events ------------------------------------------------------------

    public synchronized void appendEvent(ActivityEvent e) {
        try {
            rotateIfNeeded();
            BufferedWriter w = writer();
            w.write(COMPACT.toJson(JournalCodec.toJson(e)));
            w.newLine();
            w.flush();
        } catch (IOException ex) {
            WhereWasI.LOGGER.warn("WhereWasI: failed to append event", ex);
        }
    }

    public List<ActivityEvent> loadEvents() {
        List<ActivityEvent> out = new ArrayList<>();
        for (Path file : journalFilesInOrder()) {
            for (String line : readLines(file)) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    JsonElement el = JsonParser.parseString(line);
                    if (el.isJsonObject()) {
                        out.add(JournalCodec.eventFromJson(el.getAsJsonObject()));
                    }
                } catch (Exception ignored) {
                    // tolerate a corrupt line
                }
            }
        }
        out.sort(Comparator.comparingLong(ActivityEvent::time));
        return out;
    }

    private BufferedWriter writer() throws IOException {
        if (journalWriter == null) {
            journalWriter = Files.newBufferedWriter(activeJournal, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        return journalWriter;
    }

    private void rotateIfNeeded() throws IOException {
        if (!Files.exists(activeJournal) || Files.size(activeJournal) <= MAX_JOURNAL_BYTES) {
            return;
        }
        closeWriter();
        int idx = nextRotationIndex();
        Files.move(activeJournal, dir.resolve("journal." + idx + ".jsonl"));
    }

    private int nextRotationIndex() {
        int max = 0;
        for (Path p : rotatedFiles()) {
            max = Math.max(max, rotationIndex(p));
        }
        return max + 1;
    }

    private List<Path> journalFilesInOrder() {
        List<Path> files = new ArrayList<>(rotatedFiles());
        files.sort(Comparator.comparingInt(JournalStorage::rotationIndex));
        if (Files.exists(activeJournal)) {
            files.add(activeJournal);
        }
        return files;
    }

    private List<Path> rotatedFiles() {
        List<Path> files = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return files;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> {
                String n = p.getFileName().toString();
                return n.startsWith("journal.") && n.endsWith(".jsonl") && !n.equals("journal.jsonl");
            }).forEach(files::add);
        } catch (IOException e) {
            WhereWasI.LOGGER.warn("WhereWasI: could not list journal files", e);
        }
        return files;
    }

    private static int rotationIndex(Path p) {
        String n = p.getFileName().toString(); // journal.<idx>.jsonl
        try {
            return Integer.parseInt(n.substring("journal.".length(), n.length() - ".jsonl".length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ---- sessions ----------------------------------------------------------

    public synchronized void appendSession(Session s) {
        Path file = dir.resolve("sessions.jsonl");
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(COMPACT.toJson(JournalCodec.toJson(s)));
            w.newLine();
        } catch (IOException e) {
            WhereWasI.LOGGER.warn("WhereWasI: failed to append session", e);
        }
    }

    public List<Session> loadSessions() {
        List<Session> out = new ArrayList<>();
        for (String line : readLines(dir.resolve("sessions.jsonl"))) {
            if (line.isBlank()) {
                continue;
            }
            try {
                JsonElement el = JsonParser.parseString(line);
                if (el.isJsonObject()) {
                    out.add(JournalCodec.sessionFromJson(el.getAsJsonObject()));
                }
            } catch (Exception ignored) {
                // skip corrupt line
            }
        }
        out.sort(Comparator.comparingLong(Session::startEpochMs));
        return out;
    }

    // ---- notes -------------------------------------------------------------

    public synchronized void saveNotes(List<Note> notes) {
        JsonArray arr = new JsonArray();
        for (Note n : notes) {
            arr.add(JournalCodec.toJson(n));
        }
        writeAtomic("notes.json", PRETTY.toJson(arr));
    }

    public List<Note> loadNotes() {
        List<Note> out = new ArrayList<>();
        JsonElement el = readJson("notes.json");
        if (el != null && el.isJsonArray()) {
            for (JsonElement e : el.getAsJsonArray()) {
                try {
                    if (e.isJsonObject()) {
                        out.add(JournalCodec.noteFromJson(e.getAsJsonObject()));
                    }
                } catch (Exception ignored) {
                    // skip
                }
            }
        }
        return out;
    }

    // ---- discovered items --------------------------------------------------

    public synchronized void saveDiscovered(Set<String> ids) {
        JsonArray arr = new JsonArray();
        for (String id : ids) {
            arr.add(id);
        }
        writeAtomic("discovered.json", COMPACT.toJson(arr));
    }

    public Set<String> loadDiscovered() {
        Set<String> out = new LinkedHashSet<>();
        JsonElement el = readJson("discovered.json");
        if (el != null && el.isJsonArray()) {
            for (JsonElement e : el.getAsJsonArray()) {
                try {
                    out.add(e.getAsString());
                } catch (Exception ignored) {
                    // skip
                }
            }
        }
        return out;
    }

    // ---- generic json (zones) ---------------------------------------------

    public JsonElement readJson(String name) {
        Path file = dir.resolve(name);
        if (!Files.exists(file)) {
            return null;
        }
        try {
            return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
        } catch (Exception e) {
            WhereWasI.LOGGER.warn("WhereWasI: could not read {} (ignored)", name, e);
            return null;
        }
    }

    public void writeJson(String name, JsonElement element) {
        writeAtomic(name, PRETTY.toJson(element));
    }

    // ---- lifecycle ---------------------------------------------------------

    public synchronized void flush() {
        try {
            if (journalWriter != null) {
                journalWriter.flush();
            }
        } catch (IOException e) {
            WhereWasI.LOGGER.warn("WhereWasI: flush failed", e);
        }
    }

    public synchronized void close() {
        closeWriter();
    }

    private void closeWriter() {
        try {
            if (journalWriter != null) {
                journalWriter.flush();
                journalWriter.close();
            }
        } catch (IOException e) {
            WhereWasI.LOGGER.warn("WhereWasI: close failed", e);
        } finally {
            journalWriter = null;
        }
    }

    // ---- helpers -----------------------------------------------------------

    private synchronized void writeAtomic(String name, String content) {
        Path target = dir.resolve(name);
        Path tmp = dir.resolve(name + ".tmp");
        try {
            Files.writeString(tmp, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            WhereWasI.LOGGER.warn("WhereWasI: failed to write {}", name, e);
        }
    }

    private List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            WhereWasI.LOGGER.warn("WhereWasI: could not read {}", file, e);
        }
        return lines;
    }
}
