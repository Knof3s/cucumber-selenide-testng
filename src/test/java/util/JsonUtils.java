package util;

import com.google.gson.JsonSyntaxException;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JsonUtils {
    private static final Gson gson = new Gson();

    //Метод, сериализующий объект
    public static String serializeObject(Object obj){
        return  gson.toJson(obj);
    }

    //Метод, десериализующий объект из строки
    public static <T> T deserializeObjectFromJson(String jsonString, Class<T> clazz) {
        try {
            return gson.fromJson(jsonString, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}