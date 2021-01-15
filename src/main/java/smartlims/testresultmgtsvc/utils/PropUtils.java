package smartlims.testresultmgtsvc.utils;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropUtils {
    public static Map<String, String> getPropByFolderName(String folderName) {
        try {
            Map<String, String> result = new HashMap<>();
            Properties properties=new Properties();
            InputStreamReader inputstream = new InputStreamReader(PropUtils.class.getClassLoader().getResourceAsStream("application.properties"), "UTF-8");
            properties.load(inputstream);
            result.clear();
            result.put("sampletype", properties.getProperty(folderName + ".sampletype"));
            result.put("testitemtype", properties.getProperty(folderName + ".testitemtype"));
            result.put("csxmdescription", properties.getProperty(folderName + ".csxmdescription"));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getPropInteger(String propName) {
        try {
            Integer result = 0;
            Properties properties=new Properties();
            InputStreamReader inputstream = new InputStreamReader(PropUtils.class.getClassLoader().getResourceAsStream("application.properties"), "UTF-8");
            properties.load(inputstream);
            if (properties.getProperty(propName) != null) {
                result = Integer.valueOf(properties.getProperty(propName));
            }
            return result;
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getPropString(String propName) {
        try {
            String result = "";
            Properties properties=new Properties();
            InputStreamReader inputstream = new InputStreamReader(PropUtils.class.getClassLoader().getResourceAsStream("application.properties"), "UTF-8");
            properties.load(inputstream);
            if (properties.getProperty(propName) != null) {
                result = properties.getProperty(propName);
            }
            return result;
        } catch (Exception e) {
            return "";
        }
    }

}