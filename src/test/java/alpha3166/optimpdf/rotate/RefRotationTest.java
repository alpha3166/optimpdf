package alpha3166.optimpdf.rotate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RefRotationTest {
  Path base;

  @BeforeEach
  public void beforeEach() throws Exception {
    base = DataManager.makeTestDir();
  }

  @AfterEach
  public void afterEach() throws Exception {
    DataManager.removeDir(base);
  }

  @Test
  void refPdf_Specified() throws Exception {
    // Setup
    DataManager.generatePdf(base.resolve("ref.pdf"), 0, 90, 180, 270);
    // Exercise
    var sut = new RefRotation(base.resolve("ref.pdf"), 180);
    // Verify
    assertEquals(4, sut.getRefPdfNumberOfPages());
    assertEquals(0, sut.getRefRotation(1));
    assertEquals(90, sut.getRefRotation(2));
    assertEquals(180, sut.getRefRotation(3));
    assertEquals(270, sut.getRefRotation(4));
  }

  @Test
  void refPdf_Specified_notExists() throws Exception {
    // Exercise & Verify
    assertThrows(IOException.class, () -> new RefRotation(base.resolve("ref.pdf"), 180));
  }

  @Test
  void refPdf_dir() throws Exception {
    // Setup
    Files.createDirectory(base.resolve("ref.pdf"));
    // Exercise & Verify
    assertThrows(FileNotFoundException.class, () -> new RefRotation(base.resolve("ref.pdf"), 180));
  }

  @Test
  void refPdf_absent() throws Exception {
    // Exercise
    var sut = new RefRotation(null, 180);
    // Verify
    assertThrows(IllegalStateException.class, () -> sut.getRefPdfNumberOfPages());
    assertEquals(180, sut.getRefRotation(1));
    assertEquals(180, sut.getRefRotation(2));
    assertEquals(180, sut.getRefRotation(3));
    assertEquals(180, sut.getRefRotation(4));
  }
}
