package vlcinterface;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class created by c_moi on 10/04/2016.
 */
public class VLCUtils {

    private static Object getNestedObject(String obj, String... keys) {
        return getNestedObject(getJSONObject(obj), keys);
    }

    private static Object getNestedObject(JSONObject obj, String... keys) {
        if (obj == null) {
            return null;
        }

        for (String key : keys) {
            final Object value = obj.get(key);
            if (value != null && value.getClass() == JSONObject.class) {
                obj = (JSONObject) value;
            } else {
                return value;
            }
        }
        return obj;
    }

    private static JSONObject getJSONObject(String json) {
        JSONObject obj = null;
        if (json != null) {
            try {
                obj = (JSONObject) new JSONParser().parse(json);
            } catch (ParseException ex) {
                Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }

    private static String sendRequest(String parameter) {
        return RequestUtils.getInstance().sendGet(parameter);
    }
}
