package fr.hardel.jev.debug;

import fr.hardel.jev.bot.Bot;
import fr.hardel.jev.bot.Bots;
import fr.hardel.jev.typesafe.Json;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

public final class DebugWindow {
    private static final int REFRESH_MILLIS = 500;
    private static @Nullable DebugWindow open;

    private final JFrame frame = new JFrame("Jev bots");
    private final DefaultListModel<String> roster = new DefaultListModel<>();
    private final JList<String> bots = new JList<>(roster);
    private final JTextArea perception = area();
    private final JTextArea log = area();
    private final Timer refresh;

    private record Snapshot(List<String> names, String perception, String log) {
    }

    private DebugWindow(MinecraftServer server) {
        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(perception), new JScrollPane(log));
        right.setResizeWeight(0.7);
        JSplitPane root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(bots), right);
        root.setResizeWeight(0.15);
        frame.setContentPane(root);
        frame.setSize(new Dimension(1100, 800));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        refresh = new Timer(REFRESH_MILLIS, _ -> request(server));
        refresh.start();
        frame.setVisible(true);
    }

    private static JTextArea area() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return area;
    }

    public static boolean toggle(MinecraftServer server) {
        if (open != null) {
            close();
            return false;
        }

        SwingUtilities.invokeLater(() -> open = new DebugWindow(server));
        return true;
    }

    public static void close() {
        SwingUtilities.invokeLater(() -> {
            if (open != null) {
                open.refresh.stop();
                open.frame.dispose();
                open = null;
            }
        });
    }

    private void request(MinecraftServer server) {
        String selected = bots.getSelectedValue();
        server.execute(() -> {
            Snapshot snapshot = snapshot(selected);
            SwingUtilities.invokeLater(() -> paint(snapshot));
        });
    }

    private static Snapshot snapshot(@Nullable String selected) {
        List<String> names = Bots.all().stream().map(Bot::name).sorted().toList();
        Bot bot = selected == null ? null : Bots.get(selected);
        if (bot == null) {
            return new Snapshot(names, "", "");
        }

        String header = bot.activityName() + " at " + bot.player().blockPosition().toShortString() + "\n" + bot.brain().report() + "\n\nperception\n";
        return new Snapshot(names, header + Json.pretty(bot.perceive()), String.join("\n", bot.log()));
    }

    private void paint(Snapshot snapshot) {
        String selected = bots.getSelectedValue();
        if (!snapshot.names().equals(List.of(roster.toArray()))) {
            roster.clear();
            snapshot.names().forEach(roster::addElement);
            bots.setSelectedValue(selected, false);
        }

        perception.setText(snapshot.perception());
        log.setText(snapshot.log());
    }
}
