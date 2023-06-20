import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String... args) throws IOException {
        var trelloKey = args[0];
        var trelloToken = args[1];
        var boardIds = args[2];
        var output = args[3];

        if (trelloKey == null || trelloToken == null || boardIds == null) {
            throw new IllegalArgumentException("No input parameters");
        }

        for (String id : boardIds.split(",")) {
            var backup = Downloader.download(trelloKey, trelloToken, id);
            Files.writeString(
                    Path.of(output).resolve(id + ".json"),
                    backup,
                    StandardCharsets.UTF_8
            );
            System.out.println("Board " + id + " processed");
        }
    }
}
