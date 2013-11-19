package nz.net.osnz.common.scanner;

import java.net.URL;

public interface Notifier {

    public void underlayWar(URL url) throws Exception;

    public void jar(URL url) throws Exception;

    public void dir(URL url) throws Exception;
}
