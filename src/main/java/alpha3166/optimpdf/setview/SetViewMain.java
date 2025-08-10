package alpha3166.optimpdf.setview;

import java.nio.file.Path;

import alpha3166.optimpdf.framework.AbstractCommandMain;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "setview", description = "Set initial page view.")
public class SetViewMain extends AbstractCommandMain {
  @Mixin
  SetViewOption opt;

  @Override
  protected boolean processFile(Path src, Path temp, boolean dryRun, boolean quiet) throws Exception {
    PdfHandler.setView(src, temp, opt.clear, opt.pageLayout, opt.rightToLeft, opt.fit);
    return true;
  }
}
