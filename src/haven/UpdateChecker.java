package haven;

import java.io.*;
import java.nio.charset.Charset;
import java.net.URL;
import org.json.JSONObject;

public class UpdateChecker extends Thread {
    private final String url = "https://api.github.com/repos/romovs/amber/releases/latest";

    public void run() {
        try {
            JSONObject json = getjson();
            String latestver = json.getString("tag_name");
            String latesturl = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
            if (isnewer(Config.version, latestver) && HavenPanel.lui != null && HavenPanel.lui.root != null) {
                Window updwnd = new UpdateWnd(latesturl, latestver);
                HavenPanel.lui.root.add(updwnd);
                updwnd.show();
                updwnd.raise();
            }
        } catch (Exception e) {
            System.err.println("WARNING: error checking for updates");
            e.printStackTrace();
        }
    }

    private JSONObject getjson() throws IOException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return new JSONObject(sb.toString());
        } finally {
            is.close();
        }
    }

    private boolean isnewer(String currentver, String latestver) {
        String[] vtokc = currentver.split("[\\.]+");
        int majc = Integer.parseInt(vtokc[0]);
        int minc = Integer.parseInt(vtokc[1]);
        int ptcc = Integer.parseInt(vtokc[2]);
        String[] vtokl = latestver.split("[\\.]+");
        int majl = Integer.parseInt(vtokl[0]);
        int minl = Integer.parseInt(vtokl[1]);
        int ptcl = Integer.parseInt(vtokl[2]);
        return majl > majc || (minl > minc && majl >= majc) || (ptcl > ptcc && majl >= majc && minl >= minc);
    }
}
