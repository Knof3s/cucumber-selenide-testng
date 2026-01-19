package helpers.apiHelpers;

import java.util.Map;

//Класс-строитель апи запроса для отправки на сервер
//Объект этого класса создается во вторую очередь, после создания объекта Builder, который передается в конструктор этого класса
//Пример использования:
//ApiRequest apiRequest = new ApiRequest(builder);
public class ApiRequest {
    public final String method;
    public final String endpoint;
    public final Map<String, String> headers;
    public final Map<String, Object> params;
    public final String body;

    public ApiRequest(Builder builder) {
        this.method = builder.method;
        this.endpoint = builder.endpoint;
        this.headers = builder.headers;
        this.params = builder.params;
        this.body = builder.body;
    }
}