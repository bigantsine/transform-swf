package acceptance;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.flagstone.transform.Background;
import com.flagstone.transform.Movie;
import com.flagstone.transform.Place2;
import com.flagstone.transform.ShowFrame;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.datatype.WebPalette;
import com.flagstone.transform.font.DefineFont2;
import com.flagstone.transform.text.DefineTextField;
import com.flagstone.transform.util.font.Font;

public final class ShowTrueTypeFontTest {
    private static FilenameFilter filter;
    private static Set<Character> set;
    private static String alphabet;

    private static int width = 8000;
    private static int height = 4000;
    private static int margin = 400;
    private static int fontSize = 280;

    private static int screenWidth = width + margin;
    private static int screenHeight = height + margin;

    @BeforeClass
    public static void setUp() {
        filter = new FilenameFilter() {
            public boolean accept(final File directory, final String name) {
                return name.endsWith(".ttf");
            }
        };

        set = new LinkedHashSet<Character>();
        alphabet = "abcdefghijklmnopqrstuvwxyz";

        for (int i = 0; i < alphabet.length(); i++) {
            set.add(alphabet.charAt(i));
        }
    }

    @Test
    public void showBitstream() throws IOException, DataFormatException {
        final File sourceDir = new File(
                "test/data/ttf/reference/bitstream-vera");
        final File destDir = new File(
                "test/results/ShowTrueTypeFontTest/ttf/bitstream-vera");

        showFiles(sourceDir, sourceDir.list(filter), destDir);
    }

    @Test
    public void showDejaVu() throws IOException, DataFormatException {
        final File sourceDir = new File("test/data/ttf/reference/dejavu");
        final File destDir = new File(
                "test/results/ShowTrueTypeFontTest/ttf/dejavu");

        showFiles(sourceDir, sourceDir.list(filter), destDir);
    }

    private void showFiles(final File sourceDir, final String[] files,
            final File destDir) throws IOException, DataFormatException {
        File sourceFile;
        File destFile;
        Font font;
        DefineFont2 definition;

        if (!destDir.exists() && !destDir.mkdirs()) {
            fail();
        }

        for (final String file : files) {
            sourceFile = new File(sourceDir, file);
            destFile = new File(destDir, file.substring(0, file
                    .lastIndexOf('.'))
                    + ".swf");
            font = new Font();
            // TODOfont.decode(sourceFile);
            definition = font.defineFont(10, set);
            showFont(definition, destFile);
        }
    }

    private void showFont(final DefineFont2 font, final File file)
            throws IOException, DataFormatException {
        final Movie movie = new Movie();

        final DefineTextField text = new DefineTextField(movie.identifier());
        text.setBounds(new Bounds(0, 0, width, height));
        text.setVariableName("var");
        text.setInitialText(alphabet);
        text.setUseFontGlyphs(true);
        text.setFontIdentifier(font.getIdentifier());
        text.setFontHeight(fontSize);
        text.setColor(WebPalette.BLACK.color());

        movie.setFrameSize(new Bounds(0, 0, screenWidth, screenHeight));
        movie.setFrameRate(1.0f);
        movie.add(new Background(WebPalette.LIGHT_BLUE.color()));
        movie.add(font);
        movie.add(text);
        movie.add(Place2.show(text.getIdentifier(), 1, margin, margin));
        movie.add(ShowFrame.getInstance());
        movie.encodeToFile(file);
    }
}
