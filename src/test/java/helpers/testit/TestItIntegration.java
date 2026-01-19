package helpers.testit;
import com.codeborne.selenide.Selenide;
import org.openqa.selenium.OutputType;
import ru.testit.services.Adapter;

import java.io.*;

public class TestItIntegration {


    public void attachScreenshot() {
        File screenshot = Selenide.screenshot(OutputType.FILE); try {
            assert screenshot != null;
            try (InputStream is = new FileInputStream(screenshot)) { Adapter.addAttachments("Скриншот ошибки", is); }
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

    }

}
