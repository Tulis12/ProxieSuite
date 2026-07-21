package dev.tulis.proxieSuite.API;

import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class LogFilter extends AbstractFilter {

    private static Main plugin;

    public LogFilter() {}

    public LogFilter(Main m) {
        plugin = m;
    }

    public static void registerFilter() {
        Logger logger = (Logger) LogManager.getRootLogger();
        logger.addFilter(new LogFilter());
    }

    String regex =
        "\\[(initial connection|connected player)\\] (/\\d+\\.\\d+\\.\\d+\\.\\d+):\\d+ has disconnected: (.*)";
    Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

    @Override
    public Result filter(LogEvent event) {
        if (event == null) {
            return Result.NEUTRAL;
        }

        if (event.getLoggerName().contains("Hikari")) {
            return Result.DENY;
        }

        Message msg = event.getMessage();
        if (msg != null && msg.getFormattedMessage() != null && I18N.ready()) {
            String formattedMessage = msg.getFormattedMessage();

            Matcher matcher = pattern.matcher(formattedMessage);

            while (matcher.find()) {
                String kickMessage = matcher.group(3);
                String newMsg = I18N.matchesKick(kickMessage);

                if (newMsg != null) {
                    plugin
                        .getLogger()
                        .info(
                            "Kicked player with IP: {} cause: {}",
                            matcher.group(2),
                            newMsg
                        );
                    return Result.DENY;
                }
            }
        }

        return Result.NEUTRAL;
    }

    @Override
    public Result filter(
        Logger logger,
        Level level,
        Marker marker,
        Message msg,
        Throwable t
    ) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(
        Logger logger,
        Level level,
        Marker marker,
        String msg,
        Object... params
    ) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(
        Logger logger,
        Level level,
        Marker marker,
        Object msg,
        Throwable t
    ) {
        return Result.NEUTRAL;
    }
}
