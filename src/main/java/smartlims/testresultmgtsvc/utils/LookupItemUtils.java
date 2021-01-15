package smartlims.testresultmgtsvc.utils;

import java.util.ArrayList;

import smartlims.testresultmgtsvc.datamanager.LookupItem;
import smartlims.testresultmgtsvc.datamanager.LookupStateItem;


public class LookupItemUtils {

    // 通过id值找到LookupItem
    public static LookupItem findLookupItemById(String Id, ArrayList<LookupItem> list) {
        for (LookupItem item : list) {
            if (item.getId().equals(Id)) {
                return item;
            }
        }
        return new LookupItem("", "");
    }

    // 通过id找到value
    public static String findLookupValueById(String Id, ArrayList<LookupItem> list) {
        for (LookupItem item : list) {
            if (item.getId().equals(Id)) {
                return item.getValue();
            }
        }
        return "";
    }

    // 通过id值找到stateitem
    public static LookupStateItem findLookupStateItemById(String Id, ArrayList<LookupStateItem> list) {
        for (LookupStateItem item : list) {
            if (item.getId().equals(Id)) {
                return item;
            }
        }
        return new LookupStateItem("", "", "");
    }
}