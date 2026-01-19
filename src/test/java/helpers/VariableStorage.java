package helpers;


import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class VariableStorage {

    private static String messageError;
    private static final ThreadLocal<Map<String, Object>> threadVariables =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    public static void setVariable(String key, Object value) {
        log.info("Создана переменная: {} со значением: {}", key, value.toString());
        threadVariables.get().put(key, value);
    }

    public static Object getVariable(String key) {
        if (!variableExists(key)) {
            messageError = "Переменная с ключом '" + key + "' не найдена";
            log.error(messageError);
            throw new IllegalArgumentException(messageError);
        }
        Object var = threadVariables.get().get(key);
        log.info("Получение переменной [{}] по ключу [{}]", var.toString(), key);
        return threadVariables.get().get(key);
    }

    public static <T> T getVariable(String key, Class<T> type) {
        Object value = getVariable(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        messageError = "Переменная с ключом '" + key + "' существует, но её тип '"
                + value.getClass().getName() + "' несовместим с запрашиваемым типом '" + type.getName() + "'";
        log.error(messageError);
        throw new IllegalArgumentException(messageError);
    }

    public static void clearVariables() {
        log.info("Хранилище переменных было полностью очищено");
        threadVariables.get().clear();
    }

    public static boolean variableExists(String key) {
        log.info("Проверяется наличие переменной с ключом: '{}'", key);
        return threadVariables.get().containsKey(key);
    }

}
