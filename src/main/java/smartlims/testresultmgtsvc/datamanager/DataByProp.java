package smartlims.testresultmgtsvc.datamanager;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

public class DataByProp {
    
    public static ArrayList<LookupStateItem> getStatusOrder(String id, String text, String order) {
        try {
            ArrayList<LookupStateItem> sampleStatusList = new ArrayList<>();
            Properties properties = new Properties();
            InputStreamReader inputstream = new InputStreamReader(DataByProp.class.getClassLoader().getResourceAsStream("application.properties"), "UTF-8");
            properties.load(inputstream);
            String[] ids = properties.getProperty(id).split(",");
            String[] texts = properties.getProperty(text).split(",");
            String[] orders = properties.getProperty(order).split(",");
            if ((ids.length == texts.length) && (ids.length == orders.length)) {
                for (Integer i=0; i < ids.length; i++) {
                    sampleStatusList.add(new LookupStateItem(ids[i], texts[i], orders[i]));
                } 
                return sampleStatusList;    
            }
        } catch (Exception e) {
            return null;
        }
        return null;    
    }
    
}